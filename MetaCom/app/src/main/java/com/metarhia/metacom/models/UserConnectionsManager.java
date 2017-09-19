package com.metarhia.metacom.models;

import android.content.Context;

import com.metarhia.metacom.connection.AndroidJSTPConnection;
import com.metarhia.metacom.interfaces.ConnectionCallback;
import com.metarhia.metacom.utils.Constants;
import com.metarhia.metacom.utils.MainExecutor;

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
    private final List<UserConnection> mUserConnections;

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
     * @param context application context
     * @param host    required server host
     * @param port    required server port
     * @param cb      callback after attempt to create connection (success and error)
     */
    public void addConnection(Context context, String host, int port, final ConnectionCallback cb) {
        final AndroidJSTPConnection connection =
                new AndroidJSTPConnection(host, port, true, context);
        connection.addListener(new AndroidJSTPConnection.AndroidJSTPConnectionListener() {
            @Override
            public void onConnectionEstablished(final AndroidJSTPConnection connection) {
                final AndroidJSTPConnection.AndroidJSTPConnectionListener listener = this;
                MainExecutor.get().execute(new Runnable() {
                    @Override
                    public void run() {
                        UserConnection uc = new UserConnection(mUserConnections.size(), connection);
                        mUserConnections.add(uc);
                        cb.onConnectionEstablished(uc.getId());
                        connection.removeListener(listener);
                    }
                });
            }

            @Override
            public void onConnectionLost() {
                final AndroidJSTPConnection.AndroidJSTPConnectionListener listener = this;
                MainExecutor.get().execute(new Runnable() {
                    @Override
                    public void run() {
                        cb.onConnectionError();
                        connection.removeListener(listener);
                    }
                });
            }
        });
        connection.openConnection(Constants.APPLICATION_NAME);
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
        connection.closeConnection();
    }
}
