package com.ustadmobile.port.android.netwokmanager;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;


/**
 * <h1>NetworkServiceAndroid</h1>
 * <p>
 * NetworkServiceAndroid is effectively a wrapper for NetworkManager. A service is required as this
 * encapsulates network discovery processes and the http server that should continue running
 * regardless of which activity is active.
 *
 * @author kileha3
 * @see android.app.Service
 */
public class NetworkServiceAndroid extends Service {

    private final IBinder mBinder = new LocalServiceBinder();

    private NetworkManagerAndroid networkManagerAndroid;


    private NetworkManagerAndroidBle managerAndroidBle;

    public NetworkServiceAndroid() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //TODO: This have to go
        networkManagerAndroid = (NetworkManagerAndroid)
                UstadMobileSystemImplAndroid.getInstanceAndroid().getNetworkManager();
       networkManagerAndroid.init(NetworkServiceAndroid.this);

       /* managerAndroidBle = (NetworkManagerAndroidBle)
                UstadMobileSystemImplAndroid.getInstanceAndroid().getNetworkManagerBle();*/

    }

    @Override
    public void onDestroy() {
        networkManagerAndroid.onDestroy();
        managerAndroidBle.onDestroy();
        super.onDestroy();
    }

    /**
     * @return NetworkManagerAndroid : NetworkManagerAndroid class reference
     */
    public NetworkManagerAndroid getNetworkManager() {
        return networkManagerAndroid;
    }

    /**
     * @return NetworkManagerAndroidBle class reference
     */
    public NetworkManagerAndroidBle getNetworkManagerBle() {
        return managerAndroidBle;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we won't be dealing with IPC.
     */
    public class LocalServiceBinder extends Binder {
        public NetworkServiceAndroid getService() {
            return NetworkServiceAndroid.this;
        }

    }


}