package com.metarhia.metacom.activities;

import android.app.Application;

import com.metarhia.metacom.connection.Errors;
import com.metarhia.metacom.utils.Constants;

/**
 * MetaCom application
 *
 * @author lidaamber
 */

public class MetaComApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Errors.initResources(getResources());
        Constants.initResources(getResources());
    }
}
