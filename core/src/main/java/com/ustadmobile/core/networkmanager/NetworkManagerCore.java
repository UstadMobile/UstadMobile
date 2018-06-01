package com.ustadmobile.core.networkmanager;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmResultCallback;
import com.ustadmobile.lib.db.entities.CrawlJob;
import com.ustadmobile.lib.db.entities.DownloadSet;
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

    /**
     * The network is completely disconnected.
     */
    int CONNECTIVITY_STATE_DISCONNECTED = 0;

    /**
     * A connection is established for local connections only (e.g. for a WiFi direct download).
     */
    int CONNECTIVITY_STATE_LOCAL_ONLY = 1;

    /**
     * A metered (e.g. mobile data) connection is available.
     */
    int CONNECTIVITY_STATE_METERED = 2;

    /**
     * An unmetered Internet connection is available (e.g. 'normal' wifi).
     */
    int CONNECTIVITY_STATE_UNMETERED = 3;

    void setSuperNodeEnabled(Object context,boolean enabled);

    DownloadSet buildDownloadJob(List<OpdsEntryWithRelations> rootEntries, String destinationDir,
                                 boolean recursive);

    @Deprecated
    DownloadSet buildDownloadJob(List<OpdsEntryWithRelations> rootEntries,
                                 String destintionDir,
                                 boolean recursive, boolean wifiDirectEnabled,
                                 boolean localWifiEnabled);

    @Deprecated
    void buildDownloadJobAsync(List<OpdsEntryWithRelations> rootEntries, String destintionDir,
                               boolean recursive, boolean wifiDirectEnabled,
                               boolean localWifiEnabled, UmCallback<DownloadSet> callback);


    /**
     * Prepare for a DownloadJob to run. The preparation is mutli-threaded and includes crawling
     * catalogs recursively and making HTTP HEAD requests where needed to determine the download size
     * of each entry.
     *
     * @param downloadSet The Download Job that is to be prepared. This should have the destination
     *                    dir set. It should NOT have been inserted into the database yet.
     * @param crawlJob The Crawl Job that will prepare this download. This should be used to set any
     *                 preferences about how the download is going to be prepared (e.g. recursive or
     *                 not etc).
     * @param allowDownloadOverMeteredNetwork if true, allow the download to run over a metered network.
     *
     * @return The CrawlJob object as per the crawlJob argument, that is then executing to build the
     *          download job.
     *
     *          TODO: Remove this signature - from now on each downloadset has only one root entry
     */
    CrawlJob prepareDownload(DownloadSet downloadSet, CrawlJob crawlJob,
                             boolean allowDownloadOverMeteredNetwork);



    /**
     * Prepare for a DownloadJob to run. The preparation is mutli-threaded and includes crawling
     * catalogs recursively and making HTTP HEAD requests where needed to determine the download size
     * of each entry.
     *
     * This is the asynchronous equivalent of prepareDownload
     *
     * @param downloadSet The Download Job that is to be prepared. This should have the destination
     *                    dir set. It should NOT have been inserted into the database yet.
     * @param crawlJob The Crawl Job that will prepare this download. This should be used to set any
     *                 preferences about how the download is going to be prepared (e.g. recursive or
     *                 not etc).
     * @param allowDownloadOverMeteredNetwork if true, allow the download to run over a metered network.
     * @param resultCallback A callback to be called once the CrawlJob has started.
     *
     * @return The CrawlJob object as per the crawlJob argument, that is then executing to build the
     *          download job.
     */
    void prepareDownloadAsync(DownloadSet downloadSet, CrawlJob crawlJob,
                              boolean allowDownloadOverMeteredNetwork,
                              UmResultCallback<CrawlJob> resultCallback);

    void queueDownloadJob(int downloadJobId);

    void pauseDownloadJobAsync(int downloadJobId, UmResultCallback<Boolean> callback);

    boolean pauseDownloadJob(int downloadJobId);

    boolean cancelDownloadJob(int downloadJobId);

    void cancelDownloadJobAsync(int downloadJobId, UmResultCallback<Boolean> callback);

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
