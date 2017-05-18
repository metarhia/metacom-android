package com.metarhia.metacom.interfaces;

/**
 * Callback after file downloading
 *
 * @author lidaamber
 */

public interface FileDownloadedCallback {

    /**
     * Called when file is downloaded successfully
     */
    void onFileDownloaded();

    /**
     * Called when server responds error
     */
    void onFileDownloadError();
}
