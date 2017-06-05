package com.metarhia.metacom.models;

import android.content.Context;
import android.support.annotation.Nullable;

import com.metarhia.metacom.connection.AndroidJSTPConnection;
import com.metarhia.metacom.interfaces.ConnectionCallback;
import com.metarhia.metacom.utils.Constants;

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
     * Context used for connections
     */
    private final Context mContext;

    /**
     * Gets user connection manager instance, if null, creates it
     *
     * @param context application context
     * @return user connection manager instance
     */
    public static UserConnectionsManager get(Context context) {
        if (instance == null) {
            instance = new UserConnectionsManager(context);
        }

        return instance;
    }

    /**
     * Gets user connection manager instance
     *
     * @return user connection manager instance
     */
    @Nullable
    public static UserConnectionsManager get() {
        return instance;
    }

    /**
     * Creates new user connections manager
     */
    private UserConnectionsManager(Context context) {
        mContext = context;
        mUserConnections = new ArrayList<>();
    }

    /**
     * Adds new connection with required server host and port
     *
     * @param host     required server host
     * @param port     required server port
     * @param callback callback after attempt to create connection (success and error)
     */
    public void addConnection(String host, int port, final ConnectionCallback callback) {
        AndroidJSTPConnection connection = new AndroidJSTPConnection(host, port, true, mContext);
        connection.addListener(new AndroidJSTPConnection.AndroidJSTPConnectionListener() {
            @Override
            public void onConnectionEstablished(AndroidJSTPConnection connection) {
                UserConnection uc = new UserConnection(mUserConnections.size(), connection);
                mUserConnections.add(uc);
                callback.onConnectionEstablished(uc.getId());
                connection.removeListener(this);
            }

            @Override
            public void onConnectionLost() {
                callback.onConnectionError();
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
    }
}
