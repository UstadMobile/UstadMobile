package com.ustadmobile.core.networkmanager;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * <h1>NetworkNode</h1>
 *
 * This is a class which defines a single NetworkNode which is an actual representation of a peer device.
 *
 * @author kileha3
 */

public class NetworkNode {

    private String deviceBluetoothMacAddress;

    private String deviceIpAddress;

    private String deviceWifiDirectMacAddress;

    private long wifiDirectLastUpdated;

    private long networkServiceLastUpdated;

    private int port;

    /**
     * List of acquisition operations that have been performed from this node - used by the
     * acquisition task to determine how successful a node has been and avoid nodes that frequently
     * fail
     */
    private List<AcquisitionTaskHistoryEntry> acquisitionTaskHistory;

    /**
     * Creating a NetworkNode
     * @param deviceWifiDirectMacAddress Device Wi-Fi MAC address
     * @param deviceIpAddress Device IP address
     */
    public NetworkNode(String deviceWifiDirectMacAddress,String deviceIpAddress){
        this.deviceWifiDirectMacAddress=deviceWifiDirectMacAddress;
        this.deviceIpAddress=deviceIpAddress;
    }

    /**
     * Method which is used to get NetworkNode's Bluetooth address
     * @return String: Device bluetooth address.
     */
    public String getDeviceBluetoothMacAddress() {
        return deviceBluetoothMacAddress;
    }

    /**
     * Method which is used to set NetworkNode's Bluetooth address
     * @param deviceBluetoothMacAddress Device bluetooth address
     */
    public void setDeviceBluetoothMacAddress(String deviceBluetoothMacAddress) {
        this.deviceBluetoothMacAddress = deviceBluetoothMacAddress;
    }

    /**
     * Method which is used to get NetworkNode's IP address
     * @return String: Device IP address
     */
    public String getDeviceIpAddress() {
        return deviceIpAddress;
    }

    /**
     * Method which is used to set NetworkNode's IP address
     * @param deviceIpAddress Device IP address
     */
    public void setDeviceIpAddress(String deviceIpAddress) {
        this.deviceIpAddress = deviceIpAddress;
    }

    /**
     * Method which is used to get NetworkNode's MAC address
     * @return String: Device MAC address
     */
    public String getDeviceWifiDirectMacAddress() {
        return deviceWifiDirectMacAddress;
    }

    /**
     * Method which is used to set NetworkNode's MAC address
     * @param deviceWifiDirectMacAddress Device MAC address
     */
    public void setDeviceWifiDirectMacAddress(String deviceWifiDirectMacAddress) {
        this.deviceWifiDirectMacAddress = deviceWifiDirectMacAddress;
    }

    /**
     * Method which is used to get the HTTP service port.
     * @return int: HTTP Service port
     */
    public int getPort() {
        return port;
    }

    /**
     * Method which is used to set HTTP service port
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }


    /**
     * Method which used to get last node update time by Wi-Fi Direct service.
     * @return long: Time in milliseconds
     */
    public long getWifiDirectLastUpdated() {
        return wifiDirectLastUpdated;
    }

    /**
     *
     * @return
     */
    public long getTimeSinceWifiDirectLastUpdated() {
        return Calendar.getInstance().getTimeInMillis() - wifiDirectLastUpdated;
    }


    /**
     * Method which is responsible to set time when this node was last updated
     * by Wi-Fi Direct service
     * @param wifiDirectLastUpdated
     */
    public void setWifiDirectLastUpdated(long wifiDirectLastUpdated) {
        this.wifiDirectLastUpdated = wifiDirectLastUpdated;
    }

    /**
     * Method which used to get last node update time by local network service.
     * @return long: Time in milliseconds
     */
    public long getNetworkServiceLastUpdated() {
        return networkServiceLastUpdated;
    }

    /**
     * Method which is responsible to set time when this node was last updated
     * by local network service
     * @param networkServiceLastUpdated time in milliseconds
     */
    public void setNetworkServiceLastUpdated(long networkServiceLastUpdated) {
        this.networkServiceLastUpdated = networkServiceLastUpdated;
    }

    public long getTimeSinceNetworkServiceLastUpdated() {
        return Calendar.getInstance().getTimeInMillis() - networkServiceLastUpdated;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof NetworkNode &&
                ((deviceWifiDirectMacAddress!=null && getDeviceWifiDirectMacAddress().equals(deviceWifiDirectMacAddress))
                || ( deviceIpAddress!=null && getDeviceIpAddress().equals(deviceIpAddress)));
    }

    /**
     * Add the given acqusition history entry to the list of entries acquired from this node.
     *
     * @param entry
     */
    public void addAcquisitionHistoryEntry(AcquisitionTaskHistoryEntry entry) {
        if(acquisitionTaskHistory == null)
            acquisitionTaskHistory = new ArrayList<>();

        acquisitionTaskHistory.add(entry);
    }

    /**
     * Returns the history of acquisition entries downloaded from this node. If no entries have
     * been dowonloaded this will be null.
     *
     * @return List of AcquisitionTaskHistoryEntry downloaded from this node, null if no entries exist
     */
    public List<AcquisitionTaskHistoryEntry> getAcquisitionHistory() {
        return acquisitionTaskHistory;
    }

    public int getNumFailures() {
        if(acquisitionTaskHistory == null)
            return 0;

        int numFailures = 0;
        Iterator<AcquisitionTaskHistoryEntry> historyIterator = acquisitionTaskHistory.iterator();
        AcquisitionTaskHistoryEntry entry;
        while(historyIterator.hasNext()) {
            entry = historyIterator.next();
            if(entry.getStatus() == UstadMobileSystemImpl.DLSTATUS_FAILED)
                numFailures++;

        }

        return numFailures;
    }


}
