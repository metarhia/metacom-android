package com.metarhia.metacom.interfaces;

/**
 * Callback after file uploading
 *
 * @author lidaamber
 */

public interface FileUploadedCallback {

    /**
     * Called when file is uploaded successfully
     */
    void onFileUploaded();

    /**
     * Called when server responds error
     */
    void onFileUploadError();
}
