package com.ustadmobile.port.sharedse.networkmanager;

/**
 * Created by mike on 5/23/17.
 */

public class WiFiDirectGroup {

    private String ssid;

    private String passphrase;

    public WiFiDirectGroup(String ssid, String passphrase) {
        this.ssid = ssid;
        this.passphrase = passphrase;
    }

    public String getSsid() {
        return ssid;
    }

    public String getPassphrase() {
        return passphrase;
    }
}
