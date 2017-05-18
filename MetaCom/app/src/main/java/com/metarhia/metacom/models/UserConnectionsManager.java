package com.metarhia.metacom.models;

import com.metarhia.metacom.interfaces.ConnectionCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager for user connections to various server hosts and ports
 *
 * @author lidaamber
 */

public class UserConnectionsManager {

    /**
     * User connections manager instance
     */
    private static UserConnectionsManager instance;

    /**
     * List of user connections
     */
    private List<UserConnection> mUserConnections;

    /**
     * Gets user connection manager instance
     *
     * @return user connection manager instance
     */
    public static UserConnectionsManager get() {
        if (instance == null) {
            instance = new UserConnectionsManager();
        }

        return instance;
    }

    /**
     * Creates new user connections manager
     */
    private UserConnectionsManager() {
        mUserConnections = new ArrayList<>();
    }

    /**
     * Adds new connection with required server host and port
     *
     * @param host     required server host
     * @param port     required server port
     * @param callback callback after attempt to create connection (success and error)
     */
    public void addConnection(String host, int port, ConnectionCallback callback) {
        // TODO add real connection creation

        mUserConnections.add(new UserConnection(mUserConnections.size()));
        callback.onConnectionEstablished();
    }

    /**
     * Gets user connection by ID
     *
     * @param connectionID connection ID
     * @return user connection
     */
    public UserConnection getConnection(int connectionID) {
        if (connectionID >= 0 && connectionID < mUserConnections.size()) {
            return mUserConnections.get(connectionID);
        } else {
            return null;
        }
    }

    /**
     * Removes connection from connection list
     *
     * @param connection connection to be removed
     */
    public void removeConnection(UserConnection connection) {
        mUserConnections.remove(connection);
    }
}
