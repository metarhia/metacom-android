package com.metarhia.metacom.models;

import java.io.Serializable;

/**
 * Message structure
 *
 * @author lidaamber
 */

public class Message implements Serializable {

    /**
     * Type of the message
     */
    private final MessageType mType;

    /**
     * Message content
     */
    private final String mContent;

    /**
     * Shows if message belongs to user or it's incoming
     */
    private final boolean mIsIncoming;

    /**
     * Shows if message is sending or receiving now
     */
    private boolean mIsWaiting;

    /**
     * Creates message with specified type and content
     *
     * @param type    message type
     * @param content message content
     */
    public Message(MessageType type, String content, boolean isIncoming) {
        mType = type;
        mContent = content;
        mIsIncoming = isIncoming;
    }

    /**
     * Gets message type
     *
     * @return message type
     */
    public MessageType getType() {
        return mType;
    }

    /**
     * Gets message content
     *
     * @return message content
     */
    public String getContent() {
        return mContent;
    }

    public boolean isIncoming() {
        return mIsIncoming;
    }

    public boolean isWaiting() {
        return mIsWaiting;
    }

    public void setWaiting(boolean isWaiting) {
        mIsWaiting = isWaiting;
    }
}
