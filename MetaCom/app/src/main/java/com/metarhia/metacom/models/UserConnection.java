package com.metarhia.metacom.models;

import com.metarhia.metacom.connection.AndroidJSTPConnection;

/**
 * Connection to certain server host and port for chat and file exchange purposes
 *
 * @author lidaamber
 */

public class UserConnection {

    /**
     * Identifier of connection
     */
    private final int mId;

    /**
     * Chats manager
     */
    private final ChatRoomsManager mChatRoomsManager;

    /**
     * Files manager
     */
    private final FilesManager mFilesManager;

    /**
     * Creates new user connection
     */
    UserConnection(int id, AndroidJSTPConnection connection) {
        mId = id;
        mChatRoomsManager = new ChatRoomsManager(connection);
        mFilesManager = new FilesManager(connection);
    }

    /**
     * Gets connection ID
     *
     * @return connection ID
     */
    public int getId() {
        return mId;
    }

    /**
     * Gets connection chats manager
     *
     * @return connection chats manager
     */
    public ChatRoomsManager getChatRoomsManager() {
        return mChatRoomsManager;
    }

    /**
     * Gets connection files manager
     *
     * @return connection files manager
     */
    public FilesManager getFilesManager() {
        return mFilesManager;
    }
}
