package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.networkmanager.NetworkManagerCoreBle;
import com.ustadmobile.core.networkmanager.NetworkManagerTaskListener;
import com.ustadmobile.lib.db.entities.NetworkNode;

import java.util.List;
import java.util.UUID;

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

public abstract class NetworkManagerBle extends NetworkManagerCoreBle {

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
     * Commonly used MTU for android devices
     */
    public static final int DEFAULT_MTU = 20;

    /**
     * Bluetooth Low Energy service UUID for our app
     */
    public static final UUID USTADMOBILE_BLE_SERVICE_UUID = UUID.fromString("7d2ea28a-f7bd-485a-bd9d-92ad6ecfe93e");

    /**
     * Do the main initialization of the NetworkManager : set the context
     *
     * @param mContext The context to use for the network manager
     */
    public abstract void init(Object mContext);
    /**
     * Check if WiFi is enabled / disabled on the device
     * @return boolean: TRUE, if enabled otherwise FALSE.
     */
    public abstract boolean isWiFiEnabled();

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
    public abstract boolean setWifiEnabled(boolean enabled);


    /**
     * Create a new WiFi direct group on this device. A WiFi direct group
     * will create a new SSID and passphrase other devices can use to connect in "legacy" mode.
     *
     * The process is asynchronous and the WifiDirectGroupListener should be used to listen for
     * group creation.
     *
     * If a WiFi direct group is already under creation this method has no effect.
     */
    public abstract void createWifiDirectGroup();

    /**
     * Start monitoring availability of specific entries from peer devices
     * @param monitor Monitor which can be Presenter or
     * @param entryUidsToMonitor List of entries to be monitored
     */
    public void startMonitoringAvailability(Object context,Object monitor, List<Long> entryUidsToMonitor) {
         BleEntryStatusTask entryStatusTask= makeEntryStatusTask(context,entryUidsToMonitor,null);
         entryStatusTask.run();
    }

    /**
     * Stop monitoring the availability of entries from peer devices
     * @param monitor Monitor object which created a monitor (e.g Presenter)
     */
    public void stopMonitoringAvailability(Object monitor) {

    }

    /**
     * Create entry status task for a specific peer device,
     * it will request status of the provided entries from the provided peer device
     * @param context Platform specific context
     * @param entryUidsToCheck List of entries to be checked from the peer device
     * @param peerToCheck Peer device to request from
     * @return Created BleEntryStatusTask
     *
     * @see BleEntryStatusTask
     */
    protected BleEntryStatusTask makeEntryStatusTask(Object context,List<Long> entryUidsToCheck, NetworkNode peerToCheck){
        return null;
    }
}
