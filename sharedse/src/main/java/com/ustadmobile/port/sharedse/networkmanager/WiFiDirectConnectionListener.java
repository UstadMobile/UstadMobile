package com.ustadmobile.port.sharedse.networkmanager;

/**
 * Interface which listens for the WiFi P2P connection states
 */
public interface WiFiDirectConnectionListener {

    /**
     * Invoked when connection is made betwen group owner and client.
     */
    void onConnected(String ipAddress, String groupSSID);

    /**
     * Invoked when client fails to connection to the group owner.
     */
    void onFailure(String groupSSID);
}
