package com.metarhia.metacom.models;

import com.metarhia.metacom.interfaces.MessageListener;
import com.metarhia.metacom.interfaces.MessageSentCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * MetaCom chat
 *
 * @author lidaamber
 */

public class Chat {

    /**
     * Chat name
     */
    private String mChatName;

    /**
     * Message listeners for incoming chat messages
     */
    private List<MessageListener> mMessageListeners;


    /**
     * Creates new chat by name
     *
     * @param chatName chat name
     */
    public Chat(String chatName) {
        mChatName = chatName;
        mMessageListeners = new ArrayList<>();
    }

    /**
     * Gets chat name
     *
     * @return chat name
     */
    public String getChatName() {
        return mChatName;
    }

    /**
     * Sets chat name
     *
     * @param chatName chat name
     */
    public void setChatName(String chatName) {
        mChatName = chatName;
    }

    /**
     * Sends message to chat
     *
     * @param message  message to be sent
     * @param callback callback after message sending (success and error)
     */
    public void sendMessage(Message message, MessageSentCallback callback) {
        // TODO send message

        callback.onMessageSent();
    }

    /**
     * Adds new incoming messages listener to chat
     *
     * @param listener incoming messages listener
     */
    public void addMessageListener(MessageListener listener) {
        mMessageListeners.add(listener);
    }

    /**
     * Removes incoming messages listener
     *
     * @param listener incoming messages listener
     */
    public void removeMessageListener(MessageListener listener) {
        mMessageListeners.remove(listener);
    }
}
