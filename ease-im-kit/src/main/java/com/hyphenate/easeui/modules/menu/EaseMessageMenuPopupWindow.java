package com.hyphenate.easeui.modules.menu;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.PopupWindow;

import com.hyphenate.easeui.R;
import com.hyphenate.easeui.utils.EaseCommonUtils;

public class EaseMessageMenuPopupWindow extends PopupWindow {

    private Context mContext;
    private float mShowAlpha = 0.88f;
    private Drawable mBackgroundDrawable;
    private boolean mCloseChangeBg;

    public EaseMessageMenuPopupWindow(Context context) {
        this.mContext = context;
        initBasePopupWindow();
    }

    /**
     * 第二个参数为改变背景色
     *
     * @param context
     * @param closeChangeBg 是否改变背景色
     */
    public EaseMessageMenuPopupWindow(Context context, boolean closeChangeBg) {
        this.mContext = context;
        this.mCloseChangeBg = closeChangeBg;
        initBasePopupWindow();
    }

    /**
     * 设置背景透明度
     *
     * @param alpha
     */
    public void setBackgroundAlpha(float alpha) {
        this.mShowAlpha = alpha;
    }

    @Override
    public void setOutsideTouchable(boolean touchable) {
        super.setOutsideTouchable(touchable);
        if (touchable) {
            if (mBackgroundDrawable == null) {
                mBackgroundDrawable = new ColorDrawable(0x00000000);
            }
            super.setBackgroundDrawable(mBackgroundDrawable);
        } else {
            super.setBackgroundDrawable(null);
        }
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        mBackgroundDrawable = background;
        setOutsideTouchable(isOutsideTouchable());
    }

    /**
     * 初始化BasePopupWindow的一些信息
     */
    private void initBasePopupWindow() {
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setOutsideTouchable(true);  //默认设置outside点击无响应
        setFocusable(true);
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setAnimationStyle(R.style.message_menu_popup_window_anim_style);
    }

    @Override
    public void setContentView(View contentView) {
        if (contentView != null) {
            contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            super.setContentView(contentView);
            addKeyListener(contentView);
        }
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        setWindowBackgroundAlpha(mShowAlpha);
    }

    @Override
    public void showAsDropDown(View anchor) {
        super.showAsDropDown(anchor);
        setWindowBackgroundAlpha(mShowAlpha);
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        super.showAsDropDown(anchor, xoff, yoff);
        setWindowBackgroundAlpha(mShowAlpha);
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        super.showAsDropDown(anchor, xoff, yoff, gravity);
        setWindowBackgroundAlpha(mShowAlpha);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        setWindowBackgroundAlpha(1.0f);
    }

    /**
     * 为窗体添加outside点击事件
     */
    private void addKeyListener(View contentView) {
        if (contentView != null) {
            contentView.setFocusable(true);
            contentView.setFocusableInTouchMode(true);
            contentView.setOnKeyListener(new View.OnKeyListener() {

                @Override
                public boolean onKey(View view, int keyCode, KeyEvent event) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_BACK:
                            dismiss();
                            return true;
                        default:
                            break;
                    }
                    return false;
                }
            });
        }
    }

    /**
     * 控制窗口背景的不透明度
     */
    private void setWindowBackgroundAlpha(float alpha) {
        Window window = ((Activity) getContext()).getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.alpha = alpha;
        window.setAttributes(layoutParams);
    }


    public int getNavBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public void setViewLayoutParams(View view, int nWidth, int nHeight) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp.height != nHeight || lp.width != nWidth) {
            lp.width = nWidth;
            lp.height = nHeight;
            view.setLayoutParams(lp);
            view.requestLayout();
        }
    }

    public interface OnPopupWindowItemClickListener {
        void onReactionItemClick(ReactionItemBean item, boolean isAdd);

        void onMenuItemClick(MenuItemBean item);
    }

    public interface OnPopupWindowDismissListener {
        void onDismiss(PopupWindow menu);
    }

}