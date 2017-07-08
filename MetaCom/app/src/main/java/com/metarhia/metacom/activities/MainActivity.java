package com.metarhia.metacom.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.metarhia.metacom.R;

/**
 * @author MariaKokshaikina
 */
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_CONNECTION_ID = "extraConnectionId";
    public static final String EXTRA_HOST_NAME = "extraHostName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int connectionID = getIntent().getIntExtra(EXTRA_CONNECTION_ID, -1);
        String hostName = getIntent().getStringExtra(EXTRA_HOST_NAME);

        if (connectionID != -1) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, MainFragment.newInstance(connectionID, hostName),
                            MainFragment.MAIN_FRAGMENT_TAG)
                    .commit();
        }
    }
}
