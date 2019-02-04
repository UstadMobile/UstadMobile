package com.ustadmobile.port.android.netwokmanager;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Wrapper class for NetworkManagerBle. A service is required as this encapsulates
 * peer discovery processes and the http server that should continue running
 * regardless of which activity is active.
 *
 * @author Kileha3
 */
public class BleNetworkService extends Service {

    private final IBinder mBinder = new BleNetworkService.LocalServiceBinder();

    private NetworkManagerAndroidBle managerAndroidBle;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        managerAndroidBle = new NetworkManagerAndroidBle(this);
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
        managerAndroidBle.onDestroy();
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we won't be dealing with IPC.
     */
    public class LocalServiceBinder extends Binder {
        public BleNetworkService getService() {
            return BleNetworkService.this;
        }

    }
}
