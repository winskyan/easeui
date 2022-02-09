package com.hyphenate.easeui.modules.chat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.hyphenate.easeui.R;
import com.hyphenate.easeui.adapter.ReactionUserListGridAdapter;
import com.hyphenate.easeui.widget.EaseRecyclerView;

import java.util.ArrayList;
import java.util.List;


public class EaseChatReactionUserListView extends PopupWindow {
    private final static String TAG = EaseChatReactionUserListView.class.getSimpleName();
    private final ReactionUserListGridAdapter mListAdapter;
    private List<String> mData;

    public EaseChatReactionUserListView(Context context) {
        super(context);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setOutsideTouchable(true);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View contentView = View.inflate(context.getApplicationContext(), R.layout.ease_widget_reaction_user_list_layout, null);
        setContentView(contentView);
        EaseRecyclerView userList = contentView.findViewById(R.id.rv_list_user_list);
        userList.setLayoutManager(new LinearLayoutManager(context));
        mListAdapter = new ReactionUserListGridAdapter();
        userList.setAdapter(mListAdapter);
    }

    public void updateData(List<String> data) {
        if (null != data) {
            if (data.size() > 3) {
                mData = new ArrayList<>(3);
                for (int i = 0; i < 3; i++) {
                    mData.add(data.get(i));
                }
            } else {
                mData = new ArrayList<>(data);
            }
            mListAdapter.setData(mData);
        }

    }
}

