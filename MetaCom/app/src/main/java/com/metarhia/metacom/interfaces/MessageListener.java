package com.metarhia.metacom.interfaces;

import com.metarhia.metacom.models.Message;

/**
 * Incoming messages listener
 *
 * @author lidaamber
 */

public interface MessageListener {

    /**
     * Called when message is received
     *
     * @param message message received from server
     */
    void onMessageReceived(Message message);
}
