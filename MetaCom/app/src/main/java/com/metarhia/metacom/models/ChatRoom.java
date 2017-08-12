package com.metarhia.metacom.models;

import android.util.Base64;

import com.metarhia.jstp.compiler.annotations.handlers.Array;
import com.metarhia.jstp.core.Handlers.ManualHandler;
import com.metarhia.jstp.core.JSInterfaces.JSObject;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
     * Message listener for incoming chat room messages
     */

    private MessageListener mMessageListener;

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
     * Has interlocutor state
     */
    private boolean mHasInterlocutor;

    /**
     * File upload queue
     */
    private Queue<FileUploadData> mFileQueue;

    private ManualHandler mStartHandler;
    private ManualHandler mChunkHandler;
    private ExecutableHandler mEndHandler;
    private ExecutableHandler mChatJoinHandler;
    private ExecutableHandler mChatLeaveHandler;
    private ExecutableHandler mMessageHandler;

    /**
     * Creates new chat room by name
     *
     * @param chatRoomName chat name
     */
    ChatRoom(String chatRoomName, AndroidJSTPConnection connection) {
        mChatRoomName = chatRoomName;
        mConnection = connection;
        mCurrentFileBuffer = new ArrayList<>();
        mFileQueue = new LinkedList<>();

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
    String getChatRoomName() {
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
    public void setMessageListener(MessageListener listener) {
        mMessageListener = listener;
    }

    /**
     * Adds message event handler to JSTP connection
     */
    private void initIncomingMessagesListener() {
        mMessageHandler = new ExecutableHandler(MainExecutor.get()) {
            @Override
            public void run() {
                List messagePayload = (List) (message).get("message");
                String messageContent = (String) messagePayload.get(0);

                Message message = new Message(MessageType.TEXT, messageContent, true);
                if (mMessageListener != null) {
                    mMessageListener.onMessageReceived(message);
                }
            }
        };
        mConnection.addEventHandler(Constants.META_COM, "message", mMessageHandler);
    }

    private void initChatJoinListener() {
        mChatJoinHandler = new ExecutableHandler(MainExecutor.get()) {
            @Override
            public void run() {
                mHasInterlocutor = true;

                String infoText = Constants.EVENT_CHAT_JOIN;
                Message message = new Message(MessageType.INFO, infoText, true);
                if (mMessageListener != null) {
                    mMessageListener.onMessageReceived(message);
                }
            }
        };

        mConnection.addEventHandler(Constants.META_COM, "chatJoin", mChatJoinHandler);
    }

    private void initChatLeaveListener() {
        mFileQueue.clear();
        mChatLeaveHandler = new ExecutableHandler(MainExecutor.get()) {
            @Override
            public void run() {
                mHasInterlocutor = false;

                String infoText = Constants.EVENT_CHAT_LEAVE;
                Message message = new Message(MessageType.INFO, infoText, true);
                if (mMessageListener != null) {
                    mMessageListener.onMessageReceived(message);
                }
            }
        };
        mConnection.addEventHandler(Constants.META_COM, "chatLeave", mChatLeaveHandler);
    }

    private void initChatTransferListener() {
        mStartHandler = new ManualHandler() {
            @Override
            public void handle(JSObject jsObject) {
                List messagePayload = (List) (jsObject).get("chatFileTransferStart");
                String type = (String) messagePayload.get(0);
                mCurrentExtension = (type == null) ? "txt" :
                        FileUtils.sMimeTypeMap.getExtensionFromMimeType(type);

                mCurrentFileBuffer = new ArrayList<>();
            }
        };

        mChunkHandler = new ManualHandler() {
            @Override
            public void handle(JSObject jsObject) {
                List messagePayload = (List) (jsObject).get("chatFileTransferChunk");
                String fileChunk = (String) messagePayload.get(0);
                if (mCurrentFileBuffer != null)
                    mCurrentFileBuffer.add(Base64.decode(fileChunk, Base64.NO_WRAP));
            }
        };

        mEndHandler = new ExecutableHandler(MainExecutor.get()) {
            @Override
            public void run() {
                saveFile();
            }
        };

        mConnection.addEventHandler(Constants.META_COM, "chatFileTransferStart", mStartHandler);
        mConnection.addEventHandler(Constants.META_COM, "chatFileTransferChunk", mChunkHandler);
        mConnection.addEventHandler(Constants.META_COM, "chatFileTransferEnd", mEndHandler);
    }

    void removeAllHandlers() {
        mFileQueue.clear();

        mConnection.removeEventHandler(Constants.META_COM, "chatFileTransferStart", mStartHandler);
        mStartHandler = null;
        mConnection.removeEventHandler(Constants.META_COM, "chatFileTransferChunk", mChunkHandler);
        mChunkHandler = null;
        mConnection.removeEventHandler(Constants.META_COM, "chatFileTransferEnd", mEndHandler);
        mEndHandler = null;
        mConnection.removeEventHandler(Constants.META_COM, "chatJoin", mChatJoinHandler);
        mChatJoinHandler = null;
        mConnection.removeEventHandler(Constants.META_COM, "chatLeave", mChatLeaveHandler);
        mChatLeaveHandler = null;
        mConnection.removeEventHandler(Constants.META_COM, "message", mMessageHandler);
        mMessageHandler = null;
    }

    /**
     * Uploads file in chat
     *
     * @param fileStream file to upload
     * @param callback   callback after file upload (success and error)
     */
    public void uploadFile(final InputStream fileStream, String mimeType,
                           final FileUploadedCallback callback) {
        if (!mFileQueue.isEmpty()) {
            mFileQueue.add(new FileUploadData(fileStream, mimeType, callback));
            return;
        }

        mFileQueue.add(new FileUploadData(fileStream, mimeType, callback));
        uploadNextFromQueue();

    }

    private void uploadNextFromQueue() {
        if (mFileQueue.isEmpty()) return;
        final FileUploadData data = mFileQueue.peek();
        startFileUpload(data.mimeType, new JSTPOkErrorHandler(MainExecutor.get()) {
            @Override
            public void onOk(List<?> args) {
                FileUtils.uploadSplitFile(data.fileStream,
                        new FileUtils.FileUploadingInterface() {
                            @Override
                            public void sendChunk(byte[] chunk, JSTPOkErrorHandler handler) {
                                ChatRoom.this.sendChunk(chunk, handler);
                            }

                            @Override
                            public void endFileUpload(FileUploadedCallback callback) {
                                ChatRoom.this.endFileUpload(callback);

                            }
                        }, data.callback);
            }

            @Override
            public void onError(@Array(0) Integer errorCode) {
                data.callback.onFileUploadError(Errors.getErrorByCode(errorCode));
                mFileQueue.remove();
                uploadNextFromQueue();
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
                        mFileQueue.remove();
                        uploadNextFromQueue();
                    }

                    @Override
                    public void onError(Integer errorCode) {
                        callback.onFileUploadError(Errors.getErrorByCode(errorCode));
                        mFileQueue.remove();
                        uploadNextFromQueue();
                    }
                });
    }

    /**
     * Saves currently downloaded file to downloads folder
     */
    private void saveFile() {
        FileUtils.saveFileInDownloads(String.valueOf(mCurrentExtension),
                new ArrayList<>(mCurrentFileBuffer),
                new FileDownloadedListener() {
                    @Override
                    public void onFileDownloaded(String path) {
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
        mHasInterlocutor = hasInterlocutor;
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
        mFileQueue.clear();
        if (mChatReconnectionListener != null) {
            mChatReconnectionListener.onConnectionLost();
        }
    }

    /**
     * Sets chat reconnection listener to specified listener
     *
     * @param chatReconnectionListener chat reconnection listener
     */
    public void setChatReconnectionListener(ChatReconnectionListener chatReconnectionListener) {
        mChatReconnectionListener = chatReconnectionListener;
    }

    /**
     * Checks if there is any interlocutor in chat
     *
     * @return has interlocutor
     */
    public boolean hasInterlocutor() {
        return mHasInterlocutor;
    }

    /**
     * Sets if there is any interlocutor in chat
     *
     * @param hasInterlocutor has interlocutor
     */
    void setHasInterlocutor(boolean hasInterlocutor) {
        this.mHasInterlocutor = hasInterlocutor;
    }

    private static class FileUploadData {
        final InputStream fileStream;
        final String mimeType;
        final FileUploadedCallback callback;

        FileUploadData(InputStream is, String mimeType, FileUploadedCallback cb) {
            this.fileStream = is;
            this.mimeType = mimeType;
            this.callback = cb;
        }
    }
}
