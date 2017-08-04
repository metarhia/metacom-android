package com.metarhia.metacom.models;

import android.util.Base64;

import com.metarhia.jstp.compiler.annotations.handlers.Array;
import com.metarhia.jstp.core.Handlers.ManualHandler;
import com.metarhia.jstp.core.JSInterfaces.JSObject;
import com.metarhia.jstp.handlers.ExecutableHandler;
import com.metarhia.metacom.connection.AndroidJSTPConnection;
import com.metarhia.metacom.connection.Errors;
import com.metarhia.metacom.connection.JSTPOkErrorHandler;
import com.metarhia.metacom.interfaces.FileDownloadedListener;
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
     * Current downloaded file chunks
     */
    private ArrayList<byte[]> mCurrentFileBuffer;

    /**
     * Current downloaded file extension
     */
    private String mCurrentExtension;

    /**
     * Current downloading file callback
     */
    private FileDownloadedListener mCurrentCallback;

    /**
     * Creates new files manager
     */
    FilesManager(AndroidJSTPConnection connection) {
        mConnection = connection;

        initTransferListener();
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
    public void downloadFile(String fileCode, final FileDownloadedListener callback) {
        List<String> args = new ArrayList<>();
        args.add(fileCode);
        mConnection.cacheCall(Constants.META_COM, "downloadFile", args,
                new JSTPOkErrorHandler(MainExecutor.get()) {
                    @Override
                    public void onOk(List<?> args) {
                        mCurrentCallback = callback;
                    }

                    @Override
                    public void onError(@Array(0) Integer errorCode) {
                        callback.onFileDownloadError();
                    }
                });
    }

    private void initTransferListener() {
        mConnection.addEventHandler(Constants.META_COM, "downloadFileStart",
                new ManualHandler() {
                    @Override
                    public void handle(JSObject jsObject) {
                        List messagePayload = (List) (jsObject).get("downloadFileStart");
                        String type = (String) messagePayload.get(0);
                        mCurrentExtension = (type == null) ? "txt" :
                                FileUtils.sMimeTypeMap.getExtensionFromMimeType(type);

                        mCurrentFileBuffer = new ArrayList<>();
                    }
                });

        mConnection.addEventHandler(Constants.META_COM, "downloadFileChunk",
                new ManualHandler() {
                    @Override
                    public void handle(JSObject jsObject) {
                        List messagePayload = (List) (jsObject).get("downloadFileChunk");
                        String fileChunk = (String) messagePayload.get(0);
                        if (mCurrentFileBuffer != null)
                            mCurrentFileBuffer.add(Base64.decode(fileChunk, Base64.NO_WRAP));
                    }
                });

        mConnection.addEventHandler(Constants.META_COM, "downloadFileEnd",
                new ExecutableHandler(MainExecutor.get()) {
                    @Override
                    public void run() {
                        FileUtils.saveFileInDownloads(mCurrentExtension, mCurrentFileBuffer,
                                mCurrentCallback);
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
        args.add(Base64.encodeToString(chunk, Base64.NO_WRAP));

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
