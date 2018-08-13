package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.networkmanager.NetworkManagerCoreBle;
import com.ustadmobile.core.networkmanager.NetworkManagerTaskListener;
import com.ustadmobile.lib.db.entities.NetworkNode;

import java.util.List;

/**
 * <h1>NetworkManagerBle</h1>
 *
 * This is the class which defines all cross platform Network logical flows.
 * It is responsible to register all network listeners,register all services,
 * start/stop monitoring entry status availability and handling WiFi communications.
 *
 * @author kileha3
 *
 * @see NetworkManagerTaskListener
 * @see com.ustadmobile.core.networkmanager.NetworkManagerCore
 */

public class NetworkManagerBle extends NetworkManagerCoreBle {

    /**
     * Flag to indicate entry status request
     */
    public static final byte ENTRY_STATUS_REQUEST = (byte) 111;

    /**
     * Flag to indicate entry status response
     */
    public static final byte ENTRY_STATUS_RESPONSE = (byte) 112;

    /**
     * Flag to indicate WiFi direct group creation request
     */
    public static final byte WIFI_GROUP_CREATION_REQUEST = (byte) 113;

    /**
     * Flag to indicate WiFi direct group creation response
     */
    public static final byte WIFI_GROUP_CREATION_RESPONSE = (byte) 114;


    /**
     * Check if WiFi is enabled / disabled on the device
     * @return boolean: TRUE, if enabled otherwise FALSE.
     */
    @Override
    public boolean isWiFiEnabled() {
        return false;
    }

    /**
     * This should be called by the platform implementation when BLE discovers a nearby device
     *
     * @param node The nearby device discovered
     */
    protected void handleNodeDiscovered(NetworkNode node) {

    }

    /**
     * Enable of disable WiFi on the device
     *
     * @param enabled Enable when true otherwise disable
     * @return true if the operation is successful, false otherwise
     */
    @Override
    public boolean setWifiEnabled(boolean enabled) {
        return false;
    }

    /**
     * Start monitoring availability of specific entries from peer devices
     * @param monitor Monitor which can be Presenter or
     * @param entryUidsToMonitor List of entries to be monitored
     */
    @Override
    public void startMonitoringAvailability(Object monitor, List<Long> entryUidsToMonitor) {

    }

    /**
     * Stop monitoring the availability of entries from peer devices
     * @param monitor Monitor object which created a monitor (e.g Presenter)
     */
    @Override
    public void stopMonitoringAvailability(Object monitor) {

    }

    /**
     * Create entry status task for a specific peer device,
     * it will request status of the provided entries from the provided peer device
     * @param entryUidsToCheck List of entries to be checked from the peer device
     * @param peerToCheck Peer device to request from
     * @return Created BleEntryStatusTask
     *
     * @see BleEntryStatusTask
     */
    protected BleEntryStatusTask makeEntryStatusTask(List<Long> entryUidsToCheck, NetworkNode peerToCheck) {
        return null;
    }
}
