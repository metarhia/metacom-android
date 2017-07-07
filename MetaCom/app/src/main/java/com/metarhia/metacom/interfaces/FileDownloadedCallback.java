package com.metarhia.metacom.interfaces;

/**
 * Callback after file downloading
 *
 * @author lidaamber
 */

public interface FileDownloadedCallback {

    /**
     * Called when file is downloaded successfully
     *
     * @param path absolute path to file
     */
    void onFileDownloaded(String path);

    /**
     * Called when server responds error
     */
    void onFileDownloadError();
}
