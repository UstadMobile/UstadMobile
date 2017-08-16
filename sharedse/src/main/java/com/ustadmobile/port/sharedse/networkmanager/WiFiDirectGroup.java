package com.ustadmobile.port.sharedse.networkmanager;

/**
 * <h1>WiFiDirectGroup</h1>
 *
 * This is a class which define Wi-Fi Direct group in a cross platform way.
 *
 * @author mike
 */

public class WiFiDirectGroup {

    private String ssid;

    private String passphrase;

    private boolean owner;

    /**
     * Create Wi-Fi Direct group
     * @param ssid Group SSID
     * @param passphrase Group passphrase
     */
    public WiFiDirectGroup(String ssid, String passphrase) {
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

    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }
}
