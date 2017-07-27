package com.metarhia.metacom.interfaces;

/**
 * Listener for file downloaded event
 *
 * @author lidaamber
 */

public interface FileDownloadedListener {

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
