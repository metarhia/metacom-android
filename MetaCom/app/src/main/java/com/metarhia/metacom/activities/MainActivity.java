package com.metarhia.metacom.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.metarhia.metacom.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, new MainFragment(), MainFragment.MainFragmentTag)
                .commit();
    }
}
