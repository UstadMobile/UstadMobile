package com.ustadmobile.port.sharedse.networkmanager;

import java.util.List;

/**
 * Created by kileha3 on 09/05/2017.
 */

public interface NetworkManagerListener {
    void fileStatusCheckInformationAvailable(List<String> fileIds);
    void entryStatusCheckCompleted(NetworkTask task);
    void networkNodeDiscovered(NetworkNode node);
    void fileAcquisitionInformationAvailable(String entryId,long downloadId,int downloadSource);
    void wifiConnectionChanged(String ssid);
}
