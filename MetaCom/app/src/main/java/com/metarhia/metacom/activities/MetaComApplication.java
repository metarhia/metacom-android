package com.metarhia.metacom.activities;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

/**
 * Created by Lida on 18.05.17.
 */

public class MetaComApplication extends Application {

    private static Context mContext;

    private static Resources mAppResources;

    public MetaComApplication() {
        super();

        mContext = getApplicationContext();
        mAppResources = getResources();
    }

    public static Context getContext() {
        return mContext;
    }

    public static Resources getAppResources() {
        return mAppResources;
    }
}
