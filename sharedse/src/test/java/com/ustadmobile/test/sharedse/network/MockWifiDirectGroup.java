package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.port.sharedse.networkmanager.WiFiDirectGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mike on 8/23/17.
 */

public class MockWifiDirectGroup  extends  WiFiDirectGroup{

    private MockNetworkManager ownerNetworkManager;

    private List<MockNetworkManager> groupClients;

    public MockWifiDirectGroup(MockNetworkManager owner, String ssid, String passphrase) {
        super(ssid, passphrase);
        super.setGroupOwner(owner.getThisWifiDirectDevice());
        groupClients = new ArrayList<>();
        owner.setWifiDirectGroup(this);
        this.ownerNetworkManager = owner;
    }

    public void addClient(MockNetworkManager client) {
        groupClients.add(client);
        client.setWifiDirectGroup(this);
    }

    public void removeClient(MockNetworkManager client) {
        groupClients.remove(client);
        client.setWifiDirectGroup(null);
    }

    public String getGroupOwnerIp() {
        return ownerNetworkManager.getWifiDirectIpAddress();
    }

}
