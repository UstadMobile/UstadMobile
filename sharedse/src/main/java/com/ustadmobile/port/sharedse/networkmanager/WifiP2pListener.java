package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.networkmanager.NetworkNode;

import java.util.List;

/**
 * Created by mike on 8/14/17.
 */

public interface WifiP2pListener {

    /**
     * Fired when the list of other devices around nearby has changed
     *
     * @param peers List of peers available. NetworkNode objects with the hardware address and
     *              wifi direct mac address fields.
     */
    void peersChanged(List<NetworkNode> peers);

    /**
     * Fired when the device is connected, disconnected, or when another peers joins the group.
     */
    void wifiP2pConnectionChanged(boolean connected);


}
