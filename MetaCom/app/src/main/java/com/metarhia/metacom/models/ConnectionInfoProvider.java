package com.metarhia.metacom.models;

import android.content.Context;
import android.support.annotation.Nullable;

import com.metarhia.metacom.utils.FileUtils;

import java.io.File;
import java.util.Map;

/**
 * Simple connection info provider that saves and restores user connections for more comfortable
 * usage
 *
 * @author lidaamber
 */

public class ConnectionInfoProvider {

    private static final String CONNECTION_FILENAME = "connectionInfo";

    public static void saveConnectionInfo(Context context, String host, int port) {
        File file = new File(context.getFilesDir(), CONNECTION_FILENAME);

        FileUtils.saveConnectionInfo(file, host, port);
    }

    @Nullable
    public static Map<String, Integer> restoreConnectionInfo(Context context) {
        File file = new File(context.getFilesDir(), CONNECTION_FILENAME);
        if (!file.exists()) return null;
        else {
            try {
                return FileUtils.readConnectionListFromFile(file);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
