package com.metarhia.metacom.Chat;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.metarhia.metacom.R;
import com.metarhia.metacom.interfaces.ChatCallback;

import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatLoginFragment extends Fragment implements ChatCallback{


    public ChatLoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, view);
        ButterKnife.findById(view, R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: set chat connection
                onChatEstablished();
            }
        });
        return view;
    }

    @Override
    public void onChatEstablished() {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        startActivity(intent);
    }

    @Override
    public void onChatError() {

    }
}
