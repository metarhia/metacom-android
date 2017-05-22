package com.metarhia.metacom.activities.connection;


import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.metarhia.metacom.R;
import com.metarhia.metacom.activities.MainActivity;
import com.metarhia.metacom.interfaces.ConnectionCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectionFragment extends Fragment implements ConnectionCallback {

    private Unbinder mUnbinder;

    @BindView(R.id.host)
    TextInputEditText mHostEditText;

    @BindView(R.id.port)
    TextInputEditText mPortEditText;

    @BindView(R.id.submit)
    AppCompatButton mButtonSubmit;

    public ConnectionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_connection, container, false);
        mUnbinder = ButterKnife.bind(this, v);
        return v;
    }

    @OnClick(R.id.submit)
    public void setButtonSubmitClick() {
        mButtonSubmit.setClickable(false);
        // todo set connection
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), "connection established", Toast.LENGTH_SHORT).show();
                onConnectionEstablished();
            }
        }, 1000);
    }


    @Override
    public void onConnectionEstablished() {
        mButtonSubmit.setClickable(true);
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConnectionError() {
        // todo error message
        mButtonSubmit.setClickable(true);
        Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
