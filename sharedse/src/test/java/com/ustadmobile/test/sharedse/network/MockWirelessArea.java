package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.core.buildconfig.CoreBuildConfig;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by mike on 5/14/17.
 */

public class MockWirelessArea {

    private Vector<MockRemoteDevice> devices;

    public MockWirelessArea() {
        devices = new Vector<>();
    }

    public void addDevice(MockRemoteDevice device){
        devices.add(device);
    }

    public void removeDevice(MockRemoteDevice device) {
        devices.remove(device);
    }

    public void sendSdTxtRecords(String serviceName, HashMap txtRecords, MockNetworkManager sender) {
        for(int i = 0; i < devices.size(); i++) {
            if(devices.get(i).getNetworkManager() != sender) {
                devices.get(i).getNetworkManager().handleWifiDirectSdTxtRecordsAvailable(
                        serviceName, sender.getWifiDirectMacAddr(), txtRecords);
            }
        }
    }


}
