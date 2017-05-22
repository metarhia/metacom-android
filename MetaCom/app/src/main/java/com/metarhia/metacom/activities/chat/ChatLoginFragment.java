package com.metarhia.metacom.activities.chat;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.metarhia.metacom.R;
import com.metarhia.metacom.interfaces.JoinRoomCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatLoginFragment extends Fragment implements JoinRoomCallback {

    private Unbinder mUnbinder;

    @BindView(R.id.submit)
    AppCompatButton mButtonSubmit;

    public ChatLoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.submit)
    public void onButtonSubmitClick() {
        mButtonSubmit.setClickable(false);
        // TODO: set chat connection
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onJoinedRoom();
            }
        }, 1000);
    }

    @Override
    public void onJoinedRoom() {
        mButtonSubmit.setClickable(true);
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        startActivity(intent);
    }

    @Override
    public void onJoinError(String errorMessage) {
        mButtonSubmit.setClickable(true);
        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
