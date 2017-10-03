package com.metarhia.metacom.activities.connection;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.metarhia.metacom.R;
import com.metarhia.metacom.activities.MainActivity;
import com.metarhia.metacom.interfaces.ConnectionCallback;
import com.metarhia.metacom.models.ConnectionInfoProvider;
import com.metarhia.metacom.models.UserConnectionsManager;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * @author MariaKokshaikina
 */
public class ConnectionFragment extends Fragment implements ConnectionCallback {

    @BindView(R.id.host)
    AutoCompleteTextView mHostEditText;
    @BindView(R.id.port)
    TextInputEditText mPortEditText;
    @BindView(R.id.submit)
    TextView mButtonSubmit;
    @BindView(R.id.spinner)
    ProgressBar mSpinner;
    private Unbinder mUnbinder;

    private String mHost;
    private Integer mPort;
    private boolean mIsUIVisible = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_connection, container, false);
        mUnbinder = ButterKnife.bind(this, v);

        final Map<String, Integer> infoList = ConnectionInfoProvider.restoreConnectionInfo
                (getActivity());
        if (infoList != null) {
            String[] hosts = infoList.keySet().toArray(new String[infoList.size()]);
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout
                    .support_simple_spinner_dropdown_item, hosts);
            mHostEditText.setAdapter(adapter);
            mHostEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String host = adapter.getItem(i);
                    mPortEditText.setText(infoList.get(host) + "");
                }
            });
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity
                ().getApplicationContext());
        String host = sharedPref.getString(getString(R.string.shared_preferences_host), null);
        int port = sharedPref.getInt(getString(R.string.shared_preferences_port), -1);
        boolean isAuthorized = sharedPref.getBoolean(getString(R.string
                .shared_preferences_is_authorized), false);
        if (host != null && port != -1) {
            mHostEditText.setText(host);
            mPortEditText.setText(port + "");
        }
        if (isAuthorized) {
            setButtonSubmitClick();
        }

        return v;
    }

    @OnClick(R.id.submit)
    public void setButtonSubmitClick() {
        mHost = mHostEditText.getText().toString();
        if (!mPortEditText.getText().toString().isEmpty() && !mHost.isEmpty()) {
            mPort = Integer.valueOf(mPortEditText.getText().toString());
            ConnectionInfoProvider.saveConnectionInfo(getActivity(), mHost, mPort);
            mButtonSubmit.setVisibility(View.INVISIBLE);
            mSpinner.setVisibility(View.VISIBLE);
            mHostEditText.setEnabled(false);
            mPortEditText.setEnabled(false);
            Context context = getActivity().getApplicationContext();
            UserConnectionsManager.get().addConnection(context, mHost, mPort, this);
        }
    }

    @OnClick(R.id.installation_guide)
    public void showInstallationGuide() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(getString(R.string.installation_guide_link)));
        startActivity(Intent.createChooser(i, getString(R.string.installation_chooser)));
    }

    @Override
    public void onConnectionEstablished(final int connectionID) {
        if (mIsUIVisible) {
            mButtonSubmit.setVisibility(View.VISIBLE);
            mSpinner.setVisibility(View.INVISIBLE);
            mHostEditText.setEnabled(true);
            mPortEditText.setEnabled(true);
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity
                ().getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.shared_preferences_host), mHost);
        editor.putInt(getString(R.string.shared_preferences_port), mPort);
        editor.putBoolean(getString(R.string.shared_preferences_is_authorized), true);
        editor.apply();

        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_CONNECTION_ID, connectionID);
        intent.putExtra(MainActivity.EXTRA_HOST_NAME, mHost);
        intent.putExtra(MainActivity.EXTRA_PORT, mPort);
        startActivity(intent);
    }

    @Override
    public void onConnectionError() {
        if (mIsUIVisible) {
            mButtonSubmit.setVisibility(View.VISIBLE);
            mSpinner.setVisibility(View.INVISIBLE);
            mHostEditText.setEnabled(true);
            mPortEditText.setEnabled(true);
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
        mIsUIVisible = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsUIVisible = true;
    }
}
