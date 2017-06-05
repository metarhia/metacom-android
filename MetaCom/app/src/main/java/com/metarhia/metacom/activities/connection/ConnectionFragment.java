package com.metarhia.metacom.activities.connection;


import android.content.Intent;
import android.os.Bundle;
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
import com.metarhia.metacom.models.UserConnectionsManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectionFragment extends Fragment implements ConnectionCallback {

    @BindView(R.id.host)
    TextInputEditText mHostEditText;
    @BindView(R.id.port)
    TextInputEditText mPortEditText;
    @BindView(R.id.submit)
    AppCompatButton mButtonSubmit;
    private String host;
    private int port;
    private Unbinder mUnbinder;

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

        host = mHostEditText.getText().toString();
        port = Integer.valueOf(mPortEditText.getText().toString());

        // TODO validate data

        UserConnectionsManager.get().addConnection(host, port, this);
    }


    @Override
    public void onConnectionEstablished(int connectionID) {
        mButtonSubmit.setClickable(true);
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_CONNECTION_ID, connectionID);
        intent.putExtra(MainActivity.EXTRA_HOST_NAME, host);
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
