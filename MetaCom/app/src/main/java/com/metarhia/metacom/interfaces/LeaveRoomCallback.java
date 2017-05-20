package com.metarhia.metacom.interfaces;

/**
 * Callback after leaving chat room
 *
 * @author lidaamber
 */

public interface LeaveRoomCallback {

    /**
     * Called when user successfully left chat room
     */
    void onLeavedRoom();

    /**
     * Called when server responds error
     *
     * @param errorMessage error message
     */
    void onLeaveError(String errorMessage);
}
