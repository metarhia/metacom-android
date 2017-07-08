package com.metarhia.metacom.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Check device's network NetworkUtils and speed
 *
 * @author emil http://stackoverflow.com/users/220710/emil
 *         minor changes lundibundi
 */
public class NetworkUtils {

    private static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    public static boolean isConnected(Context context) {
        NetworkInfo info = NetworkUtils.getNetworkInfo(context);
        return (info != null && info.isConnected());
    }

    public static boolean isConnectedWifi(Context context) {
        NetworkInfo info = NetworkUtils.getNetworkInfo(context);
        return (info != null && info.isConnected() &&
                info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public static boolean isConnectedFast(Context context) {
        return isConnected(context);
    }
}
