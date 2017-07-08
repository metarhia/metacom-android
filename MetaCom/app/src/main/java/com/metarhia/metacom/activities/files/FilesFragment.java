package com.metarhia.metacom.activities.files;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.metarhia.metacom.R;
import com.metarhia.metacom.interfaces.DownloadFileByCodeListener;
import com.metarhia.metacom.interfaces.FileDownloadedCallback;
import com.metarhia.metacom.interfaces.FileUploadedCallback;
import com.metarhia.metacom.models.FilesManager;
import com.metarhia.metacom.models.UserConnectionsManager;

import java.io.FileNotFoundException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.metarhia.metacom.activities.files.DownloadFileDialog.DownloadFileDialogTag;
import static com.metarhia.metacom.activities.files.UploadFileDialog.UploadFileDialogTag;


/**
 * A simple {@link Fragment} subclass.
 *
 * @author MariaKokshaikina
 */
public class FilesFragment extends Fragment implements FileDownloadedCallback, FileUploadedCallback, DownloadFileByCodeListener {

//    public final static String FilesFragmentTag = "FilesFragmentTag";

    private static final String KEY_CONNECTION_ID = "keyConnectionId";

    public final int DIALOG_FRAGMENT_DONWLOAD = 1;

    @BindView(R.id.bottom_notice_text)
    TextView mBottomNoticeText;

    @BindView(R.id.bottom_notice_layout)
    View mBottomNoticeLayout;

    @BindView(R.id.download_file)
    ImageView mDownloadFile;

    @BindView(R.id.upload_file)
    ImageView mUploadFile;

    private String fileCode = null;
    private Unbinder mUnbinder;
    private static final int PICK_IMAGE = 0;
    private FilesManager mFilesManager;

    public static FilesFragment newInstance(int connectionID) {
        Bundle args = new Bundle();
        args.putInt(KEY_CONNECTION_ID, connectionID);
        FilesFragment fragment = new FilesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        if (getArguments() != null) {
            int connectionID = getArguments().getInt(KEY_CONNECTION_ID);
            mFilesManager = UserConnectionsManager.get().getConnection(connectionID)
                    .getFilesManager();
        }

        return view;
    }

    public void hideBottomNotice() {
        mBottomNoticeLayout.setVisibility(View.GONE);
    }

    public void setBottomNoticeMessage(String message) {
        mBottomNoticeLayout.setVisibility(View.VISIBLE);
        mBottomNoticeText.setText(message);
    }

    public void setBottomNoticeOnClick(final View.OnClickListener onClickListener) {
        mBottomNoticeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onClick(view);
                mBottomNoticeLayout.setOnClickListener(null);
                hideBottomNotice();
            }
        });
    }

    @OnClick(R.id.download_file)
    public void onDownloadFileClick() {
        DownloadFileDialog dialog = new DownloadFileDialog();
        dialog.setDownloadFileByCodeListener(this);
        dialog.show(getActivity().getSupportFragmentManager(), DownloadFileDialogTag);
    }

    @OnClick(R.id.upload_file)
    public void onUploadFileClick() {
        // todo open file browser to choose file

        showFileChooser();

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                onFileUploaded("123");
//            }
//        }, 2000);
    }

    @Override
    public void onFileDownloaded(String filePath) {
        setBottomNoticeMessage(getString(R.string.complete));
        setBottomNoticeOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // todo choose app to open the file
//                Toast.makeText(getContext(), "choose app to open", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onFileDownloadError() {
        // todo onFileDownloadError
    }

    @Override
    public void onFileUploaded(String fileCode) {
        DialogFragment dialog = UploadFileDialog.newInstance(fileCode);
        dialog.show(getActivity().getSupportFragmentManager(), UploadFileDialogTag);
    }

    @Override
    public void onFileUploadError(String message) {
        // todo onFileUploadError
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void downloadByCode(String code) {
        fileCode = code;
        setBottomNoticeMessage(getString(R.string.downloading));

        mFilesManager.downloadFile(fileCode, this);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                onFileDownloaded("stub");
//            }
//        }, 1000);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            Uri fileUri = data.getData();

            try {
                InputStream is = getActivity().getContentResolver().openInputStream(fileUri);
                mFilesManager.uploadFile(is, this);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select file"), PICK_IMAGE);
    }
}
