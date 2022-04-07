package com.hyphenate.easeui.domain;

import java.util.List;

public class EaseReactionEmojiconEntity {
    private EaseEmojicon emojicon;
    private int count;
    private List<String> userList;

    public EaseEmojicon getEmojicon() {
        return emojicon;
    }

    public void setEmojicon(EaseEmojicon emojicon) {
        this.emojicon = emojicon;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<String> getUserList() {
        return userList;
    }

    public void setUserList(List<String> userList) {
        this.userList = userList;
    }

    @Override
    public String toString() {
        return "EaseReactionEmojiconEntity{" +
                "emojicon=" + emojicon +
                ", count=" + count +
                ", userList=" + userList +
                '}';
    }
}
