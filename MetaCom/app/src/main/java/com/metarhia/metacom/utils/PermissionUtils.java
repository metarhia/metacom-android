package com.metarhia.metacom.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

/**
 * @author MariaKokshaikina
 */

public class PermissionUtils {

    public static final int ANDROID_VERSION = Build.VERSION.SDK_INT;
    public static final int REQUEST_CODE = 1;

    public static boolean checkVersion() {
        return ANDROID_VERSION > Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    public static boolean checkIfAlreadyHavePermission(Context context) {
        int write = ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read = ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE);
        return write == PackageManager.PERMISSION_GRANTED &&
                read == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestForStoragePermission(Fragment fragment) {
        fragment.requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
    }
}
