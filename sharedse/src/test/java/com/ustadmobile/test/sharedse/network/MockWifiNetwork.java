package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;


/**
 * Represents a Wifi Network for test mocking purposes. It maintains a list of devices and IP addresses
 * connected and will 'broadcast' network service discovery messsages to other nodes.
 *
 * Created by mike on 5/26/17.
 */
public class MockWifiNetwork {

    //Map IP address -> device
    private Map<String, MockNetworkManager> connectedDevices;

    private String passphrase;

    private String ssid;

    /**
     * Creates a new mock wifi network
     *
     * @param ssid Wireless SSID
     * @param passphrase Wireless passphrase
     */
    public MockWifiNetwork(String ssid, String passphrase){
        this.passphrase = passphrase;
        this.ssid = ssid;
        connectedDevices = new Hashtable<>();
    }

    /**
     * "Connects" a device to this network.
     *
     * @param device The device being connected
     * @param ssid The ssid being presented by the device
     * @param passphrase The passphrase being presented by the device
     *
     * @return The allocated IP address if ssid and passphrase were valid, otherwise null
     */
    public synchronized String connect(MockNetworkManager device, String ssid, String passphrase)  {
        if(ssid == null || !ssid.equals(this.ssid))
            return null;

        if(passphrase == null || !passphrase.equals(this.passphrase))
            return null;

        //OK to join
        String ipAddr = MockNetworkManager.makeNextMockIpAddr();
        connectedDevices.put(ipAddr, device);
        return ipAddr;
    }

    /**
     * Disconnects the given device from the network: remove it's ip from the list and don't send
     * it broadcasts anymore
     *
     * @param device Device to remove
     * @return true if the device was on the network and is now removed, false otherwise
     */
    public synchronized boolean disconnect(MockNetworkManager device) {
        String currentDeviceIp = getIpForDevice(device);
        if(currentDeviceIp != null) {
            connectedDevices.remove(device);
            return true;
        }

        return false;
    }

    private String getIpForDevice(MockNetworkManager device){
        Set<String> ipAddrSet = connectedDevices.keySet();
        String[] allIps = ipAddrSet.toArray(new String[ipAddrSet.size()]);
        NetworkManager currentDevice;
        for(String currentIp : allIps) {
            currentDevice = connectedDevices.get(currentIp);
            if(currentDevice == device) {
                return currentIp;
            }

        }

        return null;
    }


    /**
     * Get the SSID of the network
     * @return SSID of the network
     */
    public String getSsid() {
        return ssid;
    }

    /**
     * Used to force a particular device to change IP on this network. This is used for the maain
     * test device and remote test slave device.
     *
     * @param device Device to set an IP address for
     * @param ipAddr The IP address to allocate
     *
     * @return
     */
    public synchronized boolean setDeviceIpAddr(MockNetworkManager device, String ipAddr) {
        String currentDeviceIp = getIpForDevice(device);
        if(currentDeviceIp != null){
            connectedDevices.remove(currentDeviceIp);
            connectedDevices.put(ipAddr, device);
            device.setMockDeviceIpAddress(ipAddr);
            return true;
        }

        return false;
    }

    /**
     * Send a wireless service broadcast out to all devices on this wifi network to mock service
     * discovery
     *
     * @param serviceName Service name to broadcast
     * @param senderIp The ip address of the device sending the broadcast
     * @param port The port being advertised for the service
     */
    public synchronized void sendWirelessServiceBroadcast(String serviceName, String senderIp, int port) {
        for(String currentIp : connectedDevices.keySet()) {
            if(!currentIp.equals(senderIp) && connectedDevices.get(currentIp).isNetworkServiecDiscoveryEnabled())
                connectedDevices.get(currentIp).handleNetworkServerDiscovered(serviceName, senderIp,
                    port);
        }
    }


}
