package com.metarhia.metacom.interfaces;

import com.metarhia.metacom.models.Message;

/**
 * Callback after message sending
 *
 * @author lidaamber
 */

public interface MessageSentCallback {

    /**
     * Called when message is sent successfully
     *
     * @param message message sent by user
     */
    void onMessageSent(Message message);


    /**
     * Called when server responds error
     *
     * @param message error message
     */
    void onMessageSentError(String message);
}
