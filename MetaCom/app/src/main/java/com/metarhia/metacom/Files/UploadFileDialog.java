package com.metarhia.metacom.Files;


import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.metarhia.metacom.R;

import butterknife.ButterKnife;

import static com.metarhia.metacom.Files.FilesFragment.FilesFragmentTag;

/**
 * A simple {@link Fragment} subclass.
 */
public class UploadFileDialog extends DialogFragment {

    public final static String UploadFileDialogTag = "UploadFileDialogTag ";

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_upload_file_dialog, null);
        ButterKnife.bind(this, v);
        ButterKnife.findById(v, R.id.button_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return v;
    }
}
