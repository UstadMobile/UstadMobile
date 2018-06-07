package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.core.networkmanager.NetworkNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
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

    private static final int PEER_DISCOVERY_TIMER_WAIT = 1000;

    private static final int PEER_DISCOVERY_TIMER_INTERVAL = 30000;

    private Timer peerDiscoveryTimer;

    private TimerTask peerDiscoveryTimerTask = new TimerTask() {
        @Override
        public void run() {
//            ArrayList<MockNetworkManager> peers;
//            synchronized (devices) {
//                for(int i = 0; i < devices.size(); i++) {
//                    peers = new ArrayList<>(devices);
//                    peers.remove(devices.get(i));
//
//                    ArrayList<NetworkNode> nodePeers = new ArrayList<>(peers.size());
//                    for(MockNetworkManager manager : peers) {
//                        nodePeers.add(manager.getThisWifiDirectDevice());
//                    }
//
//                    devices.get(i).handleWifiDirectPeersChanged(nodePeers);
//                }
//            }
        }
    };

    public MockWirelessArea() {
        devices = new Vector<>();
        mockWifiNetworks = new Hashtable<>();
        peerDiscoveryTimer = new Timer("Mock wireless area- peer discovery timer", true);
        peerDiscoveryTimer.scheduleAtFixedRate(peerDiscoveryTimerTask, PEER_DISCOVERY_TIMER_WAIT,
                PEER_DISCOVERY_TIMER_INTERVAL);
    }

    public void addDevice(MockNetworkManager device){
        devices.add(device);
    }

    public void removeDevice(MockNetworkManager device) {
        devices.remove(device);
    }

    public void sendSdTxtRecords(String serviceName, HashMap txtRecords, MockNetworkManager sender) {
        synchronized (devices) {
            for(int i = 0; i < devices.size(); i++) {
                if(devices.get(i) != sender && devices.get(i).isWifiDirectDiscoveryEnabled()) {
                    devices.get(i).handleWifiDirectSdTxtRecordsAvailable(
                            serviceName, sender.getWifiDirectMacAddr(), txtRecords);
                }
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
     * @return null if no such wifi network exists. Throw an IOException if the network exists but passphrase is invalid
     */
    public String connectDeviceToWifiNetwork(MockNetworkManager device, String ssid, String passphrase)  {
        MockWifiNetwork network = mockWifiNetworks.get(ssid);
        if(network == null)
            return null;

        return network.connect(device, ssid, passphrase);
    }

    public MockNetworkManager getDeviceByBluetoothAddr(String bluetoothAddr) {
        synchronized (devices) {
            for(MockNetworkManager device: devices) {
                if(device.getMockBluetoothAddr() != null && device.getMockBluetoothAddr().equals(bluetoothAddr))
                    return device;
            }
        }

        return null;
    }

    public MockNetworkManager getDeviceByWifiDirectMacAddr(String wifiDirectMacAddr) {
        synchronized (devices) {
            String deviceMacAddr;
            for(MockNetworkManager device: devices) {
                deviceMacAddr = device.getWifiDirectMacAddr();
                if(deviceMacAddr != null && deviceMacAddr.equalsIgnoreCase(wifiDirectMacAddr)) {
                    return device;
                }
            }
        }

        return null;
    }




}
