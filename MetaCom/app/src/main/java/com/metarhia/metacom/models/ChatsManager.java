package com.metarhia.metacom.models;

import com.metarhia.metacom.interfaces.ChatCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager for user connection chats
 *
 * @author lidaamber
 */

public class ChatsManager {

    /**
     * List of user chats
     */
    private List<Chat> mChats;

    /**
     * Creates new chat manager
     */
    public ChatsManager() {
        mChats = new ArrayList<>();
    }

    /**
     * Adds new chat by name
     *
     * @param chatName chat name to be added
     * @param callback callback after attempt to create chat (success and error)
     */
    public void addChat(String chatName, ChatCallback callback) {
        // TODO add chat
        mChats.add(new Chat(chatName));
        callback.onChatEstablished();
    }

    /**
     * Gets chat by name
     *
     * @param chatName chat name
     * @return required chat
     */
    public Chat getChat(String chatName) {
        for (Chat chat : mChats) {
            if (chat.getChatName().equals(chatName)) {
                return chat;
            }
        }
        return null;
    }

    /**
     * Removes chat from chats list
     *
     * @param chat chat to be removed
     */
    public void removeChat(Chat chat) {
        mChats.remove(chat);
    }

}
