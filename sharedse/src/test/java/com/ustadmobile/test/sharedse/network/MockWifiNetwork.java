package com.ustadmobile.test.sharedse.network;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/**
 * Created by mike on 5/26/17.
 */

public class MockWifiNetwork {

    //Map IP address -> device
    private Map<String, MockNetworkManager> connectedDevices;

    private String passphrase;

    private String ssid;

    public MockWifiNetwork(String passphrase, String ssid){
        this.passphrase = passphrase;
        this.ssid = ssid;
        connectedDevices = new Hashtable<>();
    }

    public String connect(MockNetworkManager device, String ssid, String passphrase, String staticIp) {
        if(ssid == null || !ssid.equals(this.ssid))
            return null;

        if(passphrase == null || !passphrase.equals(this.passphrase))
            return null;

        //OK to join
        String ipAddr = staticIp != null ? staticIp : MockNetworkManager.makeNextMockIpAddr();
        connectedDevices.put(ipAddr, device);
        return ipAddr;
    }

    public String getSsid() {
        return ssid;
    }




}
