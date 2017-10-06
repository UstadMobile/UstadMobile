package com.ustadmobile.core.networkmanager;

import com.ustadmobile.core.opds.UstadJSOPDSFeed;

import java.util.List;

/* $if umplatform != 2 $ */
/* $else$
import java.util.Vector;
$endif$ */

/**
 * Created by kileha3 on 13/02/2017.
 */

public interface NetworkManagerCore {

    String LINK_REL_DOWNLOAD_DESTINATION = "http://www.ustadmobile.com/namespace/opds/download-dest";

    /**
     * Flag to indicate queue type status
     */
    public static final int QUEUE_ENTRY_STATUS=0;

    /**
     * Flag to indicate queue type acquisition.
     */
    public static final int QUEUE_ENTRY_ACQUISITION=1;


    void setSuperNodeEnabled(Object context,boolean enabled);

    long requestAcquisition(UstadJSOPDSFeed feed, boolean localNetworkEnabled, boolean wifiDirectEnabled);

    void addAcquisitionTaskListener(AcquisitionListener listener);

    void removeAcquisitionTaskListener(AcquisitionListener listener);

    void shareAppSetupFile(String filePath, String shareTitle);

    long requestFileStatus(String[] entryIds, boolean useBluetooth, boolean useHttp);

    void addNetworkManagerListener(NetworkManagerListener listener);

    void removeNetworkManagerListener(NetworkManagerListener listener);

    void startMonitoringAvailability(AvailabilityMonitorRequest request, boolean checkKnownNodes);

    void stopMonitoringAvailability(AvailabilityMonitorRequest request);

    /* $if umplatform != 2  $ */
    List<EntryCheckResponse> getEntryResponsesWithLocalFile(String entryId);
    /* $else$
    Vector getEntryResponsesWithLocalFile(String entryId);
     $endif$ */

    boolean isEntryLocallyAvailable(String entryId);

    NetworkTask getTaskById(long taskId, int queueType);

    /**
     * This method is to be called when other components (e.g. directory scanner etc) discover
     * new content, or catalog discover content that was previously thought to have been acquired
     * is no longer around (e.g. the user manually deleted or moved files)
     *
     * @param entryId Entry id for which status has been discovered
     */
    public void handleEntryStatusChangeDiscovered(String entryId, int acquisitionStatus);
}
