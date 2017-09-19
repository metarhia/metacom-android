package com.metarhia.metacom.models;

import com.metarhia.metacom.connection.AndroidJSTPConnection;

/**
 * Connection to certain server mHost and mPort for chat and file exchange purposes
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
    private AndroidJSTPConnection mConnection;

    /**
     * Creates new user connection
     */
    UserConnection(int id, AndroidJSTPConnection connection) {
        mId = id;

        mChatRoomsManager = new ChatRoomsManager(connection);
        mFilesManager = new FilesManager(connection);
        mConnection = connection;
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

    public void closeConnection() {
        mConnection.close();
    }
}
