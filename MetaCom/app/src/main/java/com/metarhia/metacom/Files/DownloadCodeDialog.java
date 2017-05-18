package com.metarhia.metacom.Files;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.metarhia.metacom.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class DownloadCodeDialog extends DialogFragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_download_code_dialog, null);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return v;
    }

}
