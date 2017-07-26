package com.metarhia.metacom.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.metarhia.metacom.R;

/**
 * @author MariaKokshaikina
 */
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_CONNECTION_ID = "extraConnectionId";
    public static final String EXTRA_HOST_NAME = "extraHostName";

    private MainFragment mMainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int connectionID = getIntent().getIntExtra(EXTRA_CONNECTION_ID, -1);
        String hostName = getIntent().getStringExtra(EXTRA_HOST_NAME);

        if (connectionID != -1) {
            mMainFragment = MainFragment.newInstance(connectionID, hostName);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, mMainFragment,
                            MainFragment.MAIN_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.exit)
                .setMessage(R.string.confirm_exit)
                .setCancelable(false)
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mMainFragment.leaveServer();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
