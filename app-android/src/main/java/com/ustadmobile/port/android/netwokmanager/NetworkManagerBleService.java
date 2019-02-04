package com.ustadmobile.port.android.netwokmanager;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Wrapper class for NetworkManagerBle. A service is required as this encapsulates
 * network discovery processes and the http server that should continue running
 * regardless of which activity is active.
 *
 * @author Kileha3
 */
public class NetworkManagerBleService extends Service {

    private final IBinder mBinder = new NetworkManagerBleService.LocalServiceBinder();

    private NetworkManagerAndroidBle managerAndroidBle;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        managerAndroidBle = new NetworkManagerAndroidBle(this);
        managerAndroidBle.onCreate();
        return mBinder;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we won't be dealing with IPC.
     */
    public class LocalServiceBinder extends Binder {
        public NetworkManagerBleService getService() {
            return NetworkManagerBleService.this;
        }

    }

    /**
     * @return NetworkManagerAndroidBle class reference
     */
    public NetworkManagerAndroidBle getNetworkManagerBle() {
        return managerAndroidBle;
    }
}
