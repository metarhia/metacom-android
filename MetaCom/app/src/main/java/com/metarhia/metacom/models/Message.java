package com.metarhia.metacom.models;

/**
 * Message structure
 *
 * @author lidaamber
 */

public class Message {

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
}
