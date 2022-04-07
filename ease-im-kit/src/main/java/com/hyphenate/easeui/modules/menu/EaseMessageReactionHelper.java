package com.hyphenate.easeui.modules.menu;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCursorResult;
import com.hyphenate.chat.EMMessageReaction;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter;
import com.hyphenate.easeui.domain.EaseReactionEmojiconEntity;
import com.hyphenate.easeui.interfaces.OnItemClickListener;
import com.hyphenate.easeui.modules.chat.EaseChatReactionView;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.util.EMLog;

import java.util.ArrayList;
import java.util.List;

public class EaseMessageReactionHelper {
    private static final String TAG = EaseMessageReactionHelper.class.getSimpleName();

    private static final int USER_LIST_PAGE_SIZE = 1;

    public enum LoadMoreStatus {
        IS_LOADING, HAS_MORE, NO_MORE_DATA
    }

    private EaseMessageMenuPopupWindow mPopupWindow;
    private Context mContext;
    private ReactionAdapter mReactionAdapter;
    private UserListAdapter mUserAdapter;
    private EaseMessageMenuPopupWindow.OnPopupWindowItemClickListener mItemClickListener;
    private EaseMessageMenuPopupWindow.OnPopupWindowDismissListener mDismissListener;
    private boolean mTouchable;
    private Drawable mBackground;
    private View mLayout;
    private RecyclerView mReactionListView;
    private RecyclerView mUserListView;

    private int mReactionListHeight;
    private int mUserListHeight;

    private View mTopView;
    private ImageView mMessageView;
    private View mPopupView;

    private List<EaseReactionEmojiconEntity> mReactionData;
    private EaseReactionEmojiconEntity mCurrentEaseReactionEntity;
    private EaseChatReactionView.OnReactionItemListener mOnReactionItemListener;
    private String mCurrentEaseReactionBegin;
    private String mMsgId;

    /**
     * switch to main thread
     */
    private final Handler mMainThreadHandler;

    private LoadMoreStatus mLoadMoreStatus;


