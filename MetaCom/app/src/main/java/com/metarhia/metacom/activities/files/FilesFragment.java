package com.metarhia.metacom.activities.files;


import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.metarhia.metacom.R;
import com.metarhia.metacom.interfaces.FileDownloadedCallback;
import com.metarhia.metacom.interfaces.FileUploadedCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.metarhia.metacom.activities.files.DownloadFileDialog.DownloadFileDialogTag;
import static com.metarhia.metacom.activities.files.UploadFileDialog.UploadFileDialogTag;


/**
 * A simple {@link Fragment} subclass.
 */
public class FilesFragment extends Fragment implements FileDownloadedCallback, FileUploadedCallback {

//    public final static String FilesFragmentTag = "FilesFragmentTag";

    Unbinder mUnbinder;

    @BindView(R.id.bottom_notice_text)
    TextView mBottomNoticeText;

    @BindView(R.id.bottom_notice_layout)
    View mBottomNoticeLayout;

    @BindView(R.id.download_file)
    ImageView mDownloadFile;

    @BindView(R.id.upload_file)
    ImageView mUploadFile;

    public FilesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_files, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        return view;
    }

    public void setBottomNotice(boolean visability, String message) {
        if (visability) {
            mBottomNoticeLayout.setVisibility(View.VISIBLE);
            mBottomNoticeText.setText(message);
        } else {
            mBottomNoticeLayout.setVisibility(View.GONE);
        }
    }

    public void setBottomNoticeOnClick(final View.OnClickListener onClickListener) {
        mBottomNoticeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onClick(view);
                mBottomNoticeLayout.setOnClickListener(null);
                mBottomNoticeLayout.setVisibility(View.GONE);
            }
        });
    }

    @OnClick(R.id.download_file)
    public void onDownloadFileClick() {
        DialogFragment dialog = new DownloadFileDialog();
        dialog.show(getActivity().getSupportFragmentManager(), DownloadFileDialogTag);
    }

    @OnClick(R.id.upload_file)
    public void onUploadFileClick() {
        // todo open file browser to choose file
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onFileUploaded();
            }
        }, 2000);
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
        // todo onFileDownloadError
    }

    @Override
    public void onFileUploaded() {
        DialogFragment dialog = new UploadFileDialog();
        dialog.show(getActivity().getSupportFragmentManager(), UploadFileDialogTag);
    }

    @Override
    public void onFileUploadError() {
        // todo onFileUploadError
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
