package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.lib.db.entities.NetworkNode;

import java.util.List;

/**
 * This is an abstract class which is used to implement platform specific BleEntryStatus
 *
 * @see BleMessageResponseListener
 * @see Runnable
 * @author kileha3
 */
public abstract class BleEntryStatusTask implements Runnable,BleMessageResponseListener {

    /**
     * Message object which carries list of entry Ids to be checked for availability.
     */
    public BleMessage message;

    private NetworkNode networkNode;

    /**
     * Constructor which will be used when creating new instance of a task
     * @param context Application context.
     * @param entryUidsToCheck List of Id's to be checked for availability from a peer device.
     * @param peerToCheck Peer device for those entries to be checked from.
     */
    public BleEntryStatusTask(Object context,List<Long> entryUidsToCheck, NetworkNode peerToCheck) {
        this.networkNode = peerToCheck;
    }

    /**
     * Handle response from the entry status task
     * @param sourceDeviceAddress Server device bluetooth MAC address
     * @param response Message received as a response from the server device.
     */
    @Override
    public void onResponseReceived(String sourceDeviceAddress,BleMessage response) {
        //TODO: Handle this when DAO for entry status response is ready - Save result to the DB
    }

    /**
     * Get BleMessage instance
     * @return Created BleMessage
     */
    public BleMessage getMessage() {
        return message;
    }

    /**
     * Get NetworkNode instance
     * @return Created NetworkNode
     */
    public NetworkNode getNetworkNode() {
        return networkNode;
    }
}
