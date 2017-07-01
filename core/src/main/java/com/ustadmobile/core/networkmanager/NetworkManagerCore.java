package com.ustadmobile.core.networkmanager;

import com.ustadmobile.core.opds.UstadJSOPDSFeed;

import java.util.List;

/**
 * Created by kileha3 on 13/02/2017.
 */

public interface NetworkManagerCore {

    void setSuperNodeEnabled(Object context,boolean enabled);

    UstadJSOPDSFeed requestAcquisition(UstadJSOPDSFeed feed, boolean localNetworkEnabled, boolean wifiDirectEnabled);

    void addAcquisitionTaskListener(AcquisitionListener listener);

    void removeAcquisitionTaskListener(AcquisitionListener listener);

    void shareAppSetupFile(String filePath, String shareTitle);

    long requestFileStatus(String[] entryIds, boolean useBluetooth, boolean useHttp);

    void addNetworkManagerListener(NetworkManagerListener listener);

    void removeNetworkManagerListener(NetworkManagerListener listener);

    List<EntryCheckResponse> getEntryResponsesWithLocalFile(String entryId);

}
