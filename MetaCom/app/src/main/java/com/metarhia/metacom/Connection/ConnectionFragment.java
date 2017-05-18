package com.metarhia.metacom.Connection;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.metarhia.metacom.MainActivity;
import com.metarhia.metacom.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectionFragment extends Fragment {

    @BindView(R.id.host)
    TextInputEditText host;

    @BindView(R.id.port)
    TextInputEditText port;

    public ConnectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_connection, container, false);
        ButterKnife.bind(this, v);
        ButterKnife.findById(v, R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitClick();
            }
        });
        return v;
    }

    public void submitClick() {
        // todo set connection
        getActivity().finish();
    }

}
