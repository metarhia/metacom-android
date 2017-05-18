package com.metarhia.metacom.interfaces;

/**
 * Callback after connection establishment attempt
 *
 * @author lidaamber
 */

public interface ConnectionCallback {

    /**
     * Called when connection is successfully established
     */
    void onConnectionEstablished();

    /**
     * Called when server responds error
     */
    void onConnectionError();
}
