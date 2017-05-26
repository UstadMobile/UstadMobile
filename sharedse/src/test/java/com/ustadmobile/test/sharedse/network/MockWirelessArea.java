package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/**
 * Created by mike on 5/14/17.
 */

public class MockWirelessArea {

    private Vector<MockNetworkManager> devices;

    private Map<String, MockWifiNetwork> mockWifiNetworks;

    public MockWirelessArea() {
        devices = new Vector<>();
        mockWifiNetworks = new Hashtable<>();
    }

    public void addDevice(MockNetworkManager device){
        devices.add(device);
    }

    public void removeDevice(MockNetworkManager device) {
        devices.remove(device);
    }

    public void sendSdTxtRecords(String serviceName, HashMap txtRecords, MockNetworkManager sender) {
        for(int i = 0; i < devices.size(); i++) {
            if(devices.get(i) != sender) {
                devices.get(i).handleWifiDirectSdTxtRecordsAvailable(
                        serviceName, sender.getWifiDirectMacAddr(), txtRecords);
            }
        }
    }

    public void addWifiNetwork(MockWifiNetwork network) {
        mockWifiNetworks.put(network.getSsid(), network);
    }

    public void removeWifiNetwork(MockWifiNetwork network) {
        mockWifiNetworks.remove(network.getSsid());
    }

    /**
     * "Connect"
     *
     * @param device
     * @param ssid
     * @param passphrase
     * @param staticIp
     *
     * @return
     */
    public String connectDeviceToWifiNetwork(MockNetworkManager device, String ssid, String passphrase, String staticIp) {
        MockWifiNetwork network = mockWifiNetworks.get(ssid);
        return network.connect(device, ssid, passphrase, staticIp);
    }

    public MockNetworkManager getDeviceByBluetoothAddr(String bluetoothAddr) {
        for(MockNetworkManager device: devices) {
            if(device.getMockBluetoothAddr() != null && device.getMockBluetoothAddr().equals(bluetoothAddr))
                return device;
        }

        return null;
    }




}
