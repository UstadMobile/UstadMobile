package com.ustadmobile.port.sharedse.networkmanager;

/**
 * Class which define Wi-Fi Direct group in a cross platform way.
 *
 * @author kileha3
 */

public class WiFiDirectGroupBle{

    private String ssid;

    private String passphrase;

    private int port;

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
     * Method which is used to get Group SSID
     * @return String: Group SSID
     */
    public String getSsid() {
        return ssid;
    }

    /**
     * Method which is used to get group pasp
     * @return String: group passphrase
     */
    public String getPassphrase() {
        return passphrase;
    }

    /**
     * Get currently active port
     * @return active port number
     */
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
