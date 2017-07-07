package com.metarhia.metacom.utils;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

/**
 * Main thread executor
 *
 * @author lidaamber
 */

public class MainExecutor implements Executor {

    /**
     * Executor instance
     */
    private static MainExecutor sInstance;

    /**
     * Main thread handler
     */
    private final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * Creates new main thread executor
     */
    private MainExecutor() {
    }

    /**
     * Gets executor instance
     *
     * @return main thread executor
     */
    public static MainExecutor get() {
        if (sInstance == null) {
            sInstance = new MainExecutor();
        }

        return sInstance;
    }

    @Override
    public void execute(@NonNull Runnable runnable) {
        handler.post(runnable);
    }
}