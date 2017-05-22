package com.ustadmobile.port.sharedse.networkmanager;

/**
 * Created by kileha3 on 08/05/2017.
 */

public class NetworkNode {
    private String deviceBluetoothMacAddress;
    private String deviceIpAddress;
    private String deviceWifiDirectMacAddress;
    private long wifiDirectLastUpdated;
    private long networkServiceLastUpdated;
    private int port;

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

    public long getWifiDirectLastUpdated() {
        return wifiDirectLastUpdated;
    }

    public void setWifiDirectLastUpdated(long wifiDirectLastUpdated) {
        this.wifiDirectLastUpdated = wifiDirectLastUpdated;
    }

    public long getNetworkServiceLastUpdated() {
        return networkServiceLastUpdated;
    }

    public void setNetworkServiceLastUpdated(long networkServiceLastUpdated) {
        this.networkServiceLastUpdated = networkServiceLastUpdated;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof NetworkNode &&
                ((deviceWifiDirectMacAddress!=null && getDeviceWifiDirectMacAddress().equals(deviceWifiDirectMacAddress))
                || ( deviceIpAddress!=null && getDeviceIpAddress().equals(deviceIpAddress)));
    }
}