    public EaseMessageReactionHelper() {
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
        clearData();

        mMainThreadHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * @param context
     */
    public void init(@NonNull Context context, EaseChatReactionView.OnReactionItemListener onReactionItemListener) {
        this.mContext = context;
        this.mOnReactionItemListener = onReactionItemListener;
        mPopupWindow = new EaseMessageMenuPopupWindow(context, true);
        mLayout = View.inflate(context.getApplicationContext(), R.layout.ease_layout_message_reaction_popupwindow, null);
        mPopupWindow.setContentView(mLayout);
        mPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        mTopView = mLayout.findViewById(R.id.top_view);
        mMessageView = mLayout.findViewById(R.id.message_view);
        mPopupView = mLayout.findViewById(R.id.popup_view);

        mReactionListView = mLayout.findViewById(R.id.rv_reaction_list);
        LinearLayoutManager ms = new LinearLayoutManager(mContext);
        ms.setOrientation(LinearLayoutManager.HORIZONTAL);
        mReactionListView.setLayoutManager(ms);

        mReactionAdapter = new ReactionAdapter();
        mReactionListView.setAdapter(mReactionAdapter);
        mReactionAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mCurrentEaseReactionEntity = mReactionAdapter.getItem(position);
                mReactionAdapter.setCurrentEntity(mCurrentEaseReactionEntity);
                mCurrentEaseReactionBegin = "0";
                mCurrentEaseReactionEntity.getUserList().clear();
                asyncReactionUserList();
            }
        });
        mReactionListView.addItemDecoration(new ReactionSpacesItemDecoration((int) dip2px(mContext, 5)));
        mReactionListView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mReactionListHeight = mReactionListView.getHeight();
            }
        });


        mUserListView = mLayout.findViewById(R.id.rv_user_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        mUserListView.setLayoutManager(linearLayoutManager);

        mUserAdapter = new UserListAdapter(new ReactionItemDelete() {
            @Override
            public void onReactionDelete() {
                EMLog.i(TAG, "onReactionDelete mCurrentEaseReactionEntity=" + mCurrentEaseReactionEntity.toString());
                if (null != mOnReactionItemListener) {
                    mOnReactionItemListener.removeReaction(mCurrentEaseReactionEntity);
                }
                dismiss();
            }
        });
        mUserAdapter.hideEmptyView(true);
        mUserListView.setAdapter(mUserAdapter);

        mUserListView.addItemDecoration(new UserListSpacesItemDecoration((int) dip2px(mContext, 20)));
        mUserListView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mUserListHeight = mUserListView.getHeight();
            }
        });

        mUserListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //has more data
                    if (mLoadMoreStatus == LoadMoreStatus.HAS_MORE
                            && linearLayoutManager.findLastVisibleItemPosition() != -1
                            && linearLayoutManager.findLastVisibleItemPosition() == linearLayoutManager.getItemCount() - 1) {
                        //load more
                        asyncReactionUserList();
                    }
                }
            }
        });

        mTopView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                dismiss();
                return true;
            }
        });
    }

    public void clearData() {
        if (null != mReactionData) {
            mReactionData.clear();
        }
        mReactionListHeight = 0;
        mUserListHeight = 0;
        mCurrentEaseReactionEntity = null;
        mCurrentEaseReactionBegin = "0";
    }

    public void setOutsideTouchable(boolean touchable) {
        this.mTouchable = touchable;
    }

    public void setBackgroundDrawable(Drawable background) {
        this.mBackground = background;
    }

    public void setReactionData(List<EaseReactionEmojiconEntity> data, String msgId) {
        mReactionData = new ArrayList<>(data);
        mMsgId = msgId;
    }

    private void showPre() {
        mPopupWindow.setOutsideTouchable(mTouchable);
        mPopupWindow.setBackgroundDrawable(mBackground);
    }

    public void show(View parent, View v) {
        showPre();
        initReactionData();

        //根据条目选择spanCount
        if (mReactionData.size() <= 0) {
            EMLog.e(TAG, "reaction span count should be at least 1. Provided " + mReactionData.size());
            return;
        }

        final float screenWidth = EaseCommonUtils.getScreenInfo(mContext)[0];
        final float screenHeight = EaseCommonUtils.getScreenInfo(mContext)[1];
        final int minPopupWindowHeight = (int) screenHeight * 2 / 5;
        final int maxPopupWindowHeight = (int) screenHeight - mPopupWindow.getNavBarHeight(mContext);
        mPopupWindow.showAtLocation(v.getRootView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        mPopupWindow.setViewLayoutParams(mPopupView, (int) screenWidth, minPopupWindowHeight);

        mLayout.setOnTouchListener(new View.OnTouchListener() {
            int orgX, orgY;
            int offsetX, offsetY;
            int popupWindowCurHeight = minPopupWindowHeight;
            int slippingHeight;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        orgX = (int) event.getRawX();
                        orgY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        offsetX = (int) event.getRawX() - orgX;
                        offsetY = (int) event.getRawY() - orgY;
                        slippingHeight = popupWindowCurHeight - offsetY;
                        mPopupWindow.setViewLayoutParams(mPopupView, (int) screenWidth, slippingHeight);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (offsetY > 0) {
                            popupWindowCurHeight = minPopupWindowHeight;
                        } else if (offsetY < 0) {
                            popupWindowCurHeight = maxPopupWindowHeight;
                        }
                        mPopupWindow.setViewLayoutParams(mPopupView, (int) screenWidth, popupWindowCurHeight);
                        break;
                }
                return true;
            }
        });
    }

    private void initReactionData() {
        mReactionAdapter.setData(mReactionData);
        mCurrentEaseReactionBegin = "0";

        mCurrentEaseReactionEntity = mReactionData.get(0);
        mCurrentEaseReactionEntity.getUserList().clear();
        mReactionAdapter.setCurrentEntity(mCurrentEaseReactionEntity);
        asyncReactionUserList();
    }


    private void asyncReactionUserList() {
        if (TextUtils.isEmpty(mMsgId) || TextUtils.isEmpty(mCurrentEaseReactionEntity.getEmojicon().getIdentityCode())) {
            return;
        }
        EMCursorResult<Object> cursor = new EMCursorResult<>();
        EMClient.getInstance().chatManager().asyncGetReactionDetail(mMsgId, mCurrentEaseReactionEntity.getEmojicon().getIdentityCode(), mCurrentEaseReactionBegin,
                USER_LIST_PAGE_SIZE, new EMValueCallBack<EMCursorResult<EMMessageReaction>>() {
                    @Override
                    public void onSuccess(EMCursorResult<EMMessageReaction> value) {
                        mCurrentEaseReactionBegin = value.getCursor();
                        List<String> userList = mCurrentEaseReactionEntity.getUserList();
                        List<EMMessageReaction> list = value.getData();

                        EMMessageReaction emMessageReaction = null;
                        if (null != list && list.size() >= 1) {
                            emMessageReaction = list.get(0);
                        }
                        if (null != emMessageReaction) {
                            userList.addAll(emMessageReaction.getUserList());
                        }
                        if (mCurrentEaseReactionEntity.getCount() > userList.size() && !TextUtils.isEmpty(mCurrentEaseReactionBegin)) {
                            mLoadMoreStatus = LoadMoreStatus.HAS_MORE;
                        } else {
                            mLoadMoreStatus = LoadMoreStatus.NO_MORE_DATA;
                        }
                        mCurrentEaseReactionEntity.setUserList(userList);
                        mUserListView.post(new Runnable() {
                            @Override
                            public void run() {
                                initReactionUserListData();
                            }
                        });
                    }

                    @Override
                    public void onError(int error, String errorMsg) {

                    }
                });
    }

    private void initReactionUserListData() {
        EMLog.i(TAG, "reaction mCurrentEaseReactionBegin=" + mCurrentEaseReactionBegin + "," + mCurrentEaseReactionEntity.toString());
        List<String> userList = new ArrayList<>(mCurrentEaseReactionEntity.getUserList());
        if (true) {
            userList.remove(EMClient.getInstance().getCurrentUser());
            userList.add(0, EMClient.getInstance().getCurrentUser());
        }
        mUserAdapter.setData(userList);
    }

    public void dismiss() {
        if (mPopupWindow == null) {
            throw new NullPointerException("please must init first!");
        }
        mPopupWindow.dismiss();
        if (mDismissListener != null) {
            mDismissListener.onDismiss(mPopupWindow);
        }
        clearData();
    }


    /**
     * 设置条目点击事件
     *
     * @param listener
     */
    public void setOnPopupReactionItemClickListener(EaseMessageMenuPopupWindow.OnPopupWindowItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    /**
     * 监听PopupMenu dismiss事件
     *
     * @param listener
     */
    public void setOnPopupMenuDismissListener(EaseMessageMenuPopupWindow.OnPopupWindowDismissListener listener) {
        this.mDismissListener = listener;
    }

    public PopupWindow getPopupWindow() {
        return mPopupWindow;
    }

    public View getView() {
        return mLayout;
    }

    /**
     * dip to px
     *
     * @param context
     * @param value
     * @return
     */
    public static float dip2px(Context context, float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }

    private static class ReactionAdapter extends EaseBaseRecyclerViewAdapter<EaseReactionEmojiconEntity> {

        private static EaseReactionEmojiconEntity mCurrentEntity;

        public void setCurrentEntity(EaseReactionEmojiconEntity currentEntity) {
            mCurrentEntity = currentEntity;
            notifyDataSetChanged();
        }

        @Override
        public ReactionViewHolder getViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.ease_row_reaction_popupwindow, parent, false);
            return new ReactionViewHolder(view);
        }

        private static class ReactionViewHolder extends ViewHolder<EaseReactionEmojiconEntity> {
            private View reactionLayout;
            private ImageView emojicon;
            private TextView count;

            public ReactionViewHolder(@NonNull View itemView) {
                super(itemView);
            }

            @Override
            public void initView(View itemView) {
                reactionLayout = findViewById(R.id.reaction_layout);
                emojicon = findViewById(R.id.iv_emojicon);
                count = findViewById(R.id.tv_count);
            }

            @Override
            public void setData(EaseReactionEmojiconEntity item, int position) {
                if (0 != item.getEmojicon().getIcon()) {
                    emojicon.setImageResource(item.getEmojicon().getIcon());
                }
                count.setText(String.valueOf(item.getCount()));

                if (null != mCurrentEntity && mCurrentEntity.getEmojicon().getIdentityCode().equals(item.getEmojicon().getIdentityCode())) {
                    reactionLayout.setBackgroundResource(R.drawable.ease_bg_message_reaction_popupwindow);
                } else {
                    reactionLayout.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        }
    }

    private static class UserListAdapter extends EaseBaseRecyclerViewAdapter<String> {
        private static EaseMessageReactionHelper.ReactionItemDelete mDeleteListener;

        public UserListAdapter(EaseMessageReactionHelper.ReactionItemDelete listener) {
            mDeleteListener = listener;
        }

        @Override
        public UserListViewHolder getViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.ease_layout_item_message_reaction_popupwindow, parent, false);
            return new UserListViewHolder(view);
        }

        private static class UserListViewHolder extends ViewHolder<String> {
            private ImageView ivUserAvatar;
            private TextView tvUserName;
            private ImageView ivDelete;

            public UserListViewHolder(@NonNull View itemView) {
                super(itemView);
            }

            @Override
            public void initView(View itemView) {
                ivUserAvatar = findViewById(R.id.iv_user_avatar);
                tvUserName = findViewById(R.id.tv_user_name);
                ivDelete = findViewById(R.id.iv_delete);
                ivDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mDeleteListener) {
                            mDeleteListener.onReactionDelete();
                        }
                    }
                });
            }

            @Override
            public void setData(String item, int position) {
                tvUserName.setText(item);
                if (EMClient.getInstance().getCurrentUser().equals(item)) {
                    ivDelete.setVisibility(View.VISIBLE);
                } else {
                    ivDelete.setVisibility(View.GONE);
                }

                EaseUserUtils.setUserAvatar(itemView.getContext(), item, ivUserAvatar);
            }
        }
    }

    private static class ReactionSpacesItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;

        public ReactionSpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, @NonNull View view,
                                   RecyclerView parent, @NonNull RecyclerView.State state) {
            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.left = 0;
            } else {
                outRect.left = space;
            }
        }
    }

    private static class UserListSpacesItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;

        public UserListSpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, @NonNull View view,
                                   RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.left = 0;
            outRect.right = 0;
            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = 0;
            } else {
                outRect.top = 0;
            }
        }
    }

    public interface ReactionItemDelete {
        void onReactionDelete();
    }


}

