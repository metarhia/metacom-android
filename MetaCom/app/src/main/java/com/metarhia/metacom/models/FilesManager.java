package com.metarhia.metacom.models;

import android.util.Base64;

import com.metarhia.jstp.core.JSTypes.JSArray;
import com.metarhia.metacom.connection.AndroidJSTPConnection;
import com.metarhia.metacom.connection.Errors;
import com.metarhia.metacom.connection.JSTPOkErrorHandler;
import com.metarhia.metacom.interfaces.FileDownloadedCallback;
import com.metarhia.metacom.interfaces.FileUploadedCallback;
import com.metarhia.metacom.utils.Constants;
import com.metarhia.metacom.utils.FileUtils;

import java.io.File;

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
     * @param file     file to upload
     * @param callback callback after file upload (success and error)
     */
    public void uploadFile(File file, final FileUploadedCallback callback) {
        FileUtils.uploadSplitFile(file, new FileUtils.FileUploadingInterface() {
            @Override
            public void sendChunk(byte[] chunk, JSTPOkErrorHandler handler) {
                FilesManager.this.sendChunk(chunk, handler);
            }

            @Override
            public void endFileUpload(FileUploadedCallback callback) {
                FilesManager.this.endFileUpload(callback);
            }
        }, callback);
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

    /**
     * Sends chunk to server
     *
     * @param chunk   chunk to send
     * @param handler JSTP handler
     */
    private void sendChunk(byte[] chunk, JSTPOkErrorHandler handler) {
        JSArray args = new JSArray();
        args.add(Base64.encodeToString(chunk, Base64.DEFAULT));

        mConnection.cacheCall(Constants.META_COM, "uploadFileChunk", args, handler);
    }

    /**
     * Ends file upload to server
     *
     * @param callback callback after ending file upload
     */
    private void endFileUpload(final FileUploadedCallback callback) {
        mConnection.cacheCall(Constants.META_COM, "endFileUpload", new JSArray(),
                new JSTPOkErrorHandler() {
                    @Override
                    public void onOk(JSArray args) {
                        String fileCode = (String) args.get(0).getGeneralizedValue();
                        callback.onFileUploaded(fileCode);
                    }

                    @Override
                    public void onError(Integer errorCode) {
                        callback.onFileUploadError(Errors.getErrorByCode(errorCode));
                    }
                });
    }
}
