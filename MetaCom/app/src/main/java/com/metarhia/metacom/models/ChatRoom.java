package com.metarhia.metacom.models;

import com.metarhia.metacom.connection.AndroidJSTPConnection;
import com.metarhia.metacom.interfaces.MessageListener;
import com.metarhia.metacom.interfaces.MessageSentCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * MetaCom chat room
 *
 * @author lidaamber
 */

public class ChatRoom {

    /**
     * Chat room name
     */
    private String mChatRoomName;

    /**
     * MetaCom connection
     */
    private final AndroidJSTPConnection mConnection;

    /**
     * Message listeners for incoming chat room messages
     */
    private final List<MessageListener> mMessageListeners;


    /**
     * Creates new chat room by name
     *
     * @param chatRoomName chat name
     */
    public ChatRoom(String chatRoomName, AndroidJSTPConnection connection) {
        mChatRoomName = chatRoomName;
        mConnection = connection;
        mMessageListeners = new ArrayList<>();
    }

    /**
     * Gets chat room name
     *
     * @return chat room name
     */
    public String getChatRoomName() {
        return mChatRoomName;
    }

    /**
     * Sets chat room name
     *
     * @param chatRoomName chat room name
     */
    public void setChatRoomName(String chatRoomName) {
        mChatRoomName = chatRoomName;
    }

    /**
     * Sends message to chat room
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
