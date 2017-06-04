package com.metarhia.metacom.interfaces;

/**
 * Callback after connection establishment attempt
 *
 * @author lidaamber
 */

public interface ConnectionCallback {

    /**
     * Called when connection is successfully established
     *
     * @param connectionID connection id
     */
    void onConnectionEstablished(int connectionID);

    /**
     * Called when server responds error
     */
    void onConnectionError();
}
