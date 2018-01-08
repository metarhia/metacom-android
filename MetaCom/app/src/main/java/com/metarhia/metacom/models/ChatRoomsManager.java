package com.metarhia.metacom.models;

import com.metarhia.jstp.compiler.annotations.handlers.Array;
import com.metarhia.metacom.connection.AndroidJSTPConnection;
import com.metarhia.metacom.connection.Errors;
import com.metarhia.metacom.connection.JSTPOkErrorHandler;
import com.metarhia.metacom.interfaces.JoinRoomCallback;
import com.metarhia.metacom.interfaces.LeaveRoomCallback;
import com.metarhia.metacom.utils.Constants;
import com.metarhia.metacom.utils.FileUtils;
import com.metarhia.metacom.utils.HistoryCallback;
import com.metarhia.metacom.utils.MainExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager for user connection chats
 *
 * @author lidaamber
 */

public class ChatRoomsManager implements AndroidJSTPConnection.AndroidJSTPConnectionListener {

    /**
     * List of user chat rooms
     */
    private final List<ChatRoom> mChatRooms;

    /**
     * MetaCom connection
     */
    private final AndroidJSTPConnection mConnection;

    /**
     * Creates new chat rooms manager
     */
    ChatRoomsManager(AndroidJSTPConnection connection) {
        mConnection = connection;
        mChatRooms = new ArrayList<>();

        mConnection.addListener(this);
    }

    /**
     * Adds new chat room by name
     *
     * @param roomName room name to be added
     * @param callback callback after attempt to create chat (success and error)
     */
    public void addChatRoom(final String roomName, final JoinRoomCallback callback) {
        joinRoom(roomName, new JSTPOkErrorHandler(MainExecutor.get()) {
            @Override
            public void onOk(List args) {

                Boolean hasInterlocutor = (Boolean) args.get(0);

                ChatRoom room = new ChatRoom(roomName, mConnection);
                room.setHasInterlocutor(hasInterlocutor);

                mChatRooms.add(room);
                callback.onJoinedRoom(hasInterlocutor);
            }

            @Override
            public void onError(Integer errorCode) {
                callback.onJoinError(Errors.getErrorByCode(errorCode));
            }
        });

    }

    private void joinRoom(String roomName, JSTPOkErrorHandler handler) {
        List<String> args = new ArrayList<>();
        args.add(roomName);
        mConnection.cacheCall(Constants.META_COM, "join", args, handler);
    }

    /**
     * Gets chat room by name
     *
     * @param roomName chat name
     * @return required chat
     */
    public ChatRoom getChatRoom(String roomName) {
        for (ChatRoom chatRoom : mChatRooms) {
            if (chatRoom.getChatRoomName().equals(roomName)) {
                return chatRoom;
            }
        }
        return null;
    }

    /**
     * Leaves chat room and removes char room from chats list
     *
     * @param chatRoom chatRoom to be removed
     */
    public void leaveChatRoom(final ChatRoom chatRoom, final LeaveRoomCallback callback) {
        mConnection.cacheCall(Constants.META_COM, "leave", new ArrayList<>(),
                new JSTPOkErrorHandler(MainExecutor.get()) {
                    @Override
                    public void onOk(List<?> args) {
                        chatRoom.removeAllHandlers();
                        mChatRooms.remove(chatRoom);
                        callback.onLeavedRoom();
                    }

                    @Override
                    public void onError(Integer errorCode) {
                        callback.onLeaveError(Errors.getErrorByCode(errorCode));
                    }
                });
    }

    @Override
    public void onConnectionEstablished(AndroidJSTPConnection connection) {
        if (mChatRooms.isEmpty()) return;
        for (final ChatRoom room : mChatRooms) {
            joinRoom(room.getChatRoomName(), new JSTPOkErrorHandler(MainExecutor.get()) {
                @Override
                public void onOk(List<?> args) {
                    Boolean hasInterlocutor = (Boolean) args.get(0);
                    room.reportRejoinSuccess(hasInterlocutor);
                }

                @Override
                public void onError(@Array(0) Integer errorCode) {
                    room.reportRejoinError(errorCode);
                }
            });
        }

    }

    @Override
    public void onConnectionLost() {
        MainExecutor.get().execute(new Runnable() {
            @Override
            public void run() {
                if (mChatRooms.isEmpty()) return;

                for (ChatRoom room : mChatRooms) {
                    room.reportConnectionLost();
                }
            }
        });
    }

    public void saveHistory(List<Message> messages, HistoryCallback callback) {
        StringBuilder messageBuilder = new StringBuilder();
        for (Message message : messages) {
            if (message.getType() == MessageType.INFO) {
                messageBuilder.append(message.getContent() + "\n");
                continue;
            }

            String sender = message.isIncoming() ? "Interlocutor: " : "Me: ";
            messageBuilder.append(sender);
            if (message.getType() == MessageType.FILE) {
                messageBuilder.append("File");
            } else {
                messageBuilder.append(message.getContent());
            }
            messageBuilder.append("\n");
        }

        FileUtils.saveMessageHistory(messageBuilder.toString(), callback);
    }
}
