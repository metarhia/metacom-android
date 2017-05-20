package com.metarhia.metacom.models;

import com.metarhia.metacom.connection.AndroidJSTPConnection;
import com.metarhia.metacom.interfaces.ChatCallback;

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
    public ChatRoomsManager(AndroidJSTPConnection connection) {
        mConnection = connection;
        mChatRooms = new ArrayList<>();
    }

    /**
     * Adds new chat room by name
     *
     * @param roomName room name to be added
     * @param callback callback after attempt to create chat (success and error)
     */
    public void addChatRoom(String roomName, ChatCallback callback) {
        // TODO add chat
        mChatRooms.add(new ChatRoom(roomName, mConnection));
        callback.onChatEstablished();
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
     * Removes chat room from chats list
     *
     * @param chatRoom chatRoom to be removed
     */
    public void removeChatRoom(ChatRoom chatRoom) {
        mChatRooms.remove(chatRoom);
    }

}
