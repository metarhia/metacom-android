package com.metarhia.metacom.Files;


import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.metarhia.metacom.MainFragment;
import com.metarhia.metacom.R;

import butterknife.ButterKnife;

import static com.metarhia.metacom.Files.FilesFragment.FilesFragmentTag;
import static com.metarhia.metacom.MainFragment.MainFragmentTag;

/**
 * A simple {@link Fragment} subclass.
 */
public class DownloadFileDialog extends DialogFragment {

    public final static String DownloadFileDialogTag = "DownloadFileDialogTag";

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_download_code_dialog, null);
        ButterKnife.bind(this, v);
        ButterKnife.findById(v, R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        ButterKnife.findById(v, R.id.button_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // todo start downloading file
                final FilesFragment fragment = (FilesFragment) getFragmentManager().findFragmentByTag(FilesFragmentTag);
                if (fragment != null && fragment.isVisible()) {
                    fragment.setBottomNotice(true, getString(R.string.downloading));
                    CountDownTimer countDownTimer = new CountDownTimer(2000, 1000) {
                        @Override
                        public void onTick(long l) {

                        }

                        @Override
                        public void onFinish() {
                            fragment.onFileDownloaded();
                        }
                    }.start();
                }
                dismiss();
            }
        });
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return v;
    }

}
