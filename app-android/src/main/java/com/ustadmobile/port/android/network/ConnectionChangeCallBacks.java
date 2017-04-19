package com.ustadmobile.port.android.network;

/**
 * Created by kileha3 on 28/02/2017.
 */

public interface ConnectionChangeCallBacks {
    void onConnected(String SSID);
    void onFailure(String reason);
}
