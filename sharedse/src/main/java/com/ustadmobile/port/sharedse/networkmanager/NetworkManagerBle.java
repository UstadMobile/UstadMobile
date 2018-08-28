package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.NetworkNodeDao;
import com.ustadmobile.lib.db.entities.NetworkNode;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * This is an abstract class which is used to implement platform specific NetworkManager
 *
 * @author kileha3
 *
 */

public abstract class NetworkManagerBle {

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


    private final Object knownNodesLock = new Object();

    private Object mContext;

    /**
     * Do the main initialization of the NetworkManager : set the mContext
     *
     * @param context The mContext to use for the network manager
     */
    public synchronized void init(Object context){
        this.mContext = context;
    }


    /**
     * Check if WiFi is enabled / disabled on the device
     * @return boolean true, if enabled otherwise false.
     */
    public abstract boolean isWiFiEnabled();


    /**
     * Check if the device is Bluetooth Low Energy capable
     * @return True is capable otherwise false
     */
    public abstract boolean isBleCapable();


    /**
     * Check if bluetooth is enabled on the device
     * @return True if enabled otherwise false
     */
    public abstract boolean isBluetoothEnabled();

    /**
     * Check if the device can create BLE service and advertise it to the peer devices
     * @return true if can advertise its service else false
     */
    public abstract boolean canDeviceAdvertise();

    /**
     * Start advertising BLE service to the peer devices
     * <b>Use case</b>
     * When this method called, it will create BLE service and start advertising it.
     */
    public abstract void startAdvertising();

    /**
     * Stop advertising the service which was created and advertised by {@link NetworkManagerBle#startAdvertising()}
     */
    public abstract void stopAdvertising();

    /**
     * Start scanning for the peer devices whose services are being advertised
     */
    public abstract void startScanning();

    /**
     * Stop scanning task which was started by {@link NetworkManagerBle#startScanning()}
     */
    public abstract void stopScanning();


    /**
     * This should be called by the platform implementation when BLE discovers a nearby device
     *
     * @param node The nearby device discovered
     */
    protected void handleNodeDiscovered(NetworkNode node) {

        NetworkNode networkNode;
        boolean isNewNode = false;

        synchronized (knownNodesLock){
            NetworkNodeDao networkNodeDao = UmAppDatabase.getInstance(mContext).getNetworkNodeDao();
            networkNode = networkNodeDao.findNodeByBluetoothAddress(node.getBluetoothMacAddress());

            if(networkNode == null) {
                networkNode = node;
                isNewNode = true;
            }

            networkNode.setWifiDirectLastUpdated(Calendar.getInstance().getTimeInMillis());

            if(isNewNode) {
                networkNodeDao.insert(networkNode);
            }else {
                networkNodeDao.update(networkNode);
            }
        }


    }

    /**
     * Open bluetooth setting section from setting panel
     */
    public abstract void openBluetoothSettings();

    /**
     * Enable or disable WiFi on the device
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
     *
     * @param wiFiDirectGroupListener Listener for group creation task
     */
    public abstract void createWifiDirectGroup(WiFiDirectGroupListenerBle wiFiDirectGroupListener);

    /**
     * Start monitoring availability of specific entries from peer devices
     * @param monitor Monitor which can be Presenter or
     * @param entryUidsToMonitor List of entries to be monitored
     */
    public void startMonitoringAvailability(Object context,Object monitor, List<Long> entryUidsToMonitor) {
        //TODO: Implement this when Db is ready - check if there are pending task left in the Db
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
     * @param context Platform specific mContext
     * @param entryUidsToCheck List of entries to be checked from the peer device
     * @param peerToCheck Peer device to request from
     * @return Created BleEntryStatusTask
     *
     * @see BleEntryStatusTask
     */
    protected BleEntryStatusTask makeEntryStatusTask(Object context,List<Long> entryUidsToCheck, NetworkNode peerToCheck){
        return null;
    }

    /**
     * Clean up the network manager for shutdown
     */
    public void onDestroy(){
        mContext = null;
    }
}
