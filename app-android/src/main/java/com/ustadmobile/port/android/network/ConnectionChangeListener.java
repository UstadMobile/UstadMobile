package com.ustadmobile.port.android.network;

import android.util.Log;

import edu.rit.se.wifibuddy.WifiDirectHandler;

/**
 * Created by kileha3 on 28/02/2017.
 */

public class ConnectionChangeListener implements ConnectionChangeCallBacks {
    @Override
    public void onConnected(String SSID) {
        Log.d(WifiDirectHandler.TAG,"ConnectionChangeListener: onConnected - connected to "+SSID);
    }

    @Override
    public void onFailure(String reason) {
        Log.d(WifiDirectHandler.TAG,"ConnectionChangeListener: onFailed - reason "+reason);
    }


}
