package com.metarhia.metacom.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.metarhia.metacom.R;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_CONNECTION_ID = "extraConnectionId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int connectionID = getIntent().getIntExtra(EXTRA_CONNECTION_ID, -1);

        if (connectionID != -1) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, MainFragment.newInstance(connectionID),
                            MainFragment.MAIN_FRAGMENT_TAG)
                    .commit();
        }
    }
}
