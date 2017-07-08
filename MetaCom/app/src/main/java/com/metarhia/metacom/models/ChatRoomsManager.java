package com.metarhia.metacom.models;

import com.metarhia.metacom.connection.AndroidJSTPConnection;
import com.metarhia.metacom.connection.Errors;
import com.metarhia.metacom.connection.JSTPOkErrorHandler;
import com.metarhia.metacom.interfaces.JoinRoomCallback;
import com.metarhia.metacom.interfaces.LeaveRoomCallback;
import com.metarhia.metacom.utils.Constants;
import com.metarhia.metacom.utils.MainExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager for user connection chats
 *
 * @author lidaamber
 */

public class ChatRoomsManager {

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
    }

    /**
     * Adds new chat room by name
     *
     * @param roomName room name to be added
     * @param callback callback after attempt to create chat (success and error)
     */
    public void addChatRoom(final String roomName, final JoinRoomCallback callback) {
        List<String> args = new ArrayList<>();
        args.add(roomName);

        mConnection.cacheCall(Constants.META_COM, "join", args,
                new JSTPOkErrorHandler(MainExecutor.get()) {
                    @Override
                    public void onOk(List args) {
                        ChatRoom room = new ChatRoom(roomName, mConnection);
                        mChatRooms.add(room);
                        callback.onJoinedRoom();
                    }

                    @Override
                    public void onError(Integer errorCode) {
                        callback.onJoinError(Errors.getErrorByCode(errorCode));
                    }
                });

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
                        mChatRooms.remove(chatRoom);
                        callback.onLeavedRoom();
                    }

                    @Override
                    public void onError(Integer errorCode) {
                        callback.onLeaveError(Errors.getErrorByCode(errorCode));
                    }
                });
    }

}
