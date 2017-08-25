package com.ustadmobile.port.android.netwokmanager;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;

import com.ustadmobile.core.networkmanager.NetworkNode;
import com.ustadmobile.port.sharedse.networkmanager.WiFiDirectGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by mike on 8/21/17.
 */

public class WifiDirectGroupAndroid extends WiFiDirectGroup {

    public WifiDirectGroupAndroid(WifiP2pGroup group) {
        super(group.getNetworkName(), group.getPassphrase());

        Collection<WifiP2pDevice> clients = group.getClientList();
        List<NetworkNode> groupClients = new ArrayList<>();
        if(clients != null) {
            for(WifiP2pDevice device : clients) {
                NetworkNode node = new NetworkNode(device.deviceAddress, null);
                node.setDeviceWifiDirectName(device.deviceName);
                groupClients.add(node);
            }
        }
        setGroupClients(groupClients);

        WifiP2pDevice ownerDevice = group.getOwner();
        if(ownerDevice != null) {
            NetworkNode ownerNode = new NetworkNode(ownerDevice.deviceAddress, null);
            ownerNode.setDeviceWifiDirectName(ownerDevice.deviceName);
            setGroupOwner(ownerNode);
        }




    }
}
