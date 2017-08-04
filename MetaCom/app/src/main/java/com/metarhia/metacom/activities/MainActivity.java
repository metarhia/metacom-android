package com.metarhia.metacom.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.metarhia.metacom.R;
import com.metarhia.metacom.interfaces.BackPressedHandler;

import static com.metarhia.metacom.activities.MainFragment.MAIN_FRAGMENT_TAG;

/**
 * @author MariaKokshaikina
 */
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_CONNECTION_ID = "extraConnectionId";
    public static final String EXTRA_HOST_NAME = "extraHostName";
    public static final String EXTRA_PORT = "extraPort";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int connectionID = getIntent().getIntExtra(EXTRA_CONNECTION_ID, -1);
        String hostName = getIntent().getStringExtra(EXTRA_HOST_NAME);
        int port = getIntent().getIntExtra(EXTRA_PORT, 0);

        if (savedInstanceState == null) {
            if (connectionID != -1) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, MainFragment.newInstance(connectionID,
                                hostName, port), MAIN_FRAGMENT_TAG)
                        .commit();
            }
        }
    }

    @Override
    public void onBackPressed() {
        ((BackPressedHandler) getSupportFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG))
                .handleBackPress();
    }

}
