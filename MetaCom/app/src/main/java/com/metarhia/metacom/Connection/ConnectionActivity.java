package com.metarhia.metacom.Connection;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.metarhia.metacom.MainActivity;
import com.metarhia.metacom.R;
import com.metarhia.metacom.interfaces.ConnectionCallback;

public class ConnectionActivity extends AppCompatActivity implements ConnectionCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, new ConnectionFragment())
                .commit();
    }

    @Override
    public void onConnectionEstablished() {
        // TODO: onConnectionEstablished
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConnectionError() {

    }
}
