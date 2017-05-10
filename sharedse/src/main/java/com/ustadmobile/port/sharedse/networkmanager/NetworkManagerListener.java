package com.ustadmobile.port.sharedse.networkmanager;

/**
 * Created by kileha3 on 09/05/2017.
 */

public interface NetworkManagerListener {
    void fileStatusCheckInformationAvailable(String entryIds[]);
    void entryStatusCheckCompleted(NetworkTask task);
}
