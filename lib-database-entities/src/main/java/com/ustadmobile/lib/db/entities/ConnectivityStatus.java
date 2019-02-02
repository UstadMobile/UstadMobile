package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

public class ConnectivityStatus {

    public static final int STATE_DISCONNECTED = 0;

    public static final int STATE_CONNECTING_LOCAL = 1;

    public static final int STATE_CONNECTED_LOCAL = 2;

    public static final int STATE_METERED = 3;

    public static final int STATE_UNMETERED = 4;

    @UmPrimaryKey
    private int csUid;

    private int connectivityState;

    private String wifiSsid;

    private boolean connectedOrConnecting;


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
}
