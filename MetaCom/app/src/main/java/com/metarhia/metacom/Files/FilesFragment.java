package com.metarhia.metacom.Files;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.metarhia.metacom.R;

import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class FilesFragment extends Fragment {


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
                DialogFragment dialog = new DownloadCodeDialog();
                dialog.show(getActivity().getSupportFragmentManager(),"download_file_dialog");
            }
        });
        return view;
    }

}
