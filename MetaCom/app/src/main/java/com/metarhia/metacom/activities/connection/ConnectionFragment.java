package com.metarhia.metacom.activities.connection;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
 * @author MariaKokshaikina
 */
public class ConnectionFragment extends Fragment implements ConnectionCallback {

    @BindView(R.id.host)
    TextInputEditText mHostEditText;
    @BindView(R.id.port)
    TextInputEditText mPortEditText;
    @BindView(R.id.submit)
    AppCompatButton mButtonSubmit;
    @BindView(R.id.spinner)
    ProgressBar mSpinner;
    private Unbinder mUnbinder;

    private String mHost;
    private Integer mPort;
    private boolean isUIVisible = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_connection, container, false);
        mUnbinder = ButterKnife.bind(this, v);
        return v;
    }

    @OnClick(R.id.submit)
    public void setButtonSubmitClick() {
        mHost = mHostEditText.getText().toString();
        mPort = Integer.valueOf(mPortEditText.getText().toString());
        if (!mHost.isEmpty()) {
            mButtonSubmit.setVisibility(View.GONE);
            mSpinner.setVisibility(View.VISIBLE);
            mHostEditText.setEnabled(false);
            mPortEditText.setEnabled(false);
            UserConnectionsManager.get().addConnection(getActivity(), mHost, mPort, this);
        }
    }

    @Override
    public void onConnectionEstablished(final int connectionID) {
        mButtonSubmit.setVisibility(View.VISIBLE);
        mSpinner.setVisibility(View.INVISIBLE);
        mHostEditText.setEnabled(true);
        mPortEditText.setEnabled(true);
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_CONNECTION_ID, connectionID);
        intent.putExtra(MainActivity.EXTRA_HOST_NAME, mHost);
        intent.putExtra(MainActivity.EXTRA_PORT, mPort);
        startActivity(intent);
    }

    @Override
    public void onConnectionError() {
        mButtonSubmit.setVisibility(View.VISIBLE);
        mSpinner.setVisibility(View.INVISIBLE);
        mHostEditText.setEnabled(true);
        mPortEditText.setEnabled(true);
        if (isUIVisible) {
            Toast.makeText(getContext(), getString(R.string.connection_error), Toast
                    .LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onPause() {
        super.onPause();
        isUIVisible = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        isUIVisible = true;
    }
}
