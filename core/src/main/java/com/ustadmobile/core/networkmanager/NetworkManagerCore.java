package com.ustadmobile.core.networkmanager;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;

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

    DownloadJob buildDownloadJob(List<OpdsEntryWithRelations> rootEntries, String destinationDir,
                                 boolean recursive);

    DownloadJob buildDownloadJob(List<OpdsEntryWithRelations> rootEntries, String destintionDir,
                                 boolean recursive, boolean wifiDirectEnabled,
                                 boolean localWifiEnabled);

    void buildDownloadJobAsync(List<OpdsEntryWithRelations> rootEntries, String destintionDir,
                               boolean recursive, boolean wifiDirectEnabled,
                               boolean localWifiEnabled, UmCallback<DownloadJob> callback);

    void queueDownloadJob(int downloadJobId);

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

}
