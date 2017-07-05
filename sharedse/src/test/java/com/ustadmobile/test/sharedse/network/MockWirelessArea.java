package com.ustadmobile.test.sharedse.network;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/**
 * This class represents a wireless area (e.g. range). It keeps a list of bluetooth addresses of
 * mock network manager instances around, sends out Wifi direct service advertisements and
 * has keeps a list of wireless networks.
 *
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
            if(devices.get(i) != sender && devices.get(i).isWifiDirectDiscoveryEnabled()) {
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

    public MockWifiNetwork getWifiNetwork(String ssid) {
        return mockWifiNetworks.get(ssid);
    }

    /**
     * Handle a device that wants to "connect" to a wireless network in this range.
     *
     * @param device Device that wants to connect
     * @param ssid SSID it wants to connect to
     * @param passphrase Passphrase it is using to connect
     *
     * @return
     */
    public String connectDeviceToWifiNetwork(MockNetworkManager device, String ssid, String passphrase) {
        MockWifiNetwork network = mockWifiNetworks.get(ssid);
        if(network == null)
            return null;

        return network.connect(device, ssid, passphrase);
    }

    public MockNetworkManager getDeviceByBluetoothAddr(String bluetoothAddr) {
        for(MockNetworkManager device: devices) {
            if(device.getMockBluetoothAddr() != null && device.getMockBluetoothAddr().equals(bluetoothAddr))
                return device;
        }

        return null;
    }




}
