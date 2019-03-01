package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class ConnectivityStatus {

    public static final int STATE_DISCONNECTED = 0;

    public static final int STATE_CONNECTING_LOCAL = 1;

    public static final int STATE_CONNECTED_LOCAL = 2;

    public static final int STATE_METERED = 3;

    public static final int STATE_UNMETERED = 4;

    @UmPrimaryKey
    private int csUid = 1;

    private int connectivityState;

    private String wifiSsid;

    private boolean connectedOrConnecting;

    public ConnectivityStatus(){

    }

    public ConnectivityStatus(int connectivityState, boolean connectedOrConnecting, String wifiSsid) {
        this.connectivityState = connectivityState;
        this.connectedOrConnecting = connectedOrConnecting;
        this.wifiSsid = wifiSsid;
    }

    public int getCsUid() {
        return csUid;
    }

    public void setCsUid(int csUid) {
        this.csUid = csUid;
    }

    public int getConnectivityState() {
        return connectivityState;
    }

    public void setConnectivityState(int connectivityState) {
        this.connectivityState = connectivityState;
    }

    public String getWifiSsid() {
        return wifiSsid;
    }

    public void setWifiSsid(String wifiSsid) {
        this.wifiSsid = wifiSsid;
    }

    public boolean isConnectedOrConnecting() {
        return connectedOrConnecting;
    }

    public void setConnectedOrConnecting(boolean connectedOrConnecting) {
        this.connectedOrConnecting = connectedOrConnecting;
    }

    @Override
    public String toString() {
        String val = "";
        switch(connectivityState) {
            case STATE_METERED:
                val += "METERED";
                break;
            case STATE_UNMETERED:
                val += "UNMETERED";
                break;
            case STATE_DISCONNECTED:
                val += "DISCONNECTED";
                break;
            case STATE_CONNECTED_LOCAL:
                val += "CONNECTED_LOCAL";
                break;
            case STATE_CONNECTING_LOCAL:
                val += "CONNECTING_LOCAL";
                break;

        }

        if(wifiSsid != null){
            val += " SSID = \"" + wifiSsid + "\"";
        }

        val += " connectedOrConnecting = " + connectedOrConnecting;

        return val;
    }
}
