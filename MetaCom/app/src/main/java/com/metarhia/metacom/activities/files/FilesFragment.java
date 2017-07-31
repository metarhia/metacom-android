package com.metarhia.metacom.activities.files;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    private static final String TMP_METACOM_JPG = "/tmp-metacom.jpg";
    private static final String AUTHORITY_STRING = "com.metarhia.metacom.fileprovider";
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
    private Unbinder mUnbinder;

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

        registerForContextMenu(mUploadFile);

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
        if (PermissionUtils.checkIfAlreadyHavePermission(getContext())) {
            DownloadFileDialog dialog = new DownloadFileDialog();
            dialog.setDownloadFileByCodeListener(this);
            dialog.show(getActivity().getSupportFragmentManager(), DownloadFileDialogTag);
        } else {
            showForbidDialog();
        }
    }

    @OnClick(R.id.upload_file)
    public void onUploadFileClick() {
        if (PermissionUtils.checkIfAlreadyHavePermission(getContext())) {
            showFileChooser();
        } else {
            showForbidDialog();
        }
    }

    private void showForbidDialog() {
        Toast.makeText(getContext(), getString(R.string.permissions_are_not_granted), Toast
                .LENGTH_SHORT).show();
    }

    @Override
    public void onFileDownloaded(final String filePath) {
        setBottomNoticeMessage(getString(R.string.complete));
        setBottomNoticeOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri selectedUri = Uri.parse("file:///" + filePath);
                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(selectedUri.toString());
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension
                        (fileExtension);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(selectedUri, mimeType);
                startActivity(Intent.createChooser(intent, getString(R.string.open_file)));
            }
        });
    }

    @Override
    public void onFileDownloadError() {
        Toast.makeText(getContext(), getString(R.string.downloading_error), Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onFileUploaded(String fileCode) {
        hideBottomNotice();
        DialogFragment dialog = UploadFileDialog.newInstance(fileCode);
        dialog.show(getActivity().getSupportFragmentManager(), UploadFileDialogTag);
    }

    @Override
    public void onFileUploadError(final String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void downloadByCode(String code) {
        setBottomNoticeMessage(String.format(getString(R.string.downloading), code));
        mFilesManager.downloadFile(code, this);
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
//                    fileUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory() +
// TMP_METACOM_JPG));
                    File f = new File(Environment.getExternalStorageDirectory() + TMP_METACOM_JPG);
                    fileUri = FileProvider.getUriForFile(getContext(), AUTHORITY_STRING, f);
                    break;
                }
            }
            try {
                InputStream is = getActivity().getContentResolver().openInputStream(fileUri);
                mFilesManager.uploadFile(is, this);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setBottomNoticeMessage(getString(R.string.uploading));
                    }
                });
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
//                Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory() +
// TMP_METACOM_JPG));
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


}
