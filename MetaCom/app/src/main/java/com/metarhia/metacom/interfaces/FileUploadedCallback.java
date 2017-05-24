package com.metarhia.metacom.interfaces;

/**
 * Callback after file uploading
 *
 * @author lidaamber
 */

public interface FileUploadedCallback {

    /**
     * Called when file is uploaded successfully
     *
     * @param fileCode code of uploaded file
     */
    void onFileUploaded(String fileCode);

    /**
     * Called when server responds error
     *
     * @param message error message
     */
    void onFileUploadError(String message);
}
