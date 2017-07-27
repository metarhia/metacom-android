package com.metarhia.metacom.activities.files;


import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.metarhia.metacom.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 *
 * @author MariaKokshaikina
 */
public class UploadFileDialog extends DialogFragment {

    public final static String UploadFileDialogTag = "UploadFileDialogTag";
    public final static String KEY_UPLOAD_FILE_CODE = "KEY_UPLOAD_FILE_CODE";

    @BindView(R.id.upload_result_string)
    TextView mUploadResultString;
    private Unbinder mUnbinder;

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
        mUploadResultString.setText(String.format(getResources().getString(R.string.upload_code), code));
        mUploadResultString.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("copy", code);
                clipboard.setPrimaryClip(clip);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), getString(R.string.copied_code), Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dismiss();
                            }
                        }
                );

        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
