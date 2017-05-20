package com.metarhia.metacom.interfaces;

/**
 * Callback after message sending
 *
 * @author lidaamber
 */

public interface MessageSentCallback {

    /**
     * Called when message is sent successfully
     */
    void onMessageSent();


    /**
     * Called when server responds error
     *
     * @param message error message
     */
    void onMessageSentError(String message);
}
