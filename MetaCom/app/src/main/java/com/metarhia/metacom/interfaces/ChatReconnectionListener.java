package com.metarhia.metacom.interfaces;

/**
 * Reconnection interface for chat room
 *
 * @author lidaamber
 */

public interface ChatReconnectionListener {

    /**
     * Emitted when connection gets lost
     */
    void onConnectionLost();

    /**
     * Emitted when rejoin passes successfully
     */
    void onRejoinSuccess(boolean hasInterlocutor);

    /**
     * Emitted when rejoin passes with error
     *
     * @param errorMessage error message
     */
    void onRejoinError(String errorMessage);
}
