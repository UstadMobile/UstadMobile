package com.ustadmobile.port.android.netwokmanager;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;

import java.util.concurrent.TimeUnit;

import edu.rit.se.wifibuddy.WifiDirectHandler;


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

    private WifiDirectHandler wifiDirectHandler;

    private final IBinder mBinder = new LocalServiceBinder();

    private NetworkManagerAndroid networkManagerAndroid;


    private NetworkManagerAndroidBle managerAndroidBle;

    /**
     * Default time interval for Wi-Fi Direct service rebroadcasting.
     */
    public static final int SERVICE_REBROADCASTING_TIMER = 120000;

    public NetworkServiceAndroid() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //TODO: This have to go
        networkManagerAndroid = (NetworkManagerAndroid)
                UstadMobileSystemImplAndroid.getInstanceAndroid().getNetworkManager();
       networkManagerAndroid.init(NetworkServiceAndroid.this);

        managerAndroidBle = (NetworkManagerAndroidBle)
                UstadMobileSystemImplAndroid.getInstanceAndroid().getNetworkManagerBle();
        managerAndroidBle.init(NetworkServiceAndroid.this);

        //Bind WifiService
        Intent wifiServiceIntent = new Intent(this, WifiDirectHandler.class);
        bindService(wifiServiceIntent, wifiP2PServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        networkManagerAndroid.onDestroy();
        managerAndroidBle.onDestroy();
        if(wifiDirectHandler!=null){
            wifiDirectHandler.removeGroup();
            UstadMobileSystemImpl.getInstance().setAppPref("devices",
                    "", getApplicationContext());
        }
        unbindService(wifiP2PServiceConnection);

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
     * This is an interface for monitoring the state of an application service.
     * it defines callbacks for service binding, passed to bindService().
     * Either of the two methods will be invoked:
     * <p>
     * <b>onServiceConnected</b>: Invoked when service successfully connected
     * <b>onServiceDisconnected</b>: Invoked when service connection failed.
     * </p>
     */
    ServiceConnection wifiP2PServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            //TODO: Have to go
            wifiDirectHandler = ((WifiDirectHandler.WifiTesterBinder) iBinder).getService();

            if(managerAndroidBle.isBluetoothEnabled() && managerAndroidBle.isBleCapable()){
                if(managerAndroidBle.canDeviceAdvertise()){
                    managerAndroidBle.startAdvertising();
                    /*Wait for 3 seconds before starting service discovery, it wont be happy when
                    both start at the same time*/
                    new Handler().postDelayed(() -> managerAndroidBle.startScanning(),
                            TimeUnit.SECONDS.toMillis(3));
                }else{
                    managerAndroidBle.startScanning();
                }
            }else{
                if(!managerAndroidBle.isBluetoothEnabled()){
                    managerAndroidBle.openBluetoothSettings();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            wifiDirectHandler = null;
        }
    };

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we won't be dealing with IPC.
     */
    public class LocalServiceBinder extends Binder {
        public NetworkServiceAndroid getService() {
            return NetworkServiceAndroid.this;
        }

    }

    /**
     * @return WifiDirectHandler: Instance of the WifiDirectHandler from Wi-Fi buddy API
     */
    @Deprecated
    public WifiDirectHandler getWifiDirectHandlerAPI(){
        return wifiDirectHandler;

    }

}