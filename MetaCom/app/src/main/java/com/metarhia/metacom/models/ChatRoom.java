package com.metarhia.metacom.models;

import android.util.Base64;

import com.metarhia.jstp.compiler.annotations.handlers.Array;
import com.metarhia.jstp.handlers.ExecutableHandler;
import com.metarhia.metacom.connection.AndroidJSTPConnection;
import com.metarhia.metacom.connection.Errors;
import com.metarhia.metacom.connection.JSTPOkErrorHandler;
import com.metarhia.metacom.interfaces.ChatReconnectionListener;
import com.metarhia.metacom.interfaces.FileDownloadedListener;
import com.metarhia.metacom.interfaces.FileUploadedCallback;
import com.metarhia.metacom.interfaces.MessageListener;
import com.metarhia.metacom.interfaces.MessageSentCallback;
import com.metarhia.metacom.utils.Constants;
import com.metarhia.metacom.utils.FileUtils;
import com.metarhia.metacom.utils.MainExecutor;

import java.io.InputStream;
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
     * Current downloaded file chunks
     */
    private ArrayList<byte[]> mCurrentFileBuffer;

    /**
     * Current downloaded file extension
     */
    private String mCurrentExtension;

    /**
     * Callback for file downloaded
     */
    private FileDownloadedListener mFileDownloadedListener;

    /**
     * Chat reconnection listener
     */
    private ChatReconnectionListener mChatReconnectionListener;

    /**
     * Creates new chat room by name
     *
     * @param chatRoomName chat name
     */
    ChatRoom(String chatRoomName, AndroidJSTPConnection connection) {
        mChatRoomName = chatRoomName;
        mConnection = connection;
        mMessageListeners = new ArrayList<>();
        mCurrentFileBuffer = new ArrayList<>();

        initIncomingMessagesListener();
        initChatJoinListener();
        initChatLeaveListener();
        initChatTransferListener();
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
     * Sends message to chat room
     *
     * @param sentMessage message to be sent
     * @param callback    callback after message sending (success and error)
     */
    public void sendMessage(final Message sentMessage, final MessageSentCallback callback) {
        List<String> args = new ArrayList<>();
        args.add(sentMessage.getContent());
        mConnection.cacheCall(Constants.META_COM, "send", args, new JSTPOkErrorHandler(MainExecutor.get()) {
            @Override
            public void onOk(List<?> args) {
                callback.onMessageSent(sentMessage);
            }

            @Override
            public void onError(Integer errorCode) {
                callback.onMessageSentError(Errors.getErrorByCode(errorCode));
            }
        });
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
        mConnection.addEventHandler(Constants.META_COM, "message",
                new ExecutableHandler(MainExecutor.get()) {
                    @Override
                    public void run() {
                        List messagePayload = (List) (message).get("message");
                        String messageContent = (String) messagePayload.get(0);

                        Message message = new Message(MessageType.TEXT, messageContent, true);
                        for (MessageListener listener : mMessageListeners) {
                            listener.onMessageReceived(message);
                        }
                    }
                });
    }

    private void initChatJoinListener() {
        mConnection.addEventHandler(Constants.META_COM, "chatJoin",
                new ExecutableHandler(MainExecutor.get()) {
                    @Override
                    public void run() {
                        String infoText = Constants.EVENT_CHAT_JOIN;
                        Message message = new Message(MessageType.INFO, infoText, true);
                        for (MessageListener listener : mMessageListeners) {
                            listener.onMessageReceived(message);
                        }
                    }
                });
    }

    private void initChatLeaveListener() {
        mConnection.addEventHandler(Constants.META_COM, "chatLeave",
                new ExecutableHandler(MainExecutor.get()) {
                    @Override
                    public void run() {
                        String infoText = Constants.EVENT_CHAT_LEAVE;
                        Message message = new Message(MessageType.INFO, infoText, true);
                        for (MessageListener listener : mMessageListeners) {
                            listener.onMessageReceived(message);
                        }
                    }
                });
    }

    private void initChatTransferListener() {
        mConnection.addEventHandler(Constants.META_COM, "chatFileTransferStart",
                new ExecutableHandler(MainExecutor.get()) {

                    @Override
                    public void run() {
                        List messagePayload = (List) (message).get("chatFileTransferStart");
                        String type = (String) messagePayload.get(0);
                        mCurrentExtension = (type == null) ? "txt" :
                                FileUtils.sMimeTypeMap.getExtensionFromMimeType(type);

                        mCurrentFileBuffer = new ArrayList<>();

                    }
                });

        mConnection.addEventHandler(Constants.META_COM, "chatFileTransferChunk",
                new ExecutableHandler(MainExecutor.get()) {
                    @Override
                    public void run() {
                        List messagePayload = (List) (message).get("chatFileTransferChunk");
                        String fileChunk = (String) messagePayload.get(0);
                        if (mCurrentFileBuffer != null)
                            mCurrentFileBuffer.add(Base64.decode(fileChunk, Base64.NO_WRAP));
                    }
                });

        mConnection.addEventHandler(Constants.META_COM, "chatFileTransferEnd",
                new ExecutableHandler(MainExecutor.get()) {
                    @Override
                    public void run() {
                        saveFile();
                    }
                });
    }

    /**
     * Uploads file in chat
     *
     * @param fileStream file to upload
     * @param callback   callback after file upload (success and error)
     */
    public void uploadFile(final InputStream fileStream, String mimeType,
                           final FileUploadedCallback callback) {
        startFileUpload(mimeType, new JSTPOkErrorHandler(MainExecutor.get()) {
            @Override
            public void onOk(List<?> args) {
                FileUtils.uploadSplitFile(fileStream, new FileUtils.FileUploadingInterface() {
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

            @Override
            public void onError(@Array(0) Integer errorCode) {
                callback.onFileUploadError(Errors.getErrorByCode(errorCode));
            }
        });
    }

    /**
     * Starts file upload to server
     *
     * @param mimeType mime type of the sent file
     * @param handler  JSTP handler
     */
    private void startFileUpload(String mimeType, JSTPOkErrorHandler handler) {
        List<String> args = new ArrayList<>();
        args.add(mimeType);
        mConnection.cacheCall(Constants.META_COM, "startChatFileTransfer", args, handler);
    }

    /**
     * Sends chunk to server
     *
     * @param chunk   chunk to send
     * @param handler JSTP handler
     */
    private void sendChunk(byte[] chunk, JSTPOkErrorHandler handler) {
        List<String> args = new ArrayList<>();
        args.add(Base64.encodeToString(chunk, Base64.NO_WRAP));
        mConnection.cacheCall(Constants.META_COM, "sendFileChunkToChat", args, handler);
    }

    /**
     * Ends file upload to server
     *
     * @param callback callback after ending file upload
     */
    private void endFileUpload(final FileUploadedCallback callback) {
        mConnection.cacheCall(Constants.META_COM, "endChatFileTransfer", new ArrayList<>(),
                new JSTPOkErrorHandler(MainExecutor.get()) {
                    @Override
                    public void onOk(List<?> args) {
                        callback.onFileUploaded(null);
                    }

                    @Override
                    public void onError(Integer errorCode) {
                        callback.onFileUploadError(Errors.getErrorByCode(errorCode));
                    }
                });
    }

    /**
     * Saves currently downloaded file to downloads folder
     */
    private void saveFile() {
        FileUtils.saveFileInDownloads(mCurrentExtension, mCurrentFileBuffer,
                new FileDownloadedListener() {
                    @Override
                    public void onFileDownloaded(String path) {
                        mCurrentExtension = null;

                        if (mFileDownloadedListener != null) {
                            mFileDownloadedListener.onFileDownloaded(path);
                        }
                    }

                    @Override
                    public void onFileDownloadError() {
                        if (mFileDownloadedListener != null) {
                            mFileDownloadedListener.onFileDownloadError();
                        }
                    }
                });
    }

    /**
     * Sets listener on file downloaded event
     *
     * @param fileDownloadedListener file downloaded listener
     */
    public void setFileDownloadedListener(FileDownloadedListener fileDownloadedListener) {
        mFileDownloadedListener = fileDownloadedListener;
    }

    void reportRejoinSuccess(boolean hasInterlocutor) {
        if (mChatReconnectionListener != null) {
            mChatReconnectionListener.onRejoinSuccess(hasInterlocutor);
        }
    }

    void reportRejoinError(Integer errorCode) {
        if (mChatReconnectionListener != null) {
            mChatReconnectionListener.onRejoinError(Errors.getErrorByCode(errorCode));
        }
    }

    void reportConnectionLost() {
        if (mChatReconnectionListener != null) {
            mChatReconnectionListener.onConnectionLost();
        }
    }

    /**
     * Gets chat reconnection listener
     *
     * @return chat reconnection listener
     */
    public ChatReconnectionListener getChatReconnectionListener() {
        return mChatReconnectionListener;
    }

    /**
     * Sets chat reconnection listener to specified listener
     *
     * @param chatReconnectionListener chat reconnection listener
     */
    public void setChatReconnectionListener(ChatReconnectionListener chatReconnectionListener) {
        mChatReconnectionListener = chatReconnectionListener;
    }
}
