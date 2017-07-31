package com.metarhia.metacom.activities.connection;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.metarhia.metacom.R;

/**
 * @author MariaKokshaikina
 */
public class ConnectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new ConnectionFragment())
                    .commit();
        }
    }

}
