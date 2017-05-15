package com.ustadmobile.test.sharedse.network;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by mike on 5/14/17.
 */

public class MockWifiDirectNetwork {

    private Vector<MockRemoteDevice> remoteDevices;

    public MockWifiDirectNetwork() {
        remoteDevices = new Vector<>();
    }

    public void sendSdTxtRecords(String serviceName, HashMap txtRecords, MockNetworkManager sender) {
        for(int i = 0; i < remoteDevices.size(); i++) {
            if(remoteDevices.get(i).getNetworkManager() != sender) {

            }
        }
    }


}
