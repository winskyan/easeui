package com.hyphenate.easeui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.hyphenate.easeui.R;
import com.hyphenate.easeui.widget.EaseImageView;


public class ReactionUserListGridAdapter extends EaseBaseRecyclerViewAdapter<String> {


    @Override
    public ReactionUserInfoViewHolder getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.ease_row_reaction_user_info, parent, false);
        return new ReactionUserInfoViewHolder(view);
    }

    private static class ReactionUserInfoViewHolder extends ViewHolder<String> {
        private EaseImageView avatar;
        private TextView userName;

        public ReactionUserInfoViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            avatar = findViewById(R.id.iv_avatar);
            userName = findViewById(R.id.tv_user_name);
        }

        @Override
        public void setData(String item, int position) {
            userName.setText(item);
        }


    }
}
