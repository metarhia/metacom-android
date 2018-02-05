package com.metarhia.metacom.utils;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.webkit.MimeTypeMap;

import com.metarhia.metacom.connection.Errors;
import com.metarhia.metacom.connection.JSTPOkErrorHandler;
import com.metarhia.metacom.interfaces.FileDownloadedListener;
import com.metarhia.metacom.interfaces.FileUploadedCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utils for files manipulations
 *
 * @author lidaamber
 */

public class FileUtils {

    private static final String FILE_HANDLER_THREAD = "fileHandlerThread";

    public static MimeTypeMap sMimeTypeMap = MimeTypeMap.getSingleton();

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
    private static final int FILE_CHUNK_SIZE = 1024 * 1024;

    /**
     * Uploads file to server
     *
     * @param is            file to upload
     * @param sendInterface send and end sending methods specific for uploading
     * @param callback      callback after file upload (success and error)
     */
    public static void uploadSplitFile(InputStream is, final FileUploadingInterface sendInterface,
                                       final FileUploadedCallback callback) {
        splitFile(is, FILE_CHUNK_SIZE, new FileUtils.FileContentsCallback() {
            @Override
            public void onSplitToChunks(List<byte[]> chunks) {
                final Iterator<byte[]> chunkIterator = chunks.iterator();
                if (chunkIterator.hasNext()) {
                    byte[] chunk = chunkIterator.next();
                    final JSTPOkErrorHandler handler = new JSTPOkErrorHandler(MainExecutor.get()) {
                        @Override
                        public void onOk(List<?> args) {
                            if (chunkIterator.hasNext()) {
                                sendInterface.sendChunk(chunkIterator.next(), this);
                            } else {
                                sendInterface.endFileUpload(callback);
                            }
                        }

                        @Override
                        public void onError(Integer errorCode) {
                            callback.onFileUploadError(Errors.getErrorByCode(errorCode));
                        }
                    };
                    sendInterface.sendChunk(chunk, handler);
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
     * @param fileStream file to split
     * @param chunkSize  size of the chunk
     * @param callback   callback on file split
     */
    private static void splitFile(final InputStream fileStream, final int chunkSize,
                                  final FileContentsCallback callback) {
        sFileHandler.post(new Runnable() {
            @Override
            public void run() {
                List<byte[]> chunks = new ArrayList<>();
                try {
                    int available = fileStream.available();
                    int currentBufferSize = available < chunkSize ? available : chunkSize;
                    byte[] buf = new byte[available < chunkSize ? available : chunkSize];

                    while (fileStream.read(buf, 0, currentBufferSize) != -1) {
                        chunks.add(buf);
                        available -= buf.length;
                        currentBufferSize = available < chunkSize ? available : chunkSize;
                        if (currentBufferSize <= 0) break;
                        buf = new byte[currentBufferSize];
                    }
                    fileStream.close();

                    callback.onSplitToChunks(chunks);
                } catch (Exception e) {
                    callback.onSplitError(e);
                }
            }
        });
    }

    /**
     * Gets downloads storage
     */
    public static void saveFileInDownloads(String extension, ArrayList<byte[]> buffer,
                                           final FileDownloadedListener callback) {
        try {
            File path = (Environment.getExternalStorageState() == null ||
                    !Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) ?
                    Environment.getDataDirectory() :
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            final File file = new File(path, System.currentTimeMillis() + "." + extension);
            if (file.createNewFile()) {

                FileUtils.writeChunksToFile(file, buffer,
                        new FileUtils.FileWritingCallback() {
                            @Override
                            public void onWrittenToFile() {
                                MainExecutor.get().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onFileDownloaded(file.getAbsolutePath());
                                    }
                                });
                            }

                            @Override
                            public void onWriteError(Exception e) {
                                callback.onFileDownloadError();
                            }
                        });
            }

        } catch (IOException e) {
            callback.onFileDownloadError();
        }
    }

    /**
     * Writes file chunks to file
     *
     * @param file   file to be written
     * @param chunks file chunks
     */
    private static void writeChunksToFile(final File file, final ArrayList<byte[]> chunks,
                                          final FileWritingCallback callback) {
        sFileHandler.post(new Runnable() {
            @Override
            public void run() {
                FileOutputStream stream;
                try {
                    stream = new FileOutputStream(file);
                    for (byte[] chunk : chunks) {
                        stream.write(chunk);
                    }
                    stream.flush();
                    stream.close();

                    callback.onWrittenToFile();

                } catch (Exception e) {
                    callback.onWriteError(e);
                }
            }
        });
    }

    public static void saveConnectionInfo(File file, String host, int port) {
        try {
            if (!file.exists()) {
                file.createNewFile();
                Map<String, Integer> infoList = new HashMap<>();
                infoList.put(host, port);

                writeConnectionListToFile(infoList, file);
            } else {
                Map<String, Integer> infoList = readConnectionListFromFile(file);

                if (infoList != null) {
                    infoList.put(host, port);
                }

                writeConnectionListToFile(infoList, file);
            }
        } catch (IOException ignored) {
        } catch (ClassNotFoundException ignored) {
        }
    }

    public static Map<String, Integer> readConnectionListFromFile(File file) throws IOException,
            ClassNotFoundException {
        ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
        Map<String, Integer> infoList = (Map<String, Integer>) is.readObject();
        is.close();

        return infoList;
    }

    private static void writeConnectionListToFile(Map<String, Integer> infoList, File file) throws
            IOException {
        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));

        os.writeObject(infoList);
        os.flush();
        os.close();
    }

    public static void saveMessageHistory(String s, HistoryCallback callback) {
        try {
            String filename = System.currentTimeMillis() + ".txt";

            File path = (Environment.getExternalStorageState() == null ||
                    !Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) ?
                    Environment.getDataDirectory() :
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            final File file = new File(path, filename);
            if (file.mkdirs() && file.createNewFile()) {
                OutputStream os = new FileOutputStream(file);
                os.write(s.getBytes());

                os.flush();
                os.close();
                callback.onHistorySaved(filename);
            }

        } catch (IOException e) {
            callback.onSaveError();
        }
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
     * Callback for writing into file
     */
    private interface FileWritingCallback {

        /**
         * Called when content was written successfully
         */
        void onWrittenToFile();

        /**
         * Called when writing failed
         *
         * @param e exception thrown while writing
         */
        void onWriteError(Exception e);
    }

    /**
     * Interface used to describe file uploading
     */
    public interface FileUploadingInterface {
        void sendChunk(byte[] chunk, JSTPOkErrorHandler handler);

        void endFileUpload(FileUploadedCallback callback);
    }
}
