package com.metarhia.metacom.Files;


import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.metarhia.metacom.R;
import com.metarhia.metacom.interfaces.FileDownloadedCallback;
import com.metarhia.metacom.interfaces.FileUploadedCallback;

import butterknife.ButterKnife;

import static com.metarhia.metacom.Files.DownloadFileDialog.DownloadFileDialogTag;
import static com.metarhia.metacom.Files.UploadFileDialog.UploadFileDialogTag;


/**
 * A simple {@link Fragment} subclass.
 */
public class FilesFragment extends Fragment implements FileDownloadedCallback, FileUploadedCallback {

    public final static String FilesFragmentTag = "FilesFragmentTag";

    private TextView bottom_notice_text = null;
    private View bottom_notice_layout = null;

    public FilesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_files, container, false);
        ButterKnife.bind(this, view);
        ButterKnife.findById(view, R.id.download_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialog = new DownloadFileDialog();
                dialog.show(getActivity().getSupportFragmentManager(), DownloadFileDialogTag);
            }
        });
        ButterKnife.findById(view, R.id.upload_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // todo open file browser to choose file
                new CountDownTimer(2000,1000){

                    @Override
                    public void onTick(long l) {

                    }

                    @Override
                    public void onFinish() {
                        onFileUploaded();
                    }
                }.start();
            }
        });

        bottom_notice_text = (TextView) ButterKnife.findById(view, R.id.bottom_notice_text);
        bottom_notice_layout = ButterKnife.findById(view, R.id.bottom_notice_layout);
        return view;
    }

    public void setBottomNotice(boolean visability, String message) {
        if (visability) {
            bottom_notice_layout.setVisibility(View.VISIBLE);
            bottom_notice_text.setText(message);
        } else {
            bottom_notice_layout.setVisibility(View.GONE);
        }
    }

    public void setBottomNoticeOnClick(final View.OnClickListener onClickListener) {
        bottom_notice_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onClick(view);
//                Toast.makeText(getContext(), "click", Toast.LENGTH_SHORT).show();
                bottom_notice_layout.setOnClickListener(null);
                bottom_notice_layout.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onFileDownloaded() {
        setBottomNotice(true, getString(R.string.complete));
        setBottomNoticeOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // todo choose app to open the file
                Toast.makeText(getContext(), "choose app to open", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onFileDownloadError() {

    }

    @Override
    public void onFileUploaded() {
        DialogFragment dialog = new UploadFileDialog();
        dialog.show(getActivity().getSupportFragmentManager(), UploadFileDialogTag);
    }

    @Override
    public void onFileUploadError() {

    }
}
