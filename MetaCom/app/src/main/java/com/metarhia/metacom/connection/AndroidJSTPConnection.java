package com.metarhia.metacom.connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.support.v4.content.LocalBroadcastManager;

import com.metarhia.jstp.connection.JSTPConnection;
import com.metarhia.jstp.connection.JSTPConnectionListener;
import com.metarhia.jstp.connection.JSTPMessage;
import com.metarhia.jstp.connection.RestorationPolicy;
import com.metarhia.jstp.core.Handlers.ManualHandler;
import com.metarhia.jstp.core.JSTypes.JSArray;
import com.metarhia.jstp.core.JSTypes.JSObject;
import com.metarhia.jstp.core.JSTypes.JSValue;
import com.metarhia.jstp.transport.TCPTransport;
import com.metarhia.metacom.utils.Constants;
import com.metarhia.metacom.utils.NetworkUtils;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * JSTP connection wrapper for Android
 *
 * @author lidaamber
 * @author lundibundi
 */

public class AndroidJSTPConnection implements JSTPConnectionListener, RestorationPolicy {

    private static final String DEFAULT_CACHE = "defaultCacheTag";

    private static final int STATE_NOT_CONNECTED = 10;
    private static final int STATE_CONNECTING = 427;
    private static final int STATE_CONNECTED = 500;

    private final LocalBroadcastManager mBroadcastManager;

    private String mApplicationName;

    private final JSTPConnection mConnection;

    private final Context mContext;

    private final ConcurrentHashMap<String, ConcurrentHashMap<UUID, CacheCallData>> mTaggedCacheCalls;

    private int mConnectionState;

    private final List<AndroidJSTPConnectionListener> mListeners;

    public AndroidJSTPConnection(String host, int port, boolean usesSSL, Context context) {
        mListeners = new CopyOnWriteArrayList<>();
        mTaggedCacheCalls = new ConcurrentHashMap<>();
        mConnectionState = STATE_NOT_CONNECTED;

        mContext = context;

        TCPTransport transport = new TCPTransport(host, port, usesSSL);
        mConnection = new JSTPConnection(transport, this);
        mConnection.addSocketListener(this);

        mBroadcastManager = LocalBroadcastManager.getInstance(mContext);

        initNetworkReceiver();
    }

    private void initNetworkReceiver() {
        IntentFilter networkActionsFilter =
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        networkActionsFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

        BroadcastReceiver networkActionsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mApplicationName == null) return;

