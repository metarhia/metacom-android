package com.metarhia.metacom;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.metarhia.metacom.Chat.ChatFragment;
import com.metarhia.metacom.Chat.ChatLoginFragment;
import com.metarhia.metacom.Files.FilesFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.metarhia.metacom.Files.FilesFragment.FilesFragmentTag;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    public final static String MainFragmentTag = "MainFragmentTag";

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, v);

        final TextView toolbar_title = (TextView) ButterKnife.findById(v, R.id.toolbar_title);
        toolbar_title.setText(getString(R.string.hostname));
        final ImageView toolbar_back = (ImageView) ButterKnife.findById(v, R.id.toolbar_back);
        toolbar_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        final TextView files_tab = (TextView) ButterKnife.findById(v, R.id.files_tab);
        final TextView chat_tab = (TextView) ButterKnife.findById(v, R.id.chat_tab);

        files_tab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                files_tab.setTextColor(getResources().getColor(R.color.red));
                chat_tab.setTextColor(getResources().getColor(R.color.black));
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_screen_container, new FilesFragment(), FilesFragmentTag)
                        .commit();
            }
        });

        chat_tab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                files_tab.setTextColor(getResources().getColor(R.color.black));
                chat_tab.setTextColor(getResources().getColor(R.color.red));
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_screen_container, new ChatLoginFragment())
                        .commit();
            }
        });

        return v;
    }


}
