package com.metarhia.metacom.activities.files;


import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.metarhia.metacom.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.metarhia.metacom.utils.TextUtils.copyToClipboard;

/**
 * @author MariaKokshaikina
 */
public class UploadFileDialog extends DialogFragment {

    public final static String UploadFileDialogTag = "UploadFileDialogTag";
    public final static String KEY_UPLOAD_FILE_CODE = "KEY_UPLOAD_FILE_CODE";

    @BindView(R.id.upload_result_string)
    TextView mUploadResultString;
    private Unbinder mUnbinder;

    private boolean isUIVisible = true;

    public static UploadFileDialog newInstance(String fileCode) {
        Bundle args = new Bundle();
        args.putString(KEY_UPLOAD_FILE_CODE, fileCode);

        UploadFileDialog fragment = new UploadFileDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        View view = layoutInflater.inflate(R.layout.fragment_upload_file_dialog, null);
        mUnbinder = ButterKnife.bind(this, view);
        final String code = getArguments().getString(KEY_UPLOAD_FILE_CODE);
        mUploadResultString.setText(String.format(getResources().getString(R.string.upload_desc),
                code));
        mUploadResultString.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                copyToClipboard(getActivity(), code);
                if (isUIVisible) {
                    Toast.makeText(getContext(), getString(R.string.copied_code), Toast
                            .LENGTH_SHORT).show();
                }
                return true;
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.upload)
                .setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dismiss();
                            }
                        }
                )
                .setNegativeButton(getString(R.string.copy), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                        copyToClipboard(getActivity(), code);
                        if (isUIVisible) {
                            Toast.makeText(getContext(), getString(R.string.copied_code), Toast
                                    .LENGTH_SHORT).show();
                        }
                    }
                });

        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R
                .color.black14)));
        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
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
    }
}
