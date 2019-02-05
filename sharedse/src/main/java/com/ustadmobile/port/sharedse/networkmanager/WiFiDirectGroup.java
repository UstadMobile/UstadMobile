package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.lib.db.entities.NetworkNode;

import java.util.List;

/**
 * This is a class which define Wi-Fi Direct group in a cross platform way.
 *
 * @author mike
 * @author kileha3
 */

public class WiFiDirectGroup {

    private String ssid;

    private String passphrase;

    private List<NetworkNode> groupClients;

    private NetworkNode groupOwner;

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

    @Deprecated
    public boolean isOwner() {
        return owner;
    }

    @Deprecated
    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    @Deprecated
    public List<NetworkNode> getGroupClients() {
        return groupClients;
    }

    @Deprecated
    protected void setGroupClients(List<NetworkNode> groupClients) {
        this.groupClients = groupClients;
    }

    @Deprecated
    public NetworkNode getGroupOwner() {
        return groupOwner;
    }

    @Deprecated
    protected void setGroupOwner(NetworkNode groupOwner) {
        this.groupOwner = groupOwner;
    }

    @Deprecated
    public boolean groupIncludes(String deviceMacAddr){
        NetworkNode owner = getGroupOwner();
        if(owner != null && owner.getWifiDirectMacAddress() != null
                && owner.getWifiDirectMacAddress().equalsIgnoreCase(deviceMacAddr))
            return true;

        return NetworkManager.isMacAddrInList(getGroupClients(), deviceMacAddr);
    }
}
