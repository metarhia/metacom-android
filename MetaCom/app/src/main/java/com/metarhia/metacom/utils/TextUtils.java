package com.metarhia.metacom.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

/**
 * Created by masha on 7/31/17.
 */

public class TextUtils {

    public static void copyToClipboard(Activity activity, String text) {
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context
                .CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("copy", text);
        clipboard.setPrimaryClip(clip);
    }
}
