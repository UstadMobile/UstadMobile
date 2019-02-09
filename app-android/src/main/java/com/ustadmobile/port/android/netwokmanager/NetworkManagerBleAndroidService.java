package com.ustadmobile.port.android.netwokmanager;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Wrapper class for NetworkManagerBle. A service is required as this encapsulates
 * peer discovery processes and the http server that should continue running
 * regardless of which activity is active.
 *
 * @author Kileha3
 */
public class NetworkManagerBleAndroidService extends Service {

    private final IBinder mBinder = new NetworkManagerBleAndroidService.LocalServiceBinder();

    private AtomicReference<NetworkManagerAndroidBle> managerAndroidBleRef = new AtomicReference<>();

    private AtomicBoolean mHttpServiceBound = new AtomicBoolean(false);

    private AtomicReference<EmbeddedHTTPD> httpdRef = new AtomicReference<>();

    private ServiceConnection mHttpdServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mHttpServiceBound.set(true);
            httpdRef.set(((EmbeddedHttpdService.LocalServiceBinder)service).getHttpd());
            handleHttpdServiceBound();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mHttpServiceBound.set(false);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent serviceIntent = new Intent(getApplicationContext(), EmbeddedHttpdService.class);
        bindService(serviceIntent, mHttpdServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void handleHttpdServiceBound() {
        NetworkManagerAndroidBle managerAndroidBle = new NetworkManagerAndroidBle(this,
                httpdRef.get());
        managerAndroidBleRef.set(managerAndroidBle);
        managerAndroidBle.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mHttpServiceBound.get())
            unbindService(mHttpdServiceConnection);

        NetworkManagerAndroidBle managerAndroidBle = managerAndroidBleRef.get();
        if(managerAndroidBle != null)
            managerAndroidBle.onDestroy();
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we won't be dealing with IPC.
     */
    public class LocalServiceBinder extends Binder {
        public NetworkManagerBleAndroidService getService() {
            return NetworkManagerBleAndroidService.this;
        }

    }

}
