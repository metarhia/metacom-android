package com.metarhia.metacom.utils;

import android.content.res.Resources;

import com.metarhia.metacom.R;

/**
 * Application constants
 *
 * @author lidaamber
 */

public class Constants {

    private static Resources sResources;

    public static void initResources(Resources resources) {
        sResources = resources;

        EVENT_CHAT_JOIN = sResources.getString(R.string.event_chat_join);
        EVENT_CHAT_LEAVE = sResources.getString(R.string.event_chat_leave);
        DOWNLOAD_FAILED = sResources.getString(R.string.download_failed);
    }

    public static final String APPLICATION_NAME = "metarhia.com";

    public static final String META_COM = "metacom";

    public static final String ACTION_NEEDS_CONNECTION = "actionNeedsConnection";
    public static final String ACTION_HAS_CONNECTION = "actionHasConnection";

    public static String EVENT_CHAT_JOIN;
    public static String EVENT_CHAT_LEAVE;
    public static String DOWNLOAD_FAILED;


    /**
     * Creates info message about file path
     *
     * @param filePath file path
     * @return info message about file path
     */
    public static String composeFilePathInfo(String filePath) {
        return String.format(sResources.getString(R.string.file_path), filePath);
    }

}
