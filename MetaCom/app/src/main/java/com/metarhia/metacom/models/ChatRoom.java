package com.metarhia.metacom.models;

import android.util.Base64;

import com.metarhia.jstp.core.Handlers.ManualHandler;
import com.metarhia.jstp.core.JSTypes.JSArray;
import com.metarhia.jstp.core.JSTypes.JSObject;
import com.metarhia.jstp.core.JSTypes.JSValue;
import com.metarhia.metacom.connection.AndroidJSTPConnection;
import com.metarhia.metacom.connection.Errors;
import com.metarhia.metacom.connection.JSTPOkErrorHandler;
import com.metarhia.metacom.interfaces.FileUploadedCallback;
import com.metarhia.metacom.interfaces.MessageListener;
import com.metarhia.metacom.interfaces.MessageSentCallback;
import com.metarhia.metacom.utils.Constants;
import com.metarhia.metacom.utils.FileUtils;

import java.io.File;
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

    /**
     * Uploads file in chat
     *
     * @param file     file to upload
     * @param callback callback after file upload (success and error)
     */
    public void uploadFile(File file, FileUploadedCallback callback) {
        FileUtils.uploadSplitFile(file, new FileUtils.FileUploadingInterface() {
            @Override
            public void sendChunk(byte[] chunk, JSTPOkErrorHandler handler) {
                ChatRoom.this.sendChunk(chunk, handler);
            }

            @Override
            public void endFileUpload(FileUploadedCallback callback) {
                ChatRoom.this.endFileUpload(callback);
            }
        }, callback);
    }

    /**
     * Sends chunk to server
     *
     * @param chunk   chunk to send
     * @param handler JSTP handler
     */
    private void sendChunk(byte[] chunk, JSTPOkErrorHandler handler) {
        JSArray args = new JSArray();
        args.add(Base64.encode(chunk, Base64.DEFAULT));
        mConnection.cacheCall(Constants.META_COM, "sendFileChunkToChat", args, handler);
    }

    /**
     * Ends file upload to server
     *
     * @param callback callback after ending file upload
     */
    private void endFileUpload(final FileUploadedCallback callback) {
        mConnection.cacheCall(Constants.META_COM, "endChatFileTransfer", new JSArray(),
                new JSTPOkErrorHandler() {
                    @Override
                    public void onOk(JSArray args) {
                        String fileCode = (String) args.get(0).getGeneralizedValue();
                        callback.onFileUploaded(fileCode);
                    }

                    @Override
                    public void onError(Integer errorCode) {
                        callback.onFileUploadError(Errors.getErrorByCode(errorCode));
                    }
                });
    }
}
