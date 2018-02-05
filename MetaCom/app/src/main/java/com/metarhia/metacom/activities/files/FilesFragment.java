package com.metarhia.metacom.activities.files;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.metarhia.metacom.BuildConfig;
import com.metarhia.metacom.R;
import com.metarhia.metacom.interfaces.DownloadFileByCodeListener;
import com.metarhia.metacom.interfaces.FileDownloadedListener;
import com.metarhia.metacom.interfaces.FileUploadedCallback;
import com.metarhia.metacom.models.FilesManager;
import com.metarhia.metacom.models.UserConnectionsManager;
import com.metarhia.metacom.utils.PermissionUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.metarhia.metacom.activities.files.DownloadFileDialog.DownloadFileDialogTag;
import static com.metarhia.metacom.activities.files.UploadFileDialog.UploadFileDialogTag;


/**
 * @author MariaKokshaikina
 */
public class FilesFragment extends Fragment implements FileDownloadedListener,
        FileUploadedCallback, DownloadFileByCodeListener {

    private static final String KEY_CONNECTION_ID = "keyConnectionId";
    private static final String KEY_BOTTOM_NOTICE = "keyBottomNotice";
    private static final String KEY_BOTTOM_MESSAGE = "keyBottomMessage";
    private static final String KEY_OPEN_FILE = "keyOpenFile";
    private static final String KEY_FILE_URI = "keyFileUri";
    private static final String TMP_METACOM_JPG = "/tmp-metacom.jpg";
    private static final String AUTHORITY_STRING = BuildConfig.APPLICATION_ID + ".provider";

    private static final int PICK_IMAGE_FROM_EXPLORER = 0;
    private static final int PICK_IMAGE_FROM_CAMERA = 1;
    private static final int TAKE_PHOTO = 2;
    private static final int FILE_EXPLORER = 3;

    @BindView(R.id.bottom_notice_text)
    TextView mBottomNoticeText;
    @BindView(R.id.bottom_notice_layout)
    View mBottomNoticeLayout;
    @BindView(R.id.upload_file)
    ImageView mUploadFile;
    @BindView(R.id.download_file)
    ImageView mDownloadFile;
    private Unbinder mUnbinder;

    private FilesManager mFilesManager;
    private boolean mBottomNotice;
    private boolean mOpenFile;
    private String mFilePath;
    private boolean isUIVisible;
    private boolean showUploadDialog;
    private String showUploadDialogCode;

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

        setRetainInstance(true);

        registerForContextMenu(mUploadFile);

        if (getArguments() != null) {
            int connectionID = getArguments().getInt(KEY_CONNECTION_ID);
            mFilesManager = UserConnectionsManager.get().getConnection(connectionID)
                    .getFilesManager();
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(KEY_BOTTOM_NOTICE)) {
                setBottomNoticeMessage(savedInstanceState.getString(KEY_BOTTOM_MESSAGE));
            }
            if (savedInstanceState.getBoolean(KEY_OPEN_FILE)) {
                String fileUri = savedInstanceState.getString(KEY_FILE_URI);
                onFileDownloaded(fileUri);
            }
        }

        return view;
    }

    public void hideBottomNotice() {
        mBottomNotice = false;
        mBottomNoticeLayout.setVisibility(View.GONE);
    }

    public void setBottomNoticeMessage(String message) {
        mBottomNotice = true;
        mBottomNoticeLayout.setVisibility(View.VISIBLE);
        mBottomNoticeText.setText(message);
    }

    @OnClick(R.id.download_file)
    public void onDownloadFileClick() {
        if (PermissionUtils.checkIfAlreadyHavePermission(getContext())) {
            DownloadFileDialog dialog = new DownloadFileDialog();
            dialog.setDownloadFileByCodeListener(this);
            dialog.show(getActivity().getSupportFragmentManager(), DownloadFileDialogTag);
        } else {
            if (PermissionUtils.checkVersion()) {
                PermissionUtils.requestForStoragePermission(this);
            }
        }
    }

    @OnClick(R.id.upload_file)
    public void onUploadFileClick() {
        if (PermissionUtils.checkIfAlreadyHavePermission(getContext())) {
            showFileChooser();
        } else {
            if (PermissionUtils.checkVersion()) {
                PermissionUtils.requestForStoragePermission(this);
            }
        }
    }

    private void showForbidDialog() {
        if (isUIVisible) {
            Toast.makeText(getContext(), getString(R.string.permissions_are_not_granted),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFileDownloaded(final String filePath) {
        setBottomNoticeMessage(getString(R.string.complete));
        mDownloadFile.setEnabled(true);
        mDownloadFile.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable
                .ic_file_download_24dp, null));
        mOpenFile = true;
        mFilePath = filePath;
        mBottomNoticeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFile(mFilePath);
                mOpenFile = false;
                mBottomNoticeLayout.setOnClickListener(null);
                hideBottomNotice();
            }
        });
    }

    private void openFile(String filePath) {
        Uri uri = FileProvider.getUriForFile(getActivity(),
                BuildConfig.APPLICATION_ID + ".provider",
                new File(filePath));

        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension
                (fileExtension);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, getString(R.string.open_file)));
    }

    @Override
    public void onFileDownloadError() {
        if (isUIVisible) {
            Toast.makeText(getContext(), getString(R.string.download_failed), Toast.LENGTH_SHORT)
                    .show();
        }
        hideBottomNotice();
        mDownloadFile.setEnabled(true);
        mDownloadFile.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable
                .ic_file_download_24dp, null));
    }

    @Override
    public void onFileUploaded(final String fileCode) {
        if (isUIVisible) {
            showUploadDialog = false;
            hideBottomNotice();
            DialogFragment dialog = UploadFileDialog.newInstance(fileCode);
            dialog.show(getActivity().getSupportFragmentManager(), UploadFileDialogTag);
        } else {
            showUploadDialog = true;
            showUploadDialogCode = fileCode;
        }
        mUploadFile.setEnabled(true);
        mUploadFile.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable
                .ic_file_upload_24dp, null));
    }

    @Override
    public void onFileUploadError(final String message) {
        if (isUIVisible) {
            Toast.makeText(getContext(), getString(R.string.err_upload_failed), Toast
                    .LENGTH_SHORT).show();
        }
        hideBottomNotice();
        mUploadFile.setEnabled(true);
        mUploadFile.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable
                .ic_file_upload_24dp, null));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void downloadByCode(String code) {
        setBottomNoticeMessage(getString(R.string.downloading_dots));
        mFilesManager.downloadFile(code, this);
        mDownloadFile.setEnabled(false);
        mDownloadFile.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable
                .ic_file_download_grey_24dp, null));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == PICK_IMAGE_FROM_EXPLORER || requestCode == PICK_IMAGE_FROM_CAMERA) &&
                resultCode == Activity.RESULT_OK) {
            Uri fileUri = null;
            switch (requestCode) {
                case PICK_IMAGE_FROM_EXPLORER: {
                    fileUri = data.getData();
                    break;
                }
                case PICK_IMAGE_FROM_CAMERA: {
                    File f = new File(Environment.getExternalStorageDirectory() + TMP_METACOM_JPG);
                    fileUri = FileProvider.getUriForFile(getContext(), AUTHORITY_STRING, f);
                    break;
                }
            }
            try {
                InputStream is = getActivity().getContentResolver().openInputStream(fileUri);
                mFilesManager.uploadFile(is, this);
                setBottomNoticeMessage(getString(R.string.uploading_dots));
                mUploadFile.setEnabled(false);
                mUploadFile.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R
                        .drawable.ic_file_upload_grey_24dp, null));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void showFileChooser() {
        getActivity().openContextMenu(mUploadFile);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo
            menuInfo) {
        if (v.getId() == mUploadFile.getId()) {
            menu.add(0, TAKE_PHOTO, 0, R.string.take_photo);
            menu.add(0, FILE_EXPLORER, 0, R.string.file_explorer);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case TAKE_PHOTO:
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File f = new File(Environment.getExternalStorageDirectory() + TMP_METACOM_JPG);
                Uri uri = FileProvider.getUriForFile(getContext(), AUTHORITY_STRING, f);
                takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, PICK_IMAGE_FROM_CAMERA);
                }
                return true;
            case FILE_EXPLORER:
                Intent intent = new Intent();
                intent.setType("*/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string
                        .select_file)), PICK_IMAGE_FROM_EXPLORER);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionUtils.REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showFileChooser();
                } else {
                    showForbidDialog();
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mBottomNotice) {
            outState.putBoolean(KEY_BOTTOM_NOTICE, mBottomNotice);
            outState.putString(KEY_BOTTOM_MESSAGE, mBottomNoticeText.getText().toString());
        }
        if (mOpenFile) {
            outState.putBoolean(KEY_OPEN_FILE, mOpenFile);
            outState.putString(KEY_FILE_URI, mFilePath);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isUIVisible = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        isUIVisible = true;
        if (showUploadDialog) {
            onFileUploaded(showUploadDialogCode);
        }
    }
}
