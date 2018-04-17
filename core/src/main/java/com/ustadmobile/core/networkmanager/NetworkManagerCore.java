package com.ustadmobile.core.networkmanager;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmResultCallback;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.lib.db.entities.CrawlJob;
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

    @Deprecated
    DownloadJob buildDownloadJob(List<OpdsEntryWithRelations> rootEntries,
                                 String destintionDir,
                                 boolean recursive, boolean wifiDirectEnabled,
                                 boolean localWifiEnabled);

    @Deprecated
    void buildDownloadJobAsync(List<OpdsEntryWithRelations> rootEntries, String destintionDir,
                               boolean recursive, boolean wifiDirectEnabled,
                               boolean localWifiEnabled, UmCallback<DownloadJob> callback);


    /**
     * Prepare for a DownloadJob to run. The preparation is mutli-threaded and includes crawling
     * catalogs recursively and making HTTP HEAD requests where needed to determine the download size
     * of each entry.
     *
     * @param rootEntries A list of root OPDS entries that should be crawled to discover containers
     *                    to download. These can be entries with containers to acquire themselves or
     *                    subsections. Can be null if entries are supplied by uri instead.
     * @param rootUris A list of uris that should be crawled to discover contains to download. These
     *                 must be OPDS Atom XML files, NOT container URLs themselves,
     * @param downloadJob The Download Job that is to be prepared. This should have the destination
     *                    dir set. It should NOT have been inserted into the database yet.
     * @param crawlJob The Crawl Job that will prepare this download. This should be used to set any
     *                 preferences about how the download is going to be prepared (e.g. recursive or
     *                 not etc).
     * @return The CrawlJob object as per the crawlJob argument, that is then executing to build the
     *          download job.
     */
    CrawlJob prepareDownload(List<OpdsEntryWithRelations> rootEntries, List<String> rootUris,
                             DownloadJob downloadJob, CrawlJob crawlJob);

    /**
     * Prepare for a DownloadJob to run. The preparation is mutli-threaded and includes crawling
     * catalogs recursively and making HTTP HEAD requests where needed to determine the download size
     * of each entry.
     *
     * This is the asynchronous equivalent of prepareDownload
     *
     * @param rootEntries A list of root OPDS entries that should be crawled to discover containers
     *                    to download. These can be entries with containers to acquire themselves or
     *                    subsections. Can be null if entries are supplied by uri instead.
     * @param rootUris A list of uris that should be crawled to discover contains to download. These
     *                 must be OPDS Atom XML files, NOT container URLs themselves,
     * @param downloadJob The Download Job that is to be prepared. This should have the destination
     *                    dir set. It should NOT have been inserted into the database yet.
     * @param crawlJob The Crawl Job that will prepare this download. This should be used to set any
     *                 preferences about how the download is going to be prepared (e.g. recursive or
     *                 not etc).
     * @param resultCallback A callback to be called once the CrawlJob has started.
     *
     * @return The CrawlJob object as per the crawlJob argument, that is then executing to build the
     *          download job.
     */
    void prepareDownloadAsync(List<OpdsEntryWithRelations> rootEntries, List<String> rootUris,
                              DownloadJob downloadJob, CrawlJob crawlJob, UmResultCallback<CrawlJob> resultCallback);

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
