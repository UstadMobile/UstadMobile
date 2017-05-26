package com.ustadmobile.port.android.netwokmanager;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;

import edu.rit.se.wifibuddy.WifiDirectHandler;

import static com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroid.PREF_KEY_SUPERNODE;


/**
 * NetworkServiceAndroid is effectively a wrapper for NetworkManager. A service is required as this
 * encapsulates network discovery processes and the http server that should continue running
 * regardless of which activity is active.
 *
 */
public class NetworkServiceAndroid extends Service{

    private WifiDirectHandler wifiDirectHandler;
    private final IBinder mBinder = new LocalServiceBinder();
    private NetworkManagerAndroid networkManagerAndroid;
    public static final int SERVICE_REBROADCASTING_TIMER=30000;


    public NetworkServiceAndroid(){

    }


    @Override
    public void onCreate() {
        super.onCreate();
        Intent wifiServiceIntent = new Intent(this, WifiDirectHandler.class);
        bindService(wifiServiceIntent, wifiP2PServiceConnection, BIND_AUTO_CREATE);
        networkManagerAndroid = (NetworkManagerAndroid) UstadMobileSystemImplAndroid.getInstanceAndroid().getNetworkManager();
        networkManagerAndroid.init(NetworkServiceAndroid.this);

    }

    @Override
    public void onDestroy() {
        networkManagerAndroid.onDestroy();

        if(wifiDirectHandler!=null){
            wifiDirectHandler.removeGroup();
            wifiDirectHandler.stopServiceDiscovery();
            wifiDirectHandler.removeService();
            UstadMobileSystemImpl.getInstance().setAppPref("devices","",getApplicationContext());
        }
        unbindService(wifiP2PServiceConnection);


        super.onDestroy();
    }

    public NetworkManagerAndroid getNetworkManager() {
        return  networkManagerAndroid;
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



    ServiceConnection wifiP2PServiceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            wifiDirectHandler = ((WifiDirectHandler.WifiTesterBinder) iBinder).getService();
            wifiDirectHandler.setStopDiscoveryAfterGroupFormed(false);
            wifiDirectHandler.setServiceDiscoveryRebroadcastingTime(SERVICE_REBROADCASTING_TIMER);

            boolean isSuperNodeEnabled = Boolean.parseBoolean(UstadMobileSystemImpl.getInstance().getAppPref(
                    PREF_KEY_SUPERNODE, "false", NetworkServiceAndroid.this.getApplicationContext()));
            networkManagerAndroid.setSuperNodeEnabled(NetworkServiceAndroid.this.getApplicationContext(),
                    isSuperNodeEnabled);
        }


        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            wifiDirectHandler = null;
        }
    };




    public class LocalServiceBinder extends Binder {
        public NetworkServiceAndroid getService(){
            return NetworkServiceAndroid.this;
        }

    }

    /**
     *
     * @return instance of the WifiDirectHandler API
     */
    public WifiDirectHandler getWifiDirectHandlerAPI(){
        return wifiDirectHandler;
    }

}