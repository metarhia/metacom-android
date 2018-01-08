package com.metarhia.metacom.utils;

/**
 * @author lidaamber
 */

public interface HistoryCallback {
    void onHistorySaved(String filename);

    void onSaveError();
}
