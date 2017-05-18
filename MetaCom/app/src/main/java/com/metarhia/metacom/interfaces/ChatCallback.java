package com.metarhia.metacom.interfaces;

/**
 * Callback after chat establishment attempt
 *
 * @author lidaamber
 */

public interface ChatCallback {

    /**
     * Called when chat is established successfully
     */
    void onChatEstablished();

    /**
     * Called when server responds error
     */
    void onChatError();
}
