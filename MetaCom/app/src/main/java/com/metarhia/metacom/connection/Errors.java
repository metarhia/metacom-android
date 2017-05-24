package com.metarhia.metacom.connection;

import android.content.res.Resources;

import com.metarhia.metacom.R;

/**
 * Error codes holder
 *
 * @author lidaamber
 */

public class Errors {

    /**
     * Local error codes
     */
    public static final int ERR_FILE_LOAD = 1;

    /**
     * API error codes
     */
    private static final int ERR_ROOM_TAKEN = 30;
    private static final int ERR_NOT_IN_CHAT = 31;
    private static final int ERR_NO_INTERLOCUTOR = 32;
    private static final int ERR_NO_SUCH_FILE = 33;

    /**
     * Application resources
     */
    private static Resources sResources;

    /**
     * Initializes utils class with app resources
     *
     * @param resources app resources
     */
    public static void initResources(Resources resources) {
        sResources = resources;
    }

    /**
     * Gets localized description of error
     *
     * @param code error code
     * @return description of error
     */
    public static String getErrorByCode(Integer code) {
        switch (code) {
            case ERR_ROOM_TAKEN:
                return sResources.getString(R.string.err_room_taken);
            case ERR_NOT_IN_CHAT:
                return sResources.getString(R.string.err_not_in_chat);
            case ERR_NO_INTERLOCUTOR:
                return sResources.getString(R.string.err_no_interlocutor);
            case ERR_NO_SUCH_FILE:
                return sResources.getString(R.string.err_no_such_file);
            case ERR_FILE_LOAD:
                return sResources.getString(R.string.err_file_load);
            default:
                return null;
        }
    }
}
