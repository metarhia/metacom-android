package com.metarhia.metacom.activities.connection;


import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.metarhia.metacom.R;
import com.metarhia.metacom.interfaces.ConnectionCallback;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectionFragment extends Fragment {

    @BindView(R.id.host)
    TextInputEditText host;

    @BindView(R.id.port)
    TextInputEditText port;

    private AppCompatButton submit = null;

    public ConnectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_connection, container, false);
        ButterKnife.bind(this, v);
        submit = ButterKnife.findById(v, R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitClick();
            }
        });
        return v;
    }

    public void submitClick() {
        submit.setClickable(false);
        // todo set connection
        new CountDownTimer(2000, 1000) {

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                Toast.makeText(getContext(), "connection established", Toast.LENGTH_SHORT).show();
                submit.setClickable(true);
                ((ConnectionCallback) getActivity()).onConnectionEstablished();
            }
        }.start();
    }

}
