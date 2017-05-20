package com.metarhia.metacom.models;

import com.metarhia.jstp.core.Handlers.ManualHandler;
import com.metarhia.jstp.core.JSTypes.JSArray;
import com.metarhia.jstp.core.JSTypes.JSObject;
import com.metarhia.jstp.core.JSTypes.JSValue;
import com.metarhia.metacom.connection.AndroidJSTPConnection;
import com.metarhia.metacom.connection.Errors;
import com.metarhia.metacom.connection.JSTPOkErrorHandler;
import com.metarhia.metacom.interfaces.MessageListener;
import com.metarhia.metacom.interfaces.MessageSentCallback;
import com.metarhia.metacom.utils.Constants;

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
        initIncomingMessagesListener();
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
    public void sendMessage(Message message, final MessageSentCallback callback) {
        JSArray args = new JSArray();
        args.add(message.getContent());
        mConnection.cacheCall(Constants.META_COM, "send", args, new JSTPOkErrorHandler() {
            @Override
            public void onOk(JSArray args) {
                callback.onMessageSent();
            }

            @Override
            public void onError(Integer errorCode) {
                callback.onMessageSentError(Errors.getErrorByCode(errorCode));
            }
        });

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

    /**
     * Adds message event handler to JSTP connection
     */
    private void initIncomingMessagesListener() {
        mConnection.addEventHandler(Constants.META_COM, "message", new ManualHandler() {
            @Override
            public void invoke(JSValue jsValue) {
                JSArray messagePayload = (JSArray) ((JSObject) jsValue).get("message");
                String messageContent = (String) messagePayload.get(0).getGeneralizedValue();

                Message message = new Message(MessageType.TEXT, messageContent, true);
                for (MessageListener listener : mMessageListeners) {
                    listener.onMessageReceived(message);
                }
            }
        });
    }
}
