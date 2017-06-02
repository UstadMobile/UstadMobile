package com.ustadmobile.port.sharedse.networkmanager;

import java.util.List;

/**
 * Created by kileha3 on 09/05/2017.
 */

public interface NetworkManagerListener {
    void fileStatusCheckInformationAvailable(List<String> fileIds);
    void entryStatusCheckCompleted(NetworkTask task);
    void networkNodeDiscovered(NetworkNode node);
    void networkNodeUpdated(NetworkNode node);
    void fileAcquisitionInformationAvailable(String entryId,long downloadId,int downloadSource);


    /**
     * Indicates that the wifi connection has been changed. This can be a connection to a network
     * ordered by the user or connectWifi.
     *
     * @param ssid The new wifi network connected to.
     */
    void wifiConnectionChanged(String ssid);
}
