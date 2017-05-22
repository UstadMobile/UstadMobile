package com.ustadmobile.port.sharedse.networkmanager;

/**
 * Created by kileha3 on 08/05/2017.
 */

public class NetworkNode {
    private String deviceBluetoothMacAddress;
    private String deviceIpAddress;
    private String deviceWifiDirectMacAddress;
    private int port;
    private long lastUpdated;

    public NetworkNode(String deviceWifiDirectMacAddress,String deviceIpAddress){
        this.deviceWifiDirectMacAddress=deviceWifiDirectMacAddress;
        this.deviceIpAddress=deviceIpAddress;
    }


    public String getDeviceBluetoothMacAddress() {
        return deviceBluetoothMacAddress;
    }

    public void setDeviceBluetoothMacAddress(String deviceBluetoothMacAddress) {
        this.deviceBluetoothMacAddress = deviceBluetoothMacAddress;
    }

    public String getDeviceIpAddress() {
        return deviceIpAddress;
    }

    public void setDeviceIpAddress(String deviceIpAddress) {
        this.deviceIpAddress = deviceIpAddress;
    }

    public String getDeviceWifiDirectMacAddress() {
        return deviceWifiDirectMacAddress;
    }

    public void setDeviceWifiDirectMacAddress(String deviceWifiDirectMacAddress) {
        this.deviceWifiDirectMacAddress = deviceWifiDirectMacAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof NetworkNode && (getDeviceWifiDirectMacAddress().equals(deviceWifiDirectMacAddress)
                || getDeviceIpAddress().equals(deviceIpAddress));
    }
}
