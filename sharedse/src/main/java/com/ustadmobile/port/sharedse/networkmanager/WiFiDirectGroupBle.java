package com.ustadmobile.port.sharedse.networkmanager;

/**
 * Class which defines WiFi Direct group in a cross platform way.
 *
 * @author kileha3
 */

public class WiFiDirectGroupBle{

    private String ssid;

    private String passphrase;

    private int port;

    private String ipAddress;

    /**
     * Create Wi-Fi Direct group
     * @param ssid Group SSID
     * @param passphrase Group passphrase
     */
    public WiFiDirectGroupBle(String ssid, String passphrase) {
        this.ssid = ssid;
        this.passphrase = passphrase;
    }

    /**
     * @return WiFi direct group SSID
     */
    public String getSsid() {
        return ssid;
    }

    /**@return Wifi direct group passphrase
     */
    public String getPassphrase() {
        return passphrase;
    }

    /**
     * @return Node ip address
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * @return Node listening port
     */
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

}
