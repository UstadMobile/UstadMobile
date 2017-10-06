package com.ustadmobile.core.networkmanager;

/**
 * <h1>NetworkManagerListener</h1>
 *
 * This is an interface which will be listening and managing all network activities and
 * fire events accordingly.
 *
 * @author kileha3
 */

public interface NetworkManagerListener extends NetworkManagerTaskListener{

    /**
     * Indicate that entry status check status task has information about the entry.
     * @param fileIds List of all entry under process.
     */
    void fileStatusCheckInformationAvailable(String[] fileIds);

    /**
     * Indicate that new NetworkNode has been discovered
     * @param node Discovered NetworkNode
     */
    void networkNodeDiscovered(NetworkNode node);

    /**
     * Indicate that NetworkNode information has been updated
     * @param node Updated node
     */
    void networkNodeUpdated(NetworkNode node);

    /**
     * Indicates that Acquisition task has been started
     *
     * @param entryId Entry ID under process
     * @param downloadId ID given to the entry acquisition task
     * @param downloadSource Source on which file will be acquired from.
     */
    void fileAcquisitionInformationAvailable(String entryId,long downloadId,int downloadSource);


    /**
     * Indicates that the wifi connection has been changed. This can be a connection to a network
     * ordered by the user or connectWifi.
     *
     * @param ssid The new wifi network connected to.
     * @param connected True if the connection is up and running, false otherwise
     * @param connectedOrConnecting True if connected or an attempt to connect is in progress
     */
    void wifiConnectionChanged(String ssid, boolean connected, boolean connectedOrConnecting);

}
