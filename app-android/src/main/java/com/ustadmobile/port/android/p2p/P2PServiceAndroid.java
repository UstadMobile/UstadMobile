package com.ustadmobile.port.android.p2p;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ustadmobile.port.sharedse.p2p.P2PActionListener;

/**
 * Created by fabio on 30/01/2016.
 */
public class P2PServiceAndroid extends Service {
    private P2PManagerAndroid p2PManagerAndroid;
    private WifiP2pManager.Channel channel;
    private WifiP2pManager manager;



    // Binder given to clients
    private final IBinder mBinder = new LocalServiceBinder();
    WiFiDeviceChangeBroadcastReceiver receiver;

    public P2PServiceAndroid(){}


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(receiver!=null){
            unregisterReceiver(receiver);
        }
        p2PManagerAndroid.stop();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        receiver = new WiFiDeviceChangeBroadcastReceiver(manager, channel, this);

        manager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this,getMainLooper(), null);
        p2PManagerAndroid=new P2PManagerAndroid(this,manager,channel);
        p2PManagerAndroid.start();
        p2PManagerAndroid.setSuperNodeEnabled(true);
        p2PManagerAndroid.init(new P2PActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int errorCode) {

            }
        });

        registerReceiver(receiver, intentFilter);

        return mBinder;
    }


    public class LocalServiceBinder extends Binder {

        public P2PServiceAndroid getService(){
            return P2PServiceAndroid.this;
        }

    }

    public P2PManagerAndroid getP2PManager() {
        return p2PManagerAndroid;
    }


    public boolean isRunning(){

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }




}