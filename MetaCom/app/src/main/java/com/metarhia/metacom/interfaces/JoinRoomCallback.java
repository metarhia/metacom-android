package com.metarhia.metacom.interfaces;

/**
 * Callback after chat establishment attempt
 *
 * @author lidaamber
 */

public interface JoinRoomCallback {

    /**
     * Called when chat is established successfully
     */
    void onJoinedRoom(boolean hasInterlocutor);

    /**
     * Called when server responds error
     *
     * @param errorMessage error message
     */
    void onJoinError(String errorMessage);
}
