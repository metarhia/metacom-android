package com.metarhia.metacom.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by masha on 9/15/17.
 */

public class KeyboardUtils {

    public static void hideKeyboard(Activity activity) {
        View currentFocus = activity.getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context
                    .INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }
}
