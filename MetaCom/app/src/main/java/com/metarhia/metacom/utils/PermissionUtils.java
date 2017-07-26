package com.metarhia.metacom.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by masha on 7/26/17.
 */

public class PermissionUtils {

    public static final int ANDROID_VERSION = Build.VERSION.SDK_INT;
    public static final int REQUEST_CODE = 1;

    public static boolean checkVersion() {
        return ANDROID_VERSION > Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    public static boolean checkIfAlreadyHavePermission(Context context) {
        int write = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        return write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestForSpecificPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
    }
}
