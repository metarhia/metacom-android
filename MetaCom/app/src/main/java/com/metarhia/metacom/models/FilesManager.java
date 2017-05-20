package com.metarhia.metacom.models;

import com.metarhia.metacom.connection.AndroidJSTPConnection;
import com.metarhia.metacom.interfaces.FileDownloadedCallback;
import com.metarhia.metacom.interfaces.FileUploadedCallback;

/**
 * Manager for uploading and downloading files
 *
 * @author lidaamber
 */

public class FilesManager {

    /**
     * MetaCom connection
     */
    private final AndroidJSTPConnection mConnection;

    /**
     * Creates new files manager
     */
    public FilesManager(AndroidJSTPConnection connection) {
        mConnection = connection;
    }

    /**
     * Uploads file to server specified in UserConnection
     *
     * @param fileBytes file represented as a byte array
     * @param callback  callback after file upload (success and error)
     */
    public void uploadFile(byte[] fileBytes, FileUploadedCallback callback) {
        // TODO upload file when API is available

        callback.onFileUploaded();
    }

    /**
     * Downloads file from server specified in UserConnection
     *
     * @param fileCode code of file to download
     * @param callback callback after file download (success and error)
     */
    public void downloadFile(String fileCode, FileDownloadedCallback callback) {
        // TODO download file when API is available

        callback.onFileDownloaded();
    }
}
