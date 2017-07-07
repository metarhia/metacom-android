package com.metarhia.metacom.models;

import android.util.Base64;

import com.metarhia.jstp.core.Handlers.ManualHandler;
import com.metarhia.jstp.core.JSInterfaces.JSObject;
import com.metarhia.metacom.connection.AndroidJSTPConnection;
import com.metarhia.metacom.connection.Errors;
import com.metarhia.metacom.connection.JSTPOkErrorHandler;
import com.metarhia.metacom.interfaces.FileDownloadedCallback;
import com.metarhia.metacom.interfaces.FileUploadedCallback;
import com.metarhia.metacom.utils.Constants;
import com.metarhia.metacom.utils.FileUtils;
import com.metarhia.metacom.utils.MainExecutor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
     * @param fileStream file to upload
     * @param callback   callback after file upload (success and error)
     */
    public void uploadFile(InputStream fileStream, final FileUploadedCallback callback) {
        FileUtils.uploadSplitFile(fileStream, new FileUtils.FileUploadingInterface() {
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

    }

    private void addSendChunkEventHandler() {
        mConnection.addEventHandler(Constants.META_COM, "downloadFileChunk", new ManualHandler() {
            @Override
            public void handle(JSObject jsValue) {
                List args = (List) (jsValue).get("downloadFileChunk");
                String chunk = (String) args.get(0);
            }
        });
    }

    /**
     * Sends chunk to server
     *
     * @param chunk   chunk to send
     * @param handler JSTP handler
     */
    private void sendChunk(byte[] chunk, JSTPOkErrorHandler handler) {
        List<String> args = new ArrayList<>();
        args.add(Base64.encodeToString(chunk, Base64.DEFAULT));

        mConnection.cacheCall(Constants.META_COM, "uploadFileChunk", args, handler);
    }

    /**
     * Ends file upload to server
     *
     * @param callback callback after ending file upload
     */
    private void endFileUpload(final FileUploadedCallback callback) {
        mConnection.cacheCall(Constants.META_COM, "endFileUpload", new ArrayList<>(),
                new JSTPOkErrorHandler(MainExecutor.get()) {
                    @Override
                    public void onOk(List<?> args) {
                        String fileCode = (String) args.get(0);
                        callback.onFileUploaded(fileCode);
                    }

                    @Override
                    public void onError(Integer errorCode) {
                        callback.onFileUploadError(Errors.getErrorByCode(errorCode));
                    }
                });
    }
}
