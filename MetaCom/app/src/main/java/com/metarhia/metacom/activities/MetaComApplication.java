package com.metarhia.metacom.activities;

import android.app.Application;

import com.metarhia.metacom.models.UserConnectionsManager;

/**
 * MetaCom application
 *
 * @author lidaamber
 */

public class MetaComApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        UserConnectionsManager.get(getApplicationContext());
    }
}
