package com.metarhia.metacom;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.metarhia.metacom.Chat.ChatFragment;
import com.metarhia.metacom.Files.FilesFragment;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, v);

        final TextView files_tab = (TextView) ButterKnife.findById(v, R.id.files_tab);
        final TextView chat_tab = (TextView) ButterKnife.findById(v, R.id.chat_tab);

        files_tab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                files_tab.setTextColor(getResources().getColor(R.color.red));
                chat_tab.setTextColor(getResources().getColor(R.color.black));
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_screen_container, new FilesFragment())
                        .commit();
            }
        });

        chat_tab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                files_tab.setTextColor(getResources().getColor(R.color.black));
                chat_tab.setTextColor(getResources().getColor(R.color.red));
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_screen_container, new ChatFragment())
                        .commit();
            }
        });

        return v;
    }

}
