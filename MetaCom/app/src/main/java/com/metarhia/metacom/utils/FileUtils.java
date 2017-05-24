package com.metarhia.metacom.utils;

import android.os.Handler;
import android.os.HandlerThread;

import com.metarhia.jstp.core.JSTypes.JSArray;
import com.metarhia.metacom.connection.Errors;
import com.metarhia.metacom.connection.JSTPOkErrorHandler;
import com.metarhia.metacom.interfaces.FileUploadedCallback;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utils for files manipulations
 *
 * @author lidaamber
 */

public class FileUtils {

    private static final String FILE_HANDLER_THREAD = "fileHandlerThread";

    static {
        HandlerThread fileHandlerThread = new HandlerThread(FILE_HANDLER_THREAD);
        fileHandlerThread.start();
        sFileHandler = new Handler(fileHandlerThread.getLooper());
    }

    /**
     * Handler processing files manipulations
     */
    private static Handler sFileHandler;

    /**
     * Size of chunk to split file
     */
    private static final int FILE_CHUNK_SIZE = 4 * 1024 * 1024;

    /**
     * Uploads file to server
     *
     * @param file             file to upload
     * @param sendingInterface send and end sending methods specific for uploading
     * @param callback         callback after file upload (success and error)
     */
    public static void uploadSplitFile(File file, final FileUploadingInterface sendingInterface,
                                       final FileUploadedCallback callback) {
        splitFile(file, FILE_CHUNK_SIZE, new FileUtils.FileContentsCallback() {
            @Override
            public void onSplitToChunks(List<byte[]> chunks) {
                final Iterator<byte[]> chunkIterator = chunks.iterator();
                if (chunkIterator.hasNext()) {
                    byte[] chunk = chunkIterator.next();
                    final JSTPOkErrorHandler handler = new JSTPOkErrorHandler() {
                        @Override
                        public void onOk(JSArray args) {
                            if (chunkIterator.hasNext()) {
                                sendingInterface.sendChunk(chunkIterator.next(), this);
                            } else {
                                sendingInterface.endFileUpload(callback);
                            }
                        }

                        @Override
                        public void onError(Integer errorCode) {
                            callback.onFileUploadError(Errors.getErrorByCode(errorCode));
                        }
                    };
                    sendingInterface.sendChunk(chunk, handler);
                }
            }

            @Override
            public void onSplitError(Exception e) {
                callback.onFileUploadError(Errors.getErrorByCode(Errors.ERR_FILE_LOAD));
            }
        });
    }

    /**
     * Splits file into byte[] chunks
     *
     * @param file      file to split
     * @param chunkSize size of the chunk
     * @param callback  callback on file split
     */
    private static void splitFile(final File file, final int chunkSize,
                                  final FileContentsCallback callback) {
        sFileHandler.post(new Runnable() {
            @Override
            public void run() {
                List<byte[]> chunks = new ArrayList<>();
                InputStream in;
                try {
                    in = new BufferedInputStream(new FileInputStream(file));
                    final byte[] buf = new byte[chunkSize];
                    while (in.read(buf) != -1) {
                        chunks.add(buf);
                    }
                    in.close();

                    callback.onSplitToChunks(chunks);
                } catch (FileNotFoundException e) {
                    callback.onSplitError(e);
                } catch (IOException e) {
                    callback.onSplitError(e);
                }
            }
        });
    }

    /**
     * Callback for splitting files
     */
    private interface FileContentsCallback {
        /**
         * Called when file is split successfully
         *
         * @param chunks file chunks
         */
        void onSplitToChunks(List<byte[]> chunks);

        /**
         * Called when splitting fails
         *
         * @param e exception thrown while splitting
         */
        void onSplitError(Exception e);
    }

    /**
     * Interface used to describe file uploading
     */
    public interface FileUploadingInterface {
        void sendChunk(byte[] chunk, JSTPOkErrorHandler handler);

        void endFileUpload(FileUploadedCallback callback);
    }
}
