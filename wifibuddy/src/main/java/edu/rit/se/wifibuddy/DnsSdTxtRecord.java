package edu.rit.se.wifibuddy;

import android.net.wifi.p2p.WifiP2pDevice;

import java.util.Map;

/**
 * A class for storing a Bonjour TXT record.
 */
public class DnsSdTxtRecord {

    private String fullDomain;
    private Map<String, String> record;
    private WifiP2pDevice device;

    public DnsSdTxtRecord(String fullDomain, Map<String, String> record, WifiP2pDevice device) {
        this.fullDomain = fullDomain;
        this.record = record;
        this.device = device;
    }

    public Map getRecord() {
        return record;
    }

    /**
     * The full domain of the service for which txt records were discovered
     *
     * @return
     */
    public String getFullDomain() {
        return fullDomain;
    }

    /**
     * Device details which has extra device information e.g status.
     *
     * @return
     */
    public WifiP2pDevice getDevice(){
        return device;
    }
}
