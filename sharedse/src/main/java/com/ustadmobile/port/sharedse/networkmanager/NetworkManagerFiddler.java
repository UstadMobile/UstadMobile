package com.ustadmobile.port.sharedse.networkmanager;

/**
 * Created by mike on 6/27/17.
 */

public interface NetworkManagerFiddler {

    void setMangleWifiConnectionSsid(boolean mangleWifiSsid);

    void stopBluetoothServer();

    void startBluetoothServer();

}
