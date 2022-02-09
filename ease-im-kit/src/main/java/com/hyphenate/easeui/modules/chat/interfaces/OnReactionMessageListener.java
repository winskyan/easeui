package com.hyphenate.easeui.modules.chat.interfaces;


import com.hyphenate.chat.EMMessage;

public interface OnReactionMessageListener {

    /**
     * 表情评论成功
     *
     * @param message
     */
    void addReactionMessageSuccess(EMMessage message);

    /**
     * 情评论失败
     *
     * @param message
     * @param code
     * @param error
     */
    void addReactionMessageFail(EMMessage message, int code, String error);

    /**
     * 删除表情评论成功
     *
     * @param message
     */
    void removeReactionMessageSuccess(EMMessage message);

    /**
     * 删除情评论失败
     *
     * @param message
     * @param code
     * @param error
     */
    void removeReactionMessageFail(EMMessage message, int code, String error);

}