                if (!isConnectedFast()) reportConnectionLost();
                else if (!isConnected()) openConnection(mApplicationName);
            }
        };
        mContext.registerReceiver(networkActionsReceiver, networkActionsFilter);
    }

    public void openConnection(final String applicationName) {
        if (mConnectionState != STATE_NOT_CONNECTED) return;
        mConnectionState = STATE_CONNECTING;

        mApplicationName = applicationName;
        checkStartConnection();
    }

    @Override
    public boolean restore(JSTPConnection jstpConnection, Queue<JSTPMessage> queue) {
        queue.clear();
        return false;
    }

    @Override
    public void onTransportAvailable(JSTPConnection jstpConnection, String appName,
                                     String sessionID) {
        jstpConnection.handshake(appName, new ManualHandler() {
            @Override
            public void invoke(JSValue jsValue) {
                mConnectionState = STATE_CONNECTED;
                notifyHasConnection();

                if (mListeners != null) {
                    sendCachedCalls();
                    for (AndroidJSTPConnectionListener l : mListeners) {
                        l.onConnectionEstablished(AndroidJSTPConnection.this);
                    }
                }
            }
        });
    }

    private void checkStartConnection() {
        if (checkConnection()) {
            mConnectionState = STATE_CONNECTING;
            mConnection.connect(mApplicationName);
        } else {
            mConnectionState = STATE_NOT_CONNECTED;
        }
    }

    private boolean checkConnection() {
        return NetworkUtils.isConnectedWifi(mContext)
                || isConnectedFast();
    }

    public void sendCachedCalls() {
        sendCachedCalls(DEFAULT_CACHE);
    }

    public void sendCachedCalls(final String tag) {
        ConcurrentHashMap<UUID, CacheCallData> cacheCalls = mTaggedCacheCalls.get(tag);
        if (cacheCalls == null) return;

        for (final Map.Entry<UUID, CacheCallData> callData : cacheCalls.entrySet()) {
            sendCachedCall(tag, callData.getKey(), callData.getValue());
        }
    }

    private void sendCachedCall(final String tag, final UUID uuid, final CacheCallData callData) {
        mConnection.call(callData.mInterfaceName,
                callData.mMethodName,
                callData.mArgs,
                new ManualHandler() {
                    @Override
                    public void invoke(JSValue jsValue) {
                        CacheCallData callData = mTaggedCacheCalls.get(tag).remove(uuid);
                        callData.mManualHandler.invoke(jsValue);
                    }
                });
    }

    public boolean isConnected() {
        return mConnectionState == STATE_CONNECTED;
    }

    public JSTPConnection getJSTPConnection() {
        if (isConnected()) return mConnection;
        return null;
    }

    @Override
    public void onConnectionClosed() {
        reportConnectionLost();
        if (isConnectedFast()) openConnection(mApplicationName);
        else if (needsConnection()) notifyNeedsConnection();
    }

    public boolean isCached(UUID uuid) {
        return isCached(DEFAULT_CACHE, uuid);
    }

    public boolean isCached(String cacheName, UUID uuid) {
        if (uuid == null) return false;
        ConcurrentHashMap<UUID, CacheCallData> cachedCalls = mTaggedCacheCalls.get(cacheName);
        return cachedCalls != null && cachedCalls.containsKey(uuid);
    }

    public boolean removeCachedCall(UUID uuid) {
        return removeCachedCall(DEFAULT_CACHE, uuid);
    }

    public boolean removeCachedCall(String cacheName, UUID uuid) {
        if (uuid == null) return false;
        ConcurrentHashMap<UUID, CacheCallData> cachedCalls = mTaggedCacheCalls.get(cacheName);
        return cachedCalls != null && cachedCalls.remove(uuid) != null;
    }

    public void removeCachedCalls(String cacheName) {
        ConcurrentHashMap<UUID, CacheCallData> cachedCalls = mTaggedCacheCalls.get(cacheName);
        if (cachedCalls != null) cachedCalls.clear();
    }

    public interface AndroidJSTPConnectionListener {
        void onConnectionEstablished(AndroidJSTPConnection connection);

        void onConnectionLost();
    }

    public UUID cacheCall(String interfaceName, final String methodName, JSArray args,
                          final ManualHandler handler) {
        return cacheCall(DEFAULT_CACHE, interfaceName, methodName, args, handler);
    }

    public UUID cacheCall(final String cacheTag, String interfaceName, final String methodName,
                          JSArray args, final ManualHandler handler) {
        ConcurrentHashMap<UUID, CacheCallData> cachedCalls = mTaggedCacheCalls.get(cacheTag);
        if (cachedCalls == null) {
            cachedCalls = new ConcurrentHashMap<>();
            mTaggedCacheCalls.put(cacheTag, cachedCalls);
        }
        CacheCallData cacheCallData = new CacheCallData(interfaceName, methodName, args, handler);
        final UUID uuid = UUID.randomUUID();
        cachedCalls.put(uuid, cacheCallData);
        if (isConnected()) {
            sendCachedCall(cacheTag, uuid, cacheCallData);
        } else if (!NetworkUtils.isConnected(mContext)) {
            notifyNeedsConnection();
        }
        return uuid;
    }

    private void reportConnectionLost() {
        mConnectionState = STATE_NOT_CONNECTED;
        for (AndroidJSTPConnectionListener listener : mListeners) {
            listener.onConnectionLost();
        }
    }

    public void notifyNeedsConnection() {
        mBroadcastManager.sendBroadcast(new Intent(Constants.ACTION_NEEDS_CONNECTION));
    }

    public void notifyHasConnection() {
        mBroadcastManager.sendBroadcast(new Intent(Constants.ACTION_HAS_CONNECTION));
    }

    public boolean needsConnection() {
        for (Map.Entry<String, ConcurrentHashMap<UUID, CacheCallData>> me :
                mTaggedCacheCalls.entrySet()) {
            if (me.getValue().size() != 0) return true;
        }
        return false;
    }

    public void event(String interfaceName, String methodName, JSArray args) {
        mConnection.event(interfaceName, methodName, args);
    }

    public void addCallHandler(String methodName, ManualHandler callHandler) {
        mConnection.setCallHandler(methodName, callHandler);
    }

    public void addEventHandler(String interfaceName, String eventName, ManualHandler handler) {
        mConnection.addEventHandler(interfaceName, eventName, handler);
    }

    private boolean isConnectedFast() {
        return NetworkUtils.isConnectedFast(mContext);
    }

    @Override
    public void onConnected(boolean restored) {
    }

    @Override
    public void onPacketRejected(JSObject jsObject) {
    }

    @Override
    public void onConnectionError(int i) {
    }

    public void addListener(AndroidJSTPConnectionListener listener) {
        mListeners.add(listener);
    }

    public void removeListener(AndroidJSTPConnectionListener listener) {
        mListeners.remove(listener);
    }

    private static class CacheCallData {

        public final String mInterfaceName;
        public final String mMethodName;
        public final JSArray mArgs;
        public final ManualHandler mManualHandler;

        public CacheCallData(String interfaceName, String methodName, JSArray args,
                             ManualHandler manualHandler) {
            this.mArgs = args;
            this.mInterfaceName = interfaceName;
            this.mMethodName = methodName;
            this.mManualHandler = manualHandler;
        }
    }
}
