/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.networkmanager;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import java.util.Vector;

/**
 *
 * @author mike
 */
public interface NetworkManagerCore {
    
    String LINK_REL_DOWNLOAD_DESTINATION = "http://www.ustadmobile.com/namespace/opds/download-dest";

    void setSuperNodeEnabled(Object context,boolean enabled);

    long requestAcquisition(UstadJSOPDSFeed feed, boolean localNetworkEnabled, boolean wifiDirectEnabled);

    void addAcquisitionTaskListener(AcquisitionListener listener);

    void removeAcquisitionTaskListener(AcquisitionListener listener);

    void shareAppSetupFile(String filePath, String shareTitle);

    long requestFileStatus(String[] entryIds, boolean useBluetooth, boolean useHttp);

    void addNetworkManagerListener(NetworkManagerListener listener);

    void removeNetworkManagerListener(NetworkManagerListener listener);

    Vector getEntryResponsesWithLocalFile(String entryId);

    
}
