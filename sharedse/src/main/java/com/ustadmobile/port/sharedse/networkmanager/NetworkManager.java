package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.CrawlJobDao;
import com.ustadmobile.core.db.dao.DownloadJobDao;
import com.ustadmobile.core.db.dao.DownloadSetDao;
import com.ustadmobile.core.db.dao.NetworkNodeDao;
import com.ustadmobile.core.fs.db.ContainerFileHelper;
import com.ustadmobile.core.impl.AppConfig;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmResultCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.AcquisitionListener;
import com.ustadmobile.core.networkmanager.AvailabilityMonitorRequest;
import com.ustadmobile.core.networkmanager.DownloadTaskListener;
import com.ustadmobile.core.networkmanager.EntryCheckResponse;
import com.ustadmobile.core.networkmanager.NetworkManagerCore;
import com.ustadmobile.core.networkmanager.NetworkManagerListener;
import com.ustadmobile.core.networkmanager.NetworkManagerTaskListener;
import com.ustadmobile.lib.db.entities.ContainerFileEntry;
import com.ustadmobile.lib.db.entities.CrawlJob;
import com.ustadmobile.lib.db.entities.CrawlJobItem;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobItemWithDownloadSetItem;
import com.ustadmobile.lib.db.entities.DownloadJobWithDownloadSet;
import com.ustadmobile.lib.db.entities.DownloadSet;
import com.ustadmobile.lib.db.entities.DownloadSetItem;
import com.ustadmobile.lib.db.entities.EntryStatusResponse;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryParentToChildJoin;
import com.ustadmobile.lib.db.entities.OpdsEntryWithChildEntries;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelationsAndContainerMimeType;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.lib.util.UmUuidUtil;
import com.ustadmobile.port.sharedse.impl.http.CatalogUriResponder;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;
import com.ustadmobile.port.sharedse.impl.http.MountedZipHandler;
import com.ustadmobile.port.sharedse.impl.http.SharedEntryResponder;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import net.lingala.zip4j.core.ZipFile;

import javax.net.SocketFactory;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

/**
 * <h1>NetworkManager</h1>
 *
 * This is the class which defines all cross platform. It is responsible to register all network listeners,
 * register all services, getting right device address like MAC address and IP address, handle bluetooth
 * and WiFi direct connections e.t.c
 *
 * @author kileha3
 * @author mike
 *
 * @see NetworkManagerTaskListener
 * @see com.ustadmobile.core.networkmanager.NetworkManagerCore
 */

public abstract class NetworkManager implements NetworkManagerCore, NetworkManagerTaskListener,
        LocalMirrorFinder, DownloadTaskListener, EntryStatusTask.NetworkNodeListProvider, EmbeddedHTTPD.ResponseListener {

    protected ExecutorService dbExecutorService;

    /**
     * Flag to indicate type of notification used when supernode is active
     */
    public static final int NOTIFICATION_TYPE_SERVER=0;

    /**
     * Flag to indicate type of notification used during file acquisition
     */
    public static final int NOTIFICATION_TYPE_ACQUISITION=1;

    /**
     * Flag to indicate file acquisition source is from cloud.
     */
    public static final int DOWNLOAD_FROM_CLOUD =1;

    /**
     * Flag to indicate file acquisition source is peer on the same network
     */
    public static final int DOWNLOAD_FROM_PEER_ON_SAME_NETWORK =2;

    /**
     * Flag to indicate file acquisition source is peer on different network
     */
    public static final int DOWNLOAD_FROM_PEER_ON_DIFFERENT_NETWORK =3;

    /**
     * Flag to indicate file acquisition source is a wifi direct connection between two devices
     */
    public static final int DOWNLOAD_FROM_PEER_WIFIDIRECT = 4;

    public BluetoothServer bluetoothServer;

    /**
     * Maximum time for the device to wait other peer services to be discovered.
     */
    public  static final int ALLOWABLE_DISCOVERY_RANGE_LIMIT = 5 * 60 * 1000;

    /**
     * Tag to hold device IP address value in DNS-Text record
     */
    public static final String SD_TXT_KEY_IP_ADDR = "a";

    /**
     * Tag to hold device bluetooth address value in DNS-Text record
     */
    public static final String SD_TXT_KEY_BT_MAC = "b";

    /**
     * Tag to hold device service port value in DNS-Text record
     */
    public static final String SD_TXT_KEY_PORT = "port";

    /**
     * Flag to indicate wifi direct group is inactive and it is not under creation
     */
    public static final int WIFI_DIRECT_GROUP_STATUS_INACTIVE = 0;

    /**
     * Flag to indicate Wifi direct group is being created now
     */
    public static final int WIFI_DIRECT_GROUP_STATUS_UNDER_CREATION = 1;

    /**
     * Flag to indicate Wifi direct group is active
     */
    public static final int WIFI_DIRECT_GROUP_STATUS_ACTIVE = 2;

    private Object mContext;

    private Vector<NetworkNode> knownNetworkNodes=new Vector<>();

    private final Object knownNodesLock = new Object();

    private Vector<NetworkTask>[] tasksQueues = new Vector[] {
        new Vector<>(), new Vector<>()
    };

    //Map of ID (integer) -> downloadTask
//    @Deprecated
//    private HashMap<Integer, DownloadTask> activeDownloadTasks;

    private Hashtable<Class, Map<Integer, ? extends NetworkTask>> activeNetworkTasks = new Hashtable<>();

    private Vector<NetworkManagerListener> networkManagerListeners = new Vector<>();

    private Vector<AcquisitionListener> acquisitionListeners=new Vector<>();

    private Map<String,List<EntryCheckResponse>> entryResponses =new HashMap<>();

    //private NetworkTask[] currentTasks = new NetworkTask[2];

    private int[] currentTaskIndex = new int[]{-1, -1};

    private Vector<WiFiDirectGroupListener> wifiDirectGroupListeners = new Vector<>();

    private Map<String,DownloadTask> entryAcquisitionTaskMap=new HashMap<>();

    /**
     * The main HTTP server which runs on a dynamic port
     */
    protected EmbeddedHTTPD httpd;

    /**
     * The fixed port HTTP server that is used only for sharing courses.
     */
    protected RouterNanoHTTPD sharedFeedHttpd;

    /**
     * Object to use for threading safety on sharedFeedHttpd operations
     */
    protected Object sharedFeedLock = new Object();

    /**
     * The feed that the user wants to share at the moment
     */
    protected String sharedFeedUuid;

    public static final int SHARED_FEED_PORT = 8006;

    public static final int AFTER_GROUP_CONNECTION_DO_NOTHING = 0;

    public static final int AFTER_GROUP_CONNECTION_DISCONNECT = 1;

    public static final int AFTER_GROUP_CONNECTION_RESTORE = 2;

    /**
     *
     */
    protected int actionRequiredAfterGroupConnection;

    /**
     * The prefix used on all Wifi Direct group networks as per the spec.
     */
    public static final String WIFI_DIRECT_GROUP_SSID_PREFIX = "DIRECT-";

    /**
     * The http prefix we use for the internal OPDS catalog server
     */
    public static final String CATALOG_HTTP_ENDPOINT_PREFIX = "/catalog/";


    /**
     * The uri used for an OPDS feed representing items the user wishes to share locally
     */
    public static final String CATALOG_HTTP_SHARED_URI = "/shared/shared.opds";

    /**
     * The ssid of the last "normal" wifi network connected to, if any
     */
    protected String ssidToRestore;

    /**
     * Used primarily for testing but cannot be conveniently pushed into test package.
     */
    private boolean mangleWifiSsid = false;

    private boolean mangleBluetoothAddr = false;

    private Timer updateServicesTimer;

    private TimerTask updateServicesTimerTask;

    private Vector<AvailabilityMonitorRequest> availabilityMonitorRequests = new Vector<>();

    /**
     * When an availability monitor is first added, it can request that upon being added the initial
     * nodes are checked. It then creates new tasks when a node is discovered. Mapped as:
     * request -> List of task ids.
     *
     * When the monitor is withdrawn, any tasks associated with that monitor, if active, should be
     * stopped.
     */
    private HashMap<AvailabilityMonitorRequest, List<Long>> availabilityMonitorRequestToTaskIdMap = new HashMap<>();

    /**
     * Map the other way around : so that when the task is complete, we can easily remove it from the list
     */
    private HashMap<Long, AvailabilityMonitorRequest> availabilityMonitorTaskIdToRequestMap = new HashMap<>();

    private Vector<NetworkNode> knownPeers = new Vector<>();

    private Vector<WifiP2pListener> peerChangeListeners = new Vector<>();

    private boolean receivingOn = false;

    protected URLConnectionOpener wifiUrlConnectionOpener;

    private List<ConnectivityListener> connectivityListeners = new Vector<>();

    private int connectivityState;

    /**
     * The time that the shared feed will be available
     */
    public static final int SHARED_FEED_HTTPD_TIMEOUT = 30000;

    private class UpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            NetworkManager.this.updateClientServices();
            NetworkManager.this.updateSupernodeServices();
        }
    }

    /**
     * Timer task which will stop the shared feed httpd server after SHARED_FEED_HTTPD_TIMEOUT has
     * elapsed since the send course presenter has been closed or the last http response sent.
     */
    private class StopSharedFeedHttpdTimerTask extends TimerTask {
        @Override
        public void run() {
            synchronized (sharedFeedLock) {
                if(sharedFeedHttpd != null) {
                    UstadMobileSystemImpl.l(UMLog.INFO, 374, "Stop shared feed httpd");
                    sharedFeedHttpd.stop();
                    sharedFeedHttpd = null;
                }
            }
        }
    }

    private TimerTask stopSharedFeedHttpdTimerTask;


    public NetworkManager() {
//        activeDownloadTasks = new HashMap<>();
    }

    /**
     * Method used to check if super node is enabled.
     * @return boolean: TRUE if enabled and FALSE otherwise
     */
    public abstract boolean isSuperNodeEnabled();

    public abstract boolean isBroadcastEnabled();

    public abstract boolean isDiscoveryEnabled();


    public abstract void updateSupernodeServices();

    public abstract void updateClientServices();

    /**
     * Do the main initialization of the NetworkManager : set the context and start the http manager
     * This will have no effect if called twice
     *
     * @param mContext The context to use for the network manager
     */
    public synchronized void init(Object mContext) {
        if(this.mContext != null)
            return;

        this.mContext = mContext;

        dbExecutorService = Executors.newCachedThreadPool();

        activeNetworkTasks.put(DownloadTask.class, new HashMap<Integer, DownloadTask>());

        try {
            /*
             * Do not use .l logging method here: when running sharedse mock tests, this gets called in the
             * UstadMobileSystemImpl constructor. Calling the system log here thus results in a
             * stack overflow.
             */
            httpd = new EmbeddedHTTPD(0, mContext);
            httpd.start();
            System.out.println("Started main http server on port: " + httpd.getListeningPort());
        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.CRITICAL, 1, "Failed to start http server");
            throw new RuntimeException("Failed to start http server", e);
        }
    }

    /**
     * Method which tell if bluetooth is enabled or disabled on the device.
     * @return boolean: TRUE, if bluetooth is enabled and FALSE otherwise
     */
    public  abstract boolean isBluetoothEnabled();

    /**
     * Enable or disable bluetooth. Depending on the underlying system and version this may or may
     * not be supported. Anything depending on bluetooth being disabled or enabled should be handled
     * in handleBluetoothEnabledChanged
     *
     * @return true the request was successfully submitted
     */
    public abstract boolean setBluetoothEnabled(boolean enabled);


    /**
     * Method which is used to get make refrence to the BluetoothServer
     * @return BluetoothServer
     */
    public abstract BluetoothServer getBluetoothServer();

    /**
     * Method to tell if Wi-Fi is enabled or disabled on the device
     * @return boolean: TRUE, if enabled otherwise FALSE.
     */
    public abstract boolean isWiFiEnabled();

    /**
     * Method to enable or disable WiFi.
     *
     * @param enabled
     * @return true if the operation is successful, false otherwise
     */
    public abstract boolean setWifiEnabled(boolean enabled);




    /**
     * Method which tells if the file can be downloaded locally or not.
     * @param entryId File Entry ID
     * @return boolean: TRUE, if is available locally otherwise FALSE.
     */
    public boolean isFileAvailable(String entryId){
        for(EntryCheckResponse response:entryResponses.get(entryId)){
            if(response.isFileAvailable()){
                return true;
            }
        }
        return false;
    }

    /**
     * Request the status of given entryIds to see if they are available locally or not
     *
     * @param entryIds EntryIDs (e.g. as per an OPDS catalog) to look for
     * @param mContext System context
     * @param nodeList List of all peer nodes discovered
     * @param useBluetooth If true - use bluetooth addresses that were discovered using WiFi direct to ask for availability
     * @param useHttp If true - use HTTP to talk with nodes discovered which are reachable using HTTP (e.g. nodes already connected to the same wifi network)
     *
     * @return
     */
    //TODO: remove mContext parameter
    public long requestFileStatus(List<String> entryIds,Object mContext,List<NetworkNode> nodeList, boolean useBluetooth, boolean useHttp){
        EntryStatusTask task = new EntryStatusTask(entryIds,this,this);
        task.setTaskType(NetworkManagerCore.QUEUE_ENTRY_STATUS);
        task.setUseBluetooth(useBluetooth);
        task.setUseHttp(useHttp);
        queueTask(task);
        return task.getTaskId();
    }

    @Override
    public List<NetworkNode> getNetworkNodes() {
        return UmAppDatabase.getInstance(getContext()).getNetworkNodeDao().findAllActiveNodes();
    }

    public long requestFileStatus(String[] entryIds, boolean useBluetooth, boolean useHttp) {
        return requestFileStatus(Arrays.asList(entryIds), getContext(), getKnownNodes(), useBluetooth, useHttp);
    }


    /**
     * Request the status of given entryIds to see if they are available locally or not. By default
     * use both bluetooth and http
     *
     * @param entryIds EntryIDs (e.g. as per an OPDS catalog) to look for
     * @param mContext System context
     * @param nodeList
     *
     * @return
     */
    public long requestFileStatus(List<String> entryIds,Object mContext,List<NetworkNode> nodeList) {
        return requestFileStatus(entryIds, mContext, nodeList, true, true);
    }


    /**
     * {@inheritDoc}
     */
    public CrawlJob prepareDownload(DownloadSet downloadSet, CrawlJob crawlJob,
                                    boolean allowDownloadOverMeteredNetwork) {
        UmAppDatabase dbManager = UmAppDatabase.getInstance(getContext());
        DownloadSetDao setDao = dbManager.getDownloadSetDao();
        DownloadJobDao jobDao = dbManager.getDownloadJobDao();

        if(crawlJob.getRootEntryUuid() == null && crawlJob.getRootEntryUri() == null)
            throw new IllegalArgumentException("CrawlJob has no root uuid or uri!");

        //see if this downloadset already exists
        if(crawlJob.getRootEntryUuid() == null){
            //we need to load the root item using the OpdsRepository
            OpdsEntryWithRelations rootEntry = UstadMobileSystemImpl.getInstance()
                    .getOpdsAtomFeedRepository(getContext())
                    .getEntryByUrlStatic(crawlJob.getRootEntryUri());
            crawlJob.setRootEntryUuid(rootEntry.getUuid());
        }


        DownloadSet existingSet = dbManager.getDownloadSetDao().findByRootEntry(
                crawlJob.getRootEntryUuid());
        if(existingSet != null) {
            //TODO: Move downloaded items between folders if needed
            existingSet.setDestinationDir(downloadSet.getDestinationDir());
            downloadSet = existingSet;
        }else{
            downloadSet.setRootOpdsUuid(crawlJob.getRootEntryUuid());
        }

        downloadSet.setId((int)setDao.insertOrReplace(downloadSet));

        //Now create a new download job
        DownloadJob downloadJob = new DownloadJob(downloadSet, System.currentTimeMillis());
        downloadJob.setAllowMeteredDataUsage(allowDownloadOverMeteredNetwork);
        downloadJob.setStatus(UstadMobileSystemImpl.DLSTATUS_NOT_STARTED);


        downloadJob.setDownloadJobId((int)jobDao.insert(downloadJob));

        CrawlJobDao crawlJobDao = dbManager.getCrawlJobDao();
        crawlJob.setContainersDownloadJobId(downloadJob.getDownloadJobId());
        crawlJob.setCrawlJobId((int)crawlJobDao.insert(crawlJob));


        //Insert the root crawl item
        OpdsEntryWithRelations entry = new OpdsEntryWithRelations();
        entry.setUuid(crawlJob.getRootEntryUuid());
        UmAppDatabase.getInstance(getContext()).getDownloadJobCrawlItemDao().insert(
                new CrawlJobItem(crawlJob.getCrawlJobId(), entry, NetworkTask.STATUS_QUEUED, 0));

        CrawlTask task = new CrawlTask(crawlJob, dbManager, this);
        task.start();

        return crawlJob;
    }

    /**
     * {@inheritDoc}
     */
    public void prepareDownloadAsync(DownloadSet downloadSet, CrawlJob crawlJob,
                                     boolean allowDownloadOverMeteredNetwork,
                                     UmResultCallback<CrawlJob> resultCallback) {
        dbExecutorService.execute(() -> resultCallback.onDone(prepareDownload(downloadSet, crawlJob,
                allowDownloadOverMeteredNetwork)));
    }


    @Deprecated
    public DownloadSet buildDownloadJob(List<OpdsEntryWithRelations> rootEntries, String destintionDir,
                                        boolean recursive, boolean wifiDirectEnabled,
                                        boolean localWifiEnabled) {
        DownloadSetDao jobDao = UmAppDatabase.getInstance(getContext()).getDownloadSetDao();

        DownloadSet job = new DownloadSet();
        job.setDestinationDir(destintionDir);
//        job.setStatus(UstadMobileSystemImpl.DLSTATUS_NOT_STARTED);
        job.setLanDownloadEnabled(localWifiEnabled);
        job.setWifiDirectDownloadEnabled(wifiDirectEnabled);
        job.setId((int)jobDao.insert(job));



        ArrayList<DownloadSetItem> jobItems = new ArrayList<>();
        for(OpdsEntryWithRelations entry : rootEntries) {
            jobItems.add(new DownloadSetItem(entry, job));
        }
        UmAppDatabase.getInstance(getContext()).getDownloadSetItemDao().insertList(jobItems);

        return job;
    }

    public void buildDownloadJobAsync(List<OpdsEntryWithRelations> rootEntries, String destintionDir,
                                      boolean recursive, boolean wifiDirectEnabled,
                                      boolean localWifiEnabled, UmCallback<DownloadSet> callback) {
        dbExecutorService.execute(() -> callback.onSuccess(buildDownloadJob(rootEntries, destintionDir,
                recursive, wifiDirectEnabled, localWifiEnabled)));
    }

    public DownloadSet buildDownloadJob(List<OpdsEntryWithRelations> rootEntries, String destinationDir,
                                        boolean recursive){
        return buildDownloadJob(rootEntries,  destinationDir, recursive, true, true);
    }


    public void queueDownloadJob(int downloadJobId) {
        //just set the status of the job and let it be found using a query
        UstadMobileSystemImpl.l(UMLog.INFO, 0, "Queuing download job #" + downloadJobId);
        dbExecutorService.execute(() -> {
            UmAppDatabase dbManager = UmAppDatabase.getInstance(getContext());
            dbManager.getDownloadJobItemDao().updateUnpauseItemsByDownloadJob(downloadJobId);
            dbManager.getDownloadJobDao().queueDownload(downloadJobId, NetworkTask.STATUS_QUEUED,
                    System.currentTimeMillis());
            int[] downloadJobItemIds = dbManager.getDownloadJobItemDao().findAllIdsByDownloadJob(
                    downloadJobId);
            //TODO: filter the above to handle only those items that are not completed
            for(int downloadJobItemId : downloadJobItemIds) {
                dbManager.getOpdsEntryStatusCacheDao().handleDownloadJobQueued(downloadJobItemId);
            }
            checkDownloadJobQueue();
        });
    }

    private DownloadTask stopDownloadAndSetStatus(int downloadJobId, int statusAfterStop) {
        NetworkTask downloadTask = activeNetworkTasks.get(DownloadTask.class).get(downloadJobId);
        if(downloadTask == null) {
            UstadMobileSystemImpl.l(UMLog.WARN, 0, "stopDownloadAndSetStatus: " +
                    " download job #" + downloadJobId + " is not active");
            return null;
        }

        downloadTask.stop(statusAfterStop);

        return (DownloadTask)downloadTask;
    }

    public <T extends NetworkTask> T getActiveTask(int taskId, Class<T> taskType) {
        Map<Integer, ? extends NetworkTask> taskTypeMap = activeNetworkTasks.get(taskType);
        NetworkTask task = taskTypeMap.get(taskId);
        if(task != null) {
            return (T)task;
        }else {
            return null;
        }
    }


    @Override
    public boolean pauseDownloadJob(int downloadJobId) {
        DownloadTask downloadTask = stopDownloadAndSetStatus(downloadJobId, NetworkTask.STATUS_PAUSED);
        //TODO: this should likely go, it should be possible to pause a download that is not currently running
        if(downloadTask == null)
            return false;

        UmAppDatabase dbManager = UmAppDatabase.getInstance(getContext());
        List<DownloadJobItemWithDownloadSetItem> pausedItems = dbManager.getDownloadJobItemDao()
                .findByDownloadJobAndStatusRange(downloadJobId, NetworkTask.STATUS_WAITING_MIN,
                        NetworkTask.STATUS_COMPLETE_MIN);
        UstadMobileSystemImpl.l(UMLog.DEBUG, 0, "Setting status to paused on " +
                pausedItems.size() + " items");
        for(DownloadJobItemWithDownloadSetItem pausedItem : pausedItems) {
            dbManager.getDownloadJobItemDao().updateStatus(pausedItem.getDownloadJobItemId(),
                    NetworkTask.STATUS_PAUSED);
            dbManager.getOpdsEntryStatusCacheDao().handleContainerDownloadPaused(
                    pausedItem.getDownloadSetItem().getEntryId());
        }

        return true;
    }

    @Override
    public void pauseDownloadJobAsync(int downloadJobId, UmResultCallback<Boolean> callback) {
        dbExecutorService.execute(() -> callback.onDone(pauseDownloadJob(downloadJobId)));
    }

    @Override
    public boolean cancelDownloadJob(int downloadJobId) {
        UmAppDatabase dbManager = UmAppDatabase.getInstance(getContext());
        DownloadTask downloadTask = stopDownloadAndSetStatus(downloadJobId,
                NetworkTask.STATUS_CANCELED);
        UstadMobileSystemImpl.l(UMLog.INFO, 0, "cancelDownloadJob #" + downloadJobId +
            " task running: " + (downloadTask != null));

        //go through all downloads that have been completed, and delete them
        List<DownloadJobItemWithDownloadSetItem> downloadedItems =  dbManager
                .getDownloadJobItemDao().findAllWithDownloadSet(downloadJobId);

        for(DownloadJobItemWithDownloadSetItem item : downloadedItems) {
            if(item.getStatus() < NetworkTask.STATUS_COMPLETE_MIN) {
                UstadMobileSystemImpl.l(UMLog.INFO, 0, "cancelDownloadJob #"
                        + downloadJobId + " : item #" + item.getDownloadJobItemId() +
                        " : " + item.getDownloadSetItem().getEntryId() + " : handleContainerDownloadAborted");
                dbManager.getOpdsEntryStatusCacheDao().handleContainerDownloadAborted(item
                        .getDownloadSetItem().getEntryId());

                //check for any file leftovers
                if(item.getDestinationFile() != null) {
                    File file = new File(item.getDestinationFile());
                    if(file.exists())
                        file.delete();

                    file = new File(item.getDestinationFile() + ResumableHttpDownload.DLPART_EXTENSION);
                    if(file.exists())
                        file.delete();
                }
            }else {
                UstadMobileSystemImpl.l(UMLog.INFO, 0, "cancelDownloadJob #"
                        + " : item #" + item.getDownloadJobItemId() +
                        " : " + item.getDownloadSetItem().getEntryId() + ": deleteContainer");
                ContainerFileHelper.getInstance().deleteAllContainerFilesByEntryId(getContext(),
                        item.getDownloadSetItem().getEntryId());
            }

        }

        UstadMobileSystemImpl.l(UMLog.INFO, 0, "cancelDownloadJob #" + downloadJobId +
                " cancel complete");
        return downloadTask != null;
    }

    @Override
    public void cancelDownloadJobAsync(int downloadJobId, UmResultCallback<Boolean> callback) {
        dbExecutorService.execute(() -> callback.onDone(cancelDownloadJob(downloadJobId)));
    }

    /**
     *
     */
    public void checkDownloadJobQueue(){
        @SuppressWarnings("unchecked")
        Map<Integer, DownloadTask> taskMap = (Map<Integer, DownloadTask>)activeNetworkTasks.get(
                DownloadTask.class);

        int connectivityState = getConnectivityState();
        if(taskMap.isEmpty() && connectivityState != CONNECTIVITY_STATE_DISCONNECTED){
            DownloadJobWithDownloadSet job = UmAppDatabase.getInstance(getContext())
                    .getDownloadJobDao()
                    .findNextDownloadJobAndSetStartingStatus(connectivityState == CONNECTIVITY_STATE_METERED);
            if(job == null) {
                UstadMobileSystemImpl.l(UMLog.DEBUG, 0, "checkDownloadJobQueue: no pending download jobs");
                return;//nothing to do
            }

            UstadMobileSystemImpl.l(UMLog.DEBUG, 0, "checkDownloadJobQueue: starting download job #" +
                    job.getDownloadJobId());
            DownloadTask task = new DownloadTask(job, this, this,
                    dbExecutorService);
            taskMap.put(job.getDownloadJobId(), task);
            task.start();
        }else {
            UstadMobileSystemImpl.l(UMLog.DEBUG, 0,
            "checkDownloadJobQueue: not looking for new downloads: " +
                    (!taskMap.isEmpty() ? " There are currently active tasks" : "Network is disconnected"));
        }
    }

    public void checkDownloadJobQueueAsync(UmResultCallback<Void> callback){
        dbExecutorService.execute(() -> {
            checkDownloadJobQueue();
            if(callback != null)
                callback.onDone(null);
        });
    }


    @Override
    public void handleDownloadTaskStatusChanged(NetworkTask task, int status) {
        if(task.getStatus() >= NetworkTask.STATUS_COMPLETE_MIN || task.getStatus() < NetworkTask.STATUS_RUNNING_MIN){
            //this task has finished or has to wait (e.g. for a connection to be available)
            activeNetworkTasks.get(DownloadTask.class).remove(task.getTaskId());
            checkDownloadJobQueue();
        }
    }


    public void startMonitoringAvailability(AvailabilityMonitorRequest request, boolean checkKnownNodes){
        synchronized (availabilityMonitorRequests) {
            availabilityMonitorRequests.addElement(request);
            List<Long> monitorTaskList = new Vector<>();
            availabilityMonitorRequestToTaskIdMap.put(request, monitorTaskList);
            if(checkKnownNodes) {
                long initCheckTaskId = requestFileStatus(new ArrayList<String>(request.getEntryIdsToMonitor()),
                        getContext(), knownNetworkNodes, true, true);
                monitorTaskList.add(initCheckTaskId);
                availabilityMonitorTaskIdToRequestMap.put(initCheckTaskId, request);
            }
        }
    }


    public void stopMonitoringAvailability(AvailabilityMonitorRequest request) {
        synchronized (availabilityMonitorRequests) {
            availabilityMonitorRequests.removeElement(request);
            List<Long> availabilityMonitorRequests = availabilityMonitorRequestToTaskIdMap.get(request);
            NetworkTask task;
            for(Long taskId : availabilityMonitorRequests) {
                task = getTaskById(taskId, NetworkManagerCore.QUEUE_ENTRY_STATUS);
                if(task != null)
                    task.stop(NetworkTask.STATUS_STOPPED);
            }

            availabilityMonitorRequestToTaskIdMap.remove(request);
        }
    }


    /**
     * Creating task que as per request received.
     * @param task Network task to be queued
     * @return NetworkTask: Queued NetworkTask
     */
    public NetworkTask queueTask(NetworkTask task){
        tasksQueues[task.getTaskType()].addElement(task);
        checkTaskQueue(task.getTaskType());

        return task;
    }

    /**
     * Method which check the queue and manage it,
     * if there is any task to be executed will be executed.
     *
     * @param queueType Queue type, whether is queue of Acquisition task or EntryStatus task
     */
    public synchronized void checkTaskQueue(int queueType){
        if(queueType == QUEUE_ENTRY_ACQUISITION && tasksQueues[queueType].isEmpty()) {
            switch(actionRequiredAfterGroupConnection) {
                case AFTER_GROUP_CONNECTION_DISCONNECT:
                    UstadMobileSystemImpl.l(UMLog.INFO, 326,
                        "NetworkManager:checkTaskQueue: all tasks complete - WiFi to be disconnected after group connection");
                    actionRequiredAfterGroupConnection = AFTER_GROUP_CONNECTION_DO_NOTHING;
                    disconnectWifi();
                    break;

                case AFTER_GROUP_CONNECTION_RESTORE:
                    UstadMobileSystemImpl.l(UMLog.INFO, 327,
                        "NetworkManager:checkTaskQueue: all tasks complete - WiFi to be restored to 'normal' after group connection");
                    actionRequiredAfterGroupConnection = AFTER_GROUP_CONNECTION_DO_NOTHING;
                    restoreWifi();
                    break;
            }
        }


        if(currentTaskIndex[queueType] == -1) {
            int nextTaskIndex = selectNextTask(tasksQueues[queueType]);

            if(nextTaskIndex != -1) {
                currentTaskIndex[queueType] = nextTaskIndex;
                NetworkTask currentTask = tasksQueues[queueType].get(currentTaskIndex[queueType]);
                currentTask.setNetworkManager(this);
                currentTask.setNetworkTaskListener(this);
                currentTask.start();
            }

        }
    }

    /**
     * Finds the next task to do as follows:
     *
     * 1. Skip any task that has been manually stopped
     * TODO: handle a task that has a do not start before time on it
     *
     * @param tasks
     *
     * @return index of the next task to run
     */
    protected int selectNextTask(Vector<NetworkTask> tasks) {
        for(int i = 0; i < tasks.size(); i++){
            if(tasks.get(i).getStatus() != NetworkTask.STATUS_STOPPED)
                return i;
        }

        return -1;
    }


    /**
     * Method which is invoked when new node has been found from Wi-Fi Direct discovery service.
     * @param serviceFullDomain Combination of application service record and protocol used
     * @param senderMacAddr Host device MAC address
     * @param txtRecords Map of DNS-Text records
     */
    public void handleWifiDirectSdTxtRecordsAvailable(String serviceFullDomain,String senderMacAddr, HashMap<String, String> txtRecords) {
        dbExecutorService.execute(() -> {
            String wifiP2pServiceName = UstadMobileSystemImpl.getInstance()
                    .getAppConfigString(AppConfig.KEY_WIFI_P2P_INSTANCE_NAME, "ustadmobile",
                            getContext());
            if(serviceFullDomain.contains(wifiP2pServiceName)){
                String ipAddr = txtRecords.get(SD_TXT_KEY_IP_ADDR);
                String btAddr = txtRecords.get(SD_TXT_KEY_BT_MAC);
                int port=Integer.parseInt(txtRecords.get(SD_TXT_KEY_PORT));

                boolean newNode = false;
                NetworkNode node = null;
                synchronized (knownNodesLock) {
                    NetworkNodeDao networkNodeDao = UmAppDatabase.getInstance(getContext()).getNetworkNodeDao();
                    node = networkNodeDao.findNodeByIpOrWifiDirectMacAddress(ipAddr, senderMacAddr);

                    if(node == null) {
                        node = new NetworkNode(senderMacAddr, ipAddr);
                        newNode = true;
                    }

                    if(ipAddr != null)
                        node.setIpAddress(ipAddr);

                    node.setBluetoothMacAddress(btAddr);
                    node.setWifiDirectMacAddress(senderMacAddr);
                    node.setPort(port);
                    node.setWifiDirectLastUpdated(Calendar.getInstance().getTimeInMillis());

                    if(newNode) {
                        networkNodeDao.insert(node);
                    }else {
                        networkNodeDao.update(node);
                    }
                }


                if(newNode){
                    queueStatusChecksForNewNode(node);
                    fireNetworkNodeDiscovered(node);
                }else{
                    fireNetworkNodeUpdated(node);
                }
            }
        });

    }

    public void handleWifiDirectPeersChanged(List<NetworkNode> peers) {
        synchronized (this.knownPeers) {
            knownPeers.clear();
            knownPeers.addAll(peers);
        }

        fireWifiDirectPeersChanged();
    }

    public List<NetworkNode> getKnownWifiDirectPeers() {
        return knownPeers;
    }

    protected void fireWifiDirectPeersChanged() {
        synchronized (peerChangeListeners) {
            for(WifiP2pListener listener: peerChangeListeners) {
                listener.peersChanged(knownPeers);
            }
        }
    }

    protected void fireWifiP2pConnectionChanged(boolean connected) {
        synchronized (peerChangeListeners) {
            for(WifiP2pListener listener : peerChangeListeners) {
                listener.wifiP2pConnectionChanged(connected);
            }
        }
    }

    protected void fireWifiP2pConnectionResult(String macAddress, boolean successful) {
        synchronized (peerChangeListeners) {
            for(WifiP2pListener listener: peerChangeListeners) {
                listener.wifiP2pConnectionResult(macAddress, successful);
            }
        }
    }

    public void addWifiDirectPeersListener(WifiP2pListener listener) {
        peerChangeListeners.add(listener);
    }

    public void removeWifiDirectPeersListener(WifiP2pListener listener) {
        peerChangeListeners.remove(listener);
    }


    protected void queueStatusChecksForNewNode(NetworkNode node) {
        Set<String> entryIdsToQuery = new HashSet<>();
        synchronized (availabilityMonitorRequests) {
            for(AvailabilityMonitorRequest request: availabilityMonitorRequests) {
                entryIdsToQuery.addAll(request.getEntryIdsToMonitor());
            }
        }

        if(entryIdsToQuery.isEmpty())
            return;

        ArrayList entryIdList = new ArrayList(entryIdsToQuery);
        ArrayList<NetworkNode> nodeList = new ArrayList<>();
        nodeList.add(node);
        requestFileStatus(entryIdList, getContext(), nodeList);
    }

    /**
     * Method which will be invoked when new node is found using network service discovery.
     * This should be called by the underlying system implementation when a service is found that
     * matches the network service type.
     *
     * @param serviceName application service name
     * @param ipAddress Host device IP address (must not be null)
     * @param port Service port on host device
     */
    public void handleNetworkServerDiscovered(String serviceName,String ipAddress,int port){
        dbExecutorService.execute(() -> {
            NetworkNode node;
            boolean newNode;
            synchronized (knownNodesLock) {
                if(ipAddress.equals(getDeviceIPAddress()) || ipAddress.equals("127.0.0.1"))
                    return;


                UmAppDatabase dbManager = UmAppDatabase.getInstance(getContext());
                node = dbManager.getNetworkNodeDao().findNodeByIpAddress(ipAddress);

//            node = getNodeByIpAddress(ipAddress);
                newNode = (node == null);

                if(node == null) {
                    node = new NetworkNode(null,ipAddress);
//                knownNetworkNodes.add(node);
                }

                node.setNsdServiceName(serviceName);
                node.setNetworkServiceLastUpdated(Calendar.getInstance().getTimeInMillis());
                node.setPort(port);

                if(newNode)
                    dbManager.getNetworkNodeDao().insert(node);
                else
                    dbManager.getNetworkNodeDao().update(node);
            }


            if(newNode){
                queueStatusChecksForNewNode(node);
                fireNetworkNodeDiscovered(node);
            }else{
                fireNetworkNodeUpdated(node);
            }
        });
    }

    public void handleNetworkServiceRemoved(String serviceName) {
        synchronized (knownNetworkNodes) {
            String nodeServiceName;
            NetworkNode removedNode;
            for(NetworkNode node : knownNetworkNodes) {
                nodeServiceName = node.getNsdServiceName();
                if(nodeServiceName != null && serviceName.equals(nodeServiceName)) {
                    node.setNsdServiceName(null);
                    break;
                }
            }
        }
    }

    /**
     * This method must be called by the underlying implementation when this is detected.
     *
     * @param enabled
     */
    public void handleWifiEnabledChanged(boolean enabled) {
//        if(enabled) {
//            checkTaskQueue(QUEUE_ENTRY_ACQUISITION);
//            if(isSuperNodeEnabled() && isBluetoothEnabled()) {
//                startSuperNode();
//            }else if(!isSuperNodeEnabled()){
//                startClientMode();
//            }
//        }else {
//            stopClientMode();
//            stopSuperNode();
//
//            if(currentTaskIndex[QUEUE_ENTRY_ACQUISITION] != -1) {
//                tasksQueues[QUEUE_ENTRY_ACQUISITION].get(currentTaskIndex[QUEUE_ENTRY_ACQUISITION]).stop(
//                        NetworkTask.STATUS_WAITING_FOR_NETWORK);
//                currentTaskIndex[QUEUE_ENTRY_ACQUISITION] = -1;
//            }
//        }

    }

    protected synchronized void submitUpdateServicesTask(long delay) {
        cancelUpdateServicesTask();
        updateServicesTimer = new Timer();
        updateServicesTimerTask = new UpdateTimerTask();
        updateServicesTimer.schedule(updateServicesTimerTask, delay);
    }

    protected synchronized void cancelUpdateServicesTask() {
        if(updateServicesTimerTask != null){
            updateServicesTimerTask.cancel();
            updateServicesTimerTask = null;
        }

        if(updateServicesTimer != null){
            updateServicesTimer.cancel();
            updateServicesTimer = null;
        }
    }

    /**
     * This method must be called by the underlying implementation when the change is detected.
     * @param enabled
     */
    public void handleBluetoothEnabledChanged(boolean enabled) {

    }


    /**
     * Get known network node using it's IP address
     * @param ipAddr Node's IP address to search for.
     * @return NetworkNode object
     */
    public NetworkNode getNodeByIpAddress(String ipAddr) {
        synchronized (knownNetworkNodes) {
            String nodeIp;
            for(NetworkNode node : knownNetworkNodes) {
                nodeIp = node.getIpAddress();
                if(nodeIp != null && nodeIp.equals(ipAddr))
                    return node;
            }
        }

        return null;
    }

    /**
     * Get known network node using it's bluetooth address
     * @param bluetoothAddr Node's bluetooth address to search for.
     * @return NetworkNode object
     *
     * @Deprecated Use the database instead
     */
    public NetworkNode getNodeByBluetoothAddr(String bluetoothAddr) {
        synchronized (knownNetworkNodes) {
            String nodeBtAddr;
            for(NetworkNode node : knownNetworkNodes) {
                nodeBtAddr = node.getBluetoothMacAddress();
                if(nodeBtAddr != null && nodeBtAddr.equals(bluetoothAddr))
                    return node;
            }
        }

        return null;
    }

    /**
     * Method which used to register NetworkManagerListener to listen for Network events.
     * @param listener NetworkManagerListener instance.
     */
    public void addNetworkManagerListener(NetworkManagerListener listener){
        networkManagerListeners.add(listener);
    }

    /**
     * Method which used to remove NetworkManagerListener after being registered.
     * @param listener NetworkManagerListener to be removed
     */
    public void removeNetworkManagerListener(NetworkManagerListener listener){
        if(listener!=null){
            networkManagerListeners.remove(listener);
        }
    }

    /**
     * Method invoked when device request bluetooth connection.
     * @param deviceAddress Peer device bluetooth address to connect to.
     * @param handler BluetoothConnectionHandler which listen for connection events.
     */
    public abstract void connectBluetooth(String deviceAddress,BluetoothConnectionHandler handler);

    public void handleEntriesStatusUpdate(NetworkNode node, List<String> entryIds,
                                          List<ContainerFileEntry> availableEntries) {

        List<String> remainingEntries = new ArrayList<>(entryIds);
        ArrayList<EntryStatusResponse> entryStatusResponses = new ArrayList<>();
        long responseTime = System.currentTimeMillis();

        for(ContainerFileEntry availableEntry : availableEntries) {
            entryStatusResponses.add(new EntryStatusResponse(availableEntry.getContainerEntryId(),
                    node.getNodeId(), responseTime, 0, true));
            remainingEntries.remove(availableEntry.getContainerEntryId());
        }

        for(String unavailableEntryId : remainingEntries) {
            entryStatusResponses.add(new EntryStatusResponse(unavailableEntryId, node.getNodeId(),
                    responseTime,0, false));
        }

        UmAppDatabase.getInstance(getContext()).getEntryStatusResponseDao().insert(entryStatusResponses);
        fireFileStatusCheckInformationAvailable(entryIds);
    }

    /**
     * Method which get particular entry status response on a specific node from list of responses.
     * @param fileId Entry Id to look for the status
     * @param node NetworkNode to request response from.
     * @return EntryCheckResponse object
     */

    public EntryCheckResponse getEntryResponse(String fileId, NetworkNode node) {
        List<EntryCheckResponse> responseList = getEntryResponses().get(fileId);
        if(responseList == null)
            return null;

        for(int responseNum = 0; responseNum < responseList.size(); responseNum++) {
            if(responseList.get(responseNum).getNetworkNode().equals(node)) {
                return responseList.get(responseNum);
            }
        }

        return null;
    }

    /**
     * Get response from response list which contains a file we a looking for and can be downloaded locally,
     * first priority is given to node on the same network.
     * If no matching node then check for the node on different network.
     * @param entryId
     * @return
     */
    @Override
    public List<EntryCheckResponse> getEntryResponsesWithLocalFile(String entryId){
        List<EntryCheckResponse> responseList=getEntryResponses().get(entryId);
        return responseList;
    }

    @Override
    public boolean isEntryLocallyAvailable(String entryId) {
        List<EntryCheckResponse> responseList = getEntryResponsesWithLocalFile(entryId);
        if(responseList == null)
            return false;

        for(int i = 0; i < responseList.size(); i++) {
            if(responseList.get(i).isFileAvailable())
                return true;
        }

        return false;
    }

    /**
     * Method which will be called to fire all events when file acquisition information is available
     * @param entryId Entry Id under processing
     * @param downloadId Id assigned to the acquisition task
     * @param downloadSource File source, which might be from cloud,
     *                       peer on the same network or peer on different network
     */
    @Deprecated
    public void handleFileAcquisitionInformationAvailable(String entryId,long downloadId,int downloadSource){
        fireFileAcquisitionInformationAvailable(entryId,downloadId,downloadSource);
    }

    /**
     * Method which will be called to fire all Wi-Fi Direct connection events
     * @param ssid SSID of the current connected Wi-Fi Direct group.
     */
    public void handleWifiConnectionChanged(String ssid, boolean connected, boolean connectedOrConnecting){
        if(ssid != null && connected && !ssid.toUpperCase().startsWith("DIRECT-")) {
            //this is not a wifi direct network
            actionRequiredAfterGroupConnection = AFTER_GROUP_CONNECTION_DO_NOTHING;
        }

        if(!connected) {
            synchronized (knownNetworkNodes) {
                for(NetworkNode node : knownNetworkNodes) {
                    node.setNsdServiceName(null);
                }
            }
        }

        fireWiFiConnectionChanged(ssid, connected, connectedOrConnecting);
    }




    /**
     * Method which will be called to fire all events when entry status check
     * is completed and information is there to process
     * @param fileIds List of entry ID's which were processed
     */
    protected void fireFileStatusCheckInformationAvailable(List<String> fileIds) {
        String[] fileIdsArray = new String[fileIds.size()];
        fileIds.toArray(fileIdsArray);
        synchronized (networkManagerListeners) {
            for(NetworkManagerListener listener : networkManagerListeners){
                listener.fileStatusCheckInformationAvailable(fileIdsArray);
            }
        }
    }

    /**
     * Method firing event to all listening part of the app
     * when file acquisition information is available.
     * @param entryId Entry Id under processing
     * @param downloadId Id assigned to the acquisition task
     * @param downloadSource File source, which might be from cloud,
     *                       peer on the same network or peer on different network
     */
    private void fireFileAcquisitionInformationAvailable(String entryId,long downloadId,int downloadSource) {
        synchronized (networkManagerListeners) {
            for(NetworkManagerListener listener : networkManagerListeners){
                listener.fileAcquisitionInformationAvailable(entryId,downloadId,downloadSource);
            }
        }
    }

    /**
     * Method which fire event to all listening part of the
     * app when new node has been found.
     * @param node NetworkNode object which contain all node information
     */
    protected void fireNetworkNodeDiscovered(NetworkNode node) {
        synchronized (networkManagerListeners) {
            for(NetworkManagerListener listener : networkManagerListeners){
                listener.networkNodeDiscovered(node);
            }
        }
    }

    /**
     * Method which will be firing events to notify other part of the app that node
     * information has been updated.
     * @param node NetworkNode object
     */
    protected void fireNetworkNodeUpdated(NetworkNode node){
        synchronized (networkManagerListeners) {
            for(NetworkManagerListener listener : networkManagerListeners){
                listener.networkNodeUpdated(node);
            }
        }
    }

    /**
     * Method which will be firing events to notify the listening parts of the app that the
     * entry status check task has been completed.
     * @param task Entry status check NetworkTask
     */
    protected void fireNetworkTaskStatusChanged(NetworkTask task){
        synchronized (networkManagerListeners) {
            for(NetworkManagerListener listener : networkManagerListeners){
                listener.networkTaskStatusChanged(task);
            }
        }
    }

    /**
     * Method which will be firing events to all listening part ot the app to notify
     * that Wi-Fi state connection has been changed.
     * @param ssid Currently connected Wi-Fi network SSID
     */
    protected void fireWiFiConnectionChanged(String ssid, boolean connected, boolean connectedOrConnecting){
        synchronized (networkManagerListeners) {
            for(NetworkManagerListener listener : networkManagerListeners){
                listener.wifiConnectionChanged(ssid, connected, connectedOrConnecting);
            }
        }
    }

    /**
     * Method which will be used to add notification as user feedback when super node is
     * activated or during entry acquisition execution
     * @param notificationType Type of notification (File acquisition or Super node notification)
     * @param title Notification title
     * @param message Notification message which provide more information about the notification
     * @return int: Type of notification
     */
    public abstract int addNotification(int notificationType,String title,String message);

    /**
     * Method which is responsible for updating the notification information especially
     * when the notification type is Acquisition
     * @param notificationType Type of notification (File acquisition or Super node notification)
     * @param progress File acquisition progress status
     * @param title Notification title
     * @param message Notification message which provide more information about the notification
     */
    public abstract void updateNotification(int notificationType,int progress,String title,String message);

    /**
     * Method which will be responsible for removing notification when file acquisition task
     * is completed or Super node mode was deactivated.
     * @param notificationType Type of notification to be removed (File acquisition or Super node notification)
     */
    public abstract void removeNotification(int notificationType);

    @Override
    public void networkTaskStatusChanged(NetworkTask task) {
        if(task instanceof DownloadTask) {

        }else{


            int taskType = task.getTaskType();
            int taskId = task.getTaskId();
            if(currentTaskIndex[taskType] != -1
                    && taskId == tasksQueues[taskType].get(currentTaskIndex[taskType]).getTaskId()){
                int status = task.getStatus();
                switch(status) {
                    case NetworkTask.STATUS_COMPLETE:
                    case NetworkTask.STATUS_FAILED:
                    case NetworkTask.STATUS_STOPPED:
                        //task is finished
                        tasksQueues[taskType].remove(currentTaskIndex[taskType]);
                        currentTaskIndex[taskType] = -1;
                        checkTaskQueue(taskType);

                        /*
                         * If this task is associated with an availability monitor request, update the
                         * tracking information. We won't need to go and stop this task when the monitor
                         * is stopped if the task has actually already finished.
                         */
                        synchronized (availabilityMonitorRequests) {
                            if(availabilityMonitorTaskIdToRequestMap.containsKey(taskId)) {
                                AvailabilityMonitorRequest request =
                                        availabilityMonitorTaskIdToRequestMap.get(taskId);
                                availabilityMonitorTaskIdToRequestMap.remove(taskId);
                                List<Long> availabilityTaskIds = availabilityMonitorRequestToTaskIdMap.get(request);
                                if(availabilityTaskIds != null && availabilityTaskIds.contains(taskId)) {
                                    availabilityTaskIds.remove(availabilityTaskIds.indexOf(taskId));
                                }
                            }
                        }


                        break;
                    case NetworkTask.STATUS_RETRY_LATER:
                        //put task to back of queue
                        NetworkTask retryTask = tasksQueues[taskType].remove(currentTaskIndex[taskType]);
                        tasksQueues[taskType].addElement(retryTask);
                        currentTaskIndex[taskType] = -1;
                        checkTaskQueue(taskType);
                        break;
                }
            }

        }


        fireNetworkTaskStatusChanged(task);
    }

    public List<NetworkNode> getKnownNodes() {
        return knownNetworkNodes;
    }

    public Map<String,List<EntryCheckResponse>> getEntryResponses(){
        return entryResponses;
    }

    /**
     * Reset all information about known nodes on the network. Use with care.
     *
     */
    public void resetKnownNodeInfo() {
        knownNetworkNodes.clear();
        entryResponses.clear();
    }

    public Object getContext() {
        return mContext;
    }

    /**
     * Getting the current device IP address from the current connected network.
     * @return String: Current device IP address
     */
    public abstract String getDeviceIPAddress();


    public void handleWifiDirectGroupCreated(WiFiDirectGroup wiFiDirectGroup){
        fireWifiDirectGroupCreated(wiFiDirectGroup,null);
    }

    public void handleWifiDirectGroupRemoved(boolean isGroupRemoved){
        fireWifiDirectGroupRemoved(isGroupRemoved,null);
    }


    /**
     * Connect to the given wifi network. Use NetworkManagerListener.wifiConnectionChanged to
     * listen for a successful connection.  There is not necessarily any event for a failed connection
     * : tasks using this may have to use a timeout approach to detect failure.
     *
     * @param ssid SSID to connect to
     * @param passphrase Passphrase to use
     */
    public abstract void connectWifi(String ssid,String passphrase);

    /**
     * Called to "restore" the wifi connection to the 'normal' WiFi. Used when we have connected
     * to a WiFi direct group to restore the connection to the normal WiFi.
     */
    public abstract void restoreWifi();

    /**
     * Called
     */
    public abstract void disconnectWifi();

    /**
     * WARNING: This method is really here for testing purposes only. When enabled any call to the
     * getWifiDirectGroup method will provide an invalid ssid. The actual ssid will be postfixed
     * with "-mangle". Unfortunately putting this in the test package would require overriding
     * UstadMobileSystemImplFactory
     *
     * @hide
     * @param mangleWifiSsid
     */
    public void setMangleWifiDirectGroup(boolean mangleWifiSsid) {
        this.mangleWifiSsid = mangleWifiSsid;
    }

    /**
     * @hide
     * @return
     */
    protected boolean isMangleWifiDirectGroup() {
        return this.mangleWifiSsid;
    }


    public void setMangleBluetoothAddr(boolean mangleBluetoothAddr) {
        this.mangleBluetoothAddr = mangleBluetoothAddr;
    }

    protected boolean isMangleBluetoothAddr() {
        return mangleBluetoothAddr;
    }


    /**
     * Connect to the given ssid. This method will mark the connection as a temporary connection.
     *
     * This method will
     *  1. Check the current connectivity status and set a flag for what to do once we are finished
     *     with this connection (either disconnect if there is currently no active wifi network,
     *     or restore if there is an active wifi connection)
     *
     *  2. Connect to the given network
     *
     * @param ssid
     * @param passphrase
     */
    public void connectToWifiDirectGroup(String ssid, String passphrase){
        if(actionRequiredAfterGroupConnection == AFTER_GROUP_CONNECTION_DO_NOTHING){
            if(getCurrentWifiSsid() != null) {
                actionRequiredAfterGroupConnection = AFTER_GROUP_CONNECTION_RESTORE;
            }else {
                actionRequiredAfterGroupConnection = AFTER_GROUP_CONNECTION_DISCONNECT;
            }
        }

        connectWifi(ssid, passphrase);
    }

    /**
     * Can be overridden if needed so that the download task can get a socket factory that is
     * bound to the wifi network. When Android connects to a wifi network that has no Internet,
     * (e.g. a WiFi Direct legacy group network)
     *
     * @return
     */
    public SocketFactory getWifiSocketFactory(){
        return SocketFactory.getDefault();
    }

    public URLConnectionOpener getWifiUrlConnectionOpener(){
        return wifiUrlConnectionOpener;
    }

    /**
     * Connect to the given wifi direct node. This node should be the owner, and the other node
     * should be a client of the group.
     *
     * @param deviceAddress
     */
    public abstract void connectToWifiDirectNode(String deviceAddress);

    /**
     * Cancel all outgoing wifi direct connections e.g. group invitations
     */
    public abstract void cancelWifiDirectConnection();


    /**
     * Determine if the device is currently connected to a wifi direct legacy network or not -
     * e.g. the main wifi is connected to a network and the network has the prefix DIRECT-
     *
     * @return
     */
    public boolean isConnectedToWifiDirectLegacyGroup() {
        String ssid = getCurrentWifiSsid();
        return ssid != null && ssid.toUpperCase().startsWith(WIFI_DIRECT_GROUP_SSID_PREFIX);
    }

    /**
     * Gets the currently connected wifi ssid : if any
     *
     * @return
     */
    public abstract String getCurrentWifiSsid();



    /**
     * Create a new WiFi direct group on this device. A WiFi direct group
     * will create a new SSID and passphrase other devices can use to connect in "legacy" mode.
     *
     * The process is asynchronous and the WifiDirectGroupListener should be used to listen for
     * group creation.
     *
     * If a WiFi direct group is already under creation this method has no effect.
     */
    public abstract void createWifiDirectGroup();


    /**
     * Stop the WiFi direct group if it is active. If there is no group this method will have no effect.
     */
    public abstract void removeWiFiDirectGroup();


    /**
     * Gets the current active WiFi Direct group (if any)
     *
     * @return The active WiFi direct group (if any) - otherwise null
     */
    public abstract WiFiDirectGroup getWifiDirectGroup();



    /**
     * Add a WiFiDirectGroupListener to receive notifications for group
     * creation and removal
     *
     * @param listener Listener to add
     */
    public void addWifiDirectGroupListener(WiFiDirectGroupListener listener) {
        wifiDirectGroupListeners.add(listener);
    }

    /**
     * Remove a given WifiDirectGroupListener
     *
     * @param listener Listener to remove
     */
    public void removeWifiDirectGroupListener(WiFiDirectGroupListener listener) {
        wifiDirectGroupListeners.remove(listener);
    }

    /**
     * Method which will be firing event to all listening part of the app upon successful Wi-Fi Direct group creation.
     * @param group WiFiDirectGroup wrapper  with more information about the group
     * @param error Exception thrown.
     */
    protected void fireWifiDirectGroupCreated(WiFiDirectGroup group, Exception error) {
        synchronized (wifiDirectGroupListeners) {
            for(int i = 0; i < wifiDirectGroupListeners.size(); i++) {
                wifiDirectGroupListeners.get(i).groupCreated(group, error);
            }
        }
    }

    /**
     * Method which will be firing event to all listening part of the app upon successful removal of the Wi-Fi Direct group
     * @param successful removal status (If removed TRUE, otherwise FALSE)
     * @param error
     */
    protected void fireWifiDirectGroupRemoved(boolean successful, Exception error) {
        synchronized (wifiDirectGroupListeners) {
            for(int i = 0; i < wifiDirectGroupListeners.size(); i++) {
                wifiDirectGroupListeners.get(i).groupRemoved(successful, error);
            }
        }
    }


    /**
     * Method which is responsible for adding all acquisition listeners.
     * @param listener AcquisitionListener to listen to and fire events accordingly
     */
    @Deprecated
    public void addAcquisitionTaskListener(AcquisitionListener listener){
        acquisitionListeners.add(listener);
    }

    /**
     * Method which is responsible for removing all listeners added
     * @param listener
     */
    @Deprecated
    public void removeAcquisitionTaskListener(AcquisitionListener listener){
        acquisitionListeners.remove(listener);
    }


    /**
     * Get a network task by id and queue type
     *
     * @param taskId The task id
     * @param queueType
     *
     * @return
     */
    @Deprecated
    @Override
    public NetworkTask getTaskById(long taskId, int queueType) {
        if(taskId == -1)
            return null;//to save time - there is no task id -1

        synchronized (tasksQueues[queueType]) {
            for(int i = 0; i < tasksQueues[queueType].size(); i++) {
                if(tasksQueues[queueType].get(i).getTaskId() == taskId) {
                    return tasksQueues[queueType].get(i);
                }
            }
        }

        return null;
    }

//    public DownloadTask getActiveDownloadTask(int taskId){
//        return
//    }

    public void addConnectivityListener(ConnectivityListener listener) {
        connectivityListeners.add(listener);
    }

    public void removeConnectivityListener(ConnectivityListener listener) {
        connectivityListeners.remove(listener);
    }

    protected void fireOnConnectivityChanged(int state) {
        for(ConnectivityListener listener : connectivityListeners) {
            listener.onConnectivityChanged(state);
        }
    }

    protected void handleConnectivityChanged(int state) {
        setConnectivityState(state);
        fireOnConnectivityChanged(state);

        if(state == CONNECTIVITY_STATE_METERED || state == CONNECTIVITY_STATE_UNMETERED)
            checkDownloadJobQueueAsync(null);
    }

    protected synchronized void setConnectivityState(int connectivityState) {
        this.connectivityState = connectivityState;
    }

    public synchronized int getConnectivityState() {
        return connectivityState;
    }


    /**
     * Return the Entry ID to AcquisitionTask map
     * @return
     */
    public Map<String,DownloadTask> getEntryAcquisitionTaskMap(){
        return entryAcquisitionTaskMap;
    }


    /**
     * Returns the IP address of this device as used on Wifi Direct connections.
     *
     * @return The Wifi Direct IP address, or null if none
     */
    public abstract String getWifiDirectIpAddress();

    /**
     * Gets the current status of the Wifi direct group.  Will return
     * one of the WIFIDIRECT_GROUP_STATUS_  constants
     *
     * @return Wifi direct group status as per the constants
     */
    public abstract int getWifiDirectGroupStatus();

    /**
     * Clean up the network manager for shutdown
     */
    public void onDestroy() {
        mContext = null;
        if(httpd != null) {
            httpd.stop();
            httpd = null;
        }
    }

    /**
     * Mount a Zip File to the http server.  Optionally specify a preferred mount point (useful if
     * the activity is being created from a saved state)
     *
     * @param zipPath Path to the zip that should be mounted (mandatory)
     * @param mountName Directory name that this should be mounted as e.g. something.epub-timestamp. Can be null
     *
     * @return The mountname that was used - the content will then be accessible on getZipMountURL()/return value
     */
    public String mountZipOnHttp(String zipPath, String mountName, boolean epubFilterEnabled,
                                 String epubScriptToAdd) {
        UstadMobileSystemImpl.l(UMLog.INFO, 371, "Mount zip " + zipPath + " on service "
                + this + "httpd server = " + httpd + " listening port = " + httpd.getListeningPort());

        String extension = UMFileUtil.getExtension(zipPath);
        HashMap<String, List<MountedZipHandler.MountedZipFilter>> filterMap = null;

        if(extension != null && extension.endsWith("epub")) {
            filterMap = new HashMap<>();
            List<MountedZipHandler.MountedZipFilter> xhtmlFilterList = new ArrayList<>();
            MountedZipHandler.MountedZipFilter autoplayFilter = new MountedZipHandler.MountedZipFilter(
                    Pattern.compile("autoplay(\\s?)=(\\s?)([\"'])autoplay", Pattern.CASE_INSENSITIVE),
                    "data-autoplay$1=$2$3autoplay");
            xhtmlFilterList.add(autoplayFilter);
            filterMap.put("xhtml", xhtmlFilterList);
        }

        mountName = httpd.mountZip(zipPath, mountName, epubFilterEnabled, epubScriptToAdd);
        return mountName;
    }

    /**
     * Method which is responsible for unmounting zipped file from HTTP
     * @param mountName File mount name
     */
    public void unmountZipFromHttp(String mountName) {
        httpd.unmountZip(mountName);
    }

    /**
     * Method to access the ZipFile object once it's been mounted. This can be helpful for some
     * presenters and avoid reading the same file twice
     */
    public ZipFile getHttpMountedZip(String mountPath){
        return httpd.getMountedZip(mountPath);
    }

    /**
     * Method which is used to get HTTP service listening port.
     * @return int: Listening port
     */
    public int getHttpListeningPort() {
        return httpd.getListeningPort();
    }

    /**
     * Get the local HTTP server url with the URL as it is to be used for access over the loopback
     * interface
     *
     * @return Local http server url e.g. http://127.0.0.1:PORT/
     */
    public String getLocalHttpUrl() {
        return "http://127.0.0.1:" + getHttpListeningPort() + "/";
    }

    /**
     * Method which is responsible for sharing application setup file to other devices.
     * @param filePath Setup file absolute path
     * @param shareTitle Share dialog title
     */
    public abstract void shareAppSetupFile(String filePath, String shareTitle);

    /**
     * Sets the shared http endpoint catalog.
     *
     * @param sharedFeedUuid
     */
    public void setSharedFeed(String sharedFeedUuid) {
        synchronized (sharedFeedLock) {
            this.sharedFeedUuid = sharedFeedUuid;

            cancelStopSharedFeedHttpdTimerTask();
            if(sharedFeedUuid != null) {
                if(sharedFeedHttpd == null) {
                    sharedFeedHttpd = new RouterNanoHTTPD(SHARED_FEED_PORT);
                    try {
                        sharedFeedHttpd.start();
                        UstadMobileSystemImpl.l(UMLog.INFO, 302,
                                "setSharedFeed: Shared feed listening port = "
                                        + sharedFeedHttpd.getListeningPort());
                    }catch(IOException e) {
                        //TODO: If we can't start the http server - nothing will work, show error and give up
                        UstadMobileSystemImpl.l(UMLog.ERROR, 663, "setSendingOn: Exception starting http server");
                        sharedFeedHttpd = null;
                        return;
                    }
                }else {
                    sharedFeedHttpd.removeRoute("(.*)");
                }

                sharedFeedHttpd.addRoute("(.*)", SharedEntryResponder.class, sharedFeedUuid,
                    getContext(), getHttpListeningPort());
                updateClientServices();
            }else if(sharedFeedUuid == null) {
                UstadMobileSystemImpl.l(UMLog.INFO, 301, "setSharedFeed: shared feed is now null");
                updateClientServices();
                cancelStopSharedFeedHttpdTimerTask();
                submitCancelSharedFeedHttpdTimerTask();
            }

//            httpd.addRoute(CATALOG_HTTP_SHARED_URI, OPDSFeedUriResponder.class, sha);
        }
    }

    /**
     * The Uuid i
     * @return
     */
    public String getSharedFeed() {
        return sharedFeedUuid;
    }

    private void cancelStopSharedFeedHttpdTimerTask() {
        synchronized (sharedFeedLock) {
            if(stopSharedFeedHttpdTimerTask != null) {
                UstadMobileSystemImpl.l(UMLog.INFO, 366, "Cancel stop shared feed httpd");
                stopSharedFeedHttpdTimerTask.cancel();
                stopSharedFeedHttpdTimerTask = null;
            }
        }
    }

    private void submitCancelSharedFeedHttpdTimerTask() {
        synchronized (sharedFeedLock) {
            cancelStopSharedFeedHttpdTimerTask();
            stopSharedFeedHttpdTimerTask = new StopSharedFeedHttpdTimerTask();
            updateServicesTimer.schedule(stopSharedFeedHttpdTimerTask, SHARED_FEED_HTTPD_TIMEOUT);
            UstadMobileSystemImpl.l(UMLog.INFO, 368, "Submit cancel stop shared feed httpd task");
        }
    }

    @Override
    public void responseStarted(NanoHTTPD.Response response) {

    }

    @Override
    public void responseFinished(NanoHTTPD.Response response) {
        //The device we are sharing should have downloaded the course
        submitCancelSharedFeedHttpdTimerTask();
    }

    /**
     * Set the shared http endpoint catalog using an array of entry ids to be shared
     *
     * @param uuids
     */
    @Deprecated
    public void setSharedFeed(String[] uuids, String title) {
        //TODO: replace this hardcoded value with something generic that gets replaced by client

        //TODO: if there is already a shared feed, delete the old one from the database
        String feedSrcHref = "p2p://groupowner:" + getHttpListeningPort() + "/";

        OpdsEntryWithRelations sharedFeed = new OpdsEntryWithRelations();
        sharedFeed.setUrl(feedSrcHref);
        sharedFeed.setTitle(title);
        sharedFeed.setUuid(UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()));

        OpdsLink p2pSelfLink = new OpdsLink(sharedFeed.getUuid(), OpdsEntry.TYPE_OPDS_ACQUISITION_FEED,
                "p2p://groupowner:" + getHttpListeningPort() + "/", OpdsEntry.LINK_REL_P2P_SELF);

        List<OpdsEntryParentToChildJoin> joinList = new ArrayList<>();
        List<OpdsEntry> sharedEntries = new ArrayList<>();
        List<OpdsLink> sharedLinks = new ArrayList<>();
        sharedLinks.add(p2pSelfLink);

        dbExecutorService.execute(() -> {
            List<OpdsEntryWithRelationsAndContainerMimeType> entriesToShareSrc = UmAppDatabase.getInstance(getContext())
                    .getOpdsEntryWithRelationsDao().findByUuidsWithContainerMimeType(Arrays.asList(uuids));


            for(int i = 0; i < entriesToShareSrc.size(); i++) {
                OpdsEntryWithRelationsAndContainerMimeType entry = entriesToShareSrc.get(i);
                OpdsEntryWithRelations sharedEntry = new OpdsEntryWithRelations();
                sharedEntry.setUuid(UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()));
                sharedEntry.setTitle(entry.getTitle());
                sharedEntry.setEntryId(entry.getEntryId());

                if(entry.getContainerMimeType() == null) {
                    //for now - we can't share that
                    continue;
                }

                String entryIdEncoded = CatalogUriResponder.doubleUrlEncode(entry.getEntryId());

                OpdsLink link = new OpdsLink(sharedEntry.getUuid(), entry.getContainerMimeType(),
                        UMFileUtil.joinPaths(CATALOG_HTTP_ENDPOINT_PREFIX,
                                CatalogUriResponder.CONTAINER_DL_PATH_COMPONENT, entryIdEncoded),
                        OpdsEntry.LINK_REL_ACQUIRE);

                sharedEntries.add(sharedEntry);
                sharedLinks.add(link);
                joinList.add(new OpdsEntryParentToChildJoin(sharedFeed.getUuid(), sharedEntry.getUuid(),
                        i));
            }

            //persist to the database
            UmAppDatabase dbManager = UmAppDatabase.getInstance(getContext());
            dbManager.getOpdsEntryDao().insert(sharedFeed);
            dbManager.getOpdsEntryDao().insertList(sharedEntries);
            dbManager.getOpdsLinkDao().insert(sharedLinks);
            dbManager.getOpdsEntryParentToChildJoinDao().insertAll(joinList);
            setSharedFeed(sharedFeed.getUuid());
        });
    }

    /**
     * Reset the acquisition history of all known nodes. History is used to track failures so that
     * failure prone nodes are avoided as far as possible.
     *
     */
    public void clearNetworkNodeAcquisitionHistory() {
//        TODO: re-enable this for db based version
//        Iterator<NetworkNode> nodeIterator = getKnownNodes().iterator();
//        NetworkNode node;
//        while(nodeIterator.hasNext()) {
//            node = nodeIterator.next();
//            if(node.getAcquisitionHistory() != null)
//                node.getAcquisitionHistory().clear();
//        }
    }

    public int getActionRequiredAfterGroupConnection() {
        return actionRequiredAfterGroupConnection;
    }

    /**
     * To be implemented by the underlying platform.
     *
     * @return The timeout after which a connection request for a wifi network will be considered to
     * have failed (in ms).
     */
    public abstract int getWifiConnectionTimeout();


    public boolean isReceivingOn() {
        return receivingOn;
    }

    public void setReceivingOn(boolean receivingOn) {
        this.receivingOn = receivingOn;
        updateClientServices();
    }

    /**
     * Return info about this wifi direct device as a NetworkNode object
     *
     * @return
     */
    public abstract NetworkNode getThisWifiDirectDevice();

    public abstract String getWifiDirectGroupOwnerIp();

    /**
     * Check if there is an active wifi direct connection with the given other device
     *
     * @param otherDevice Mac address of the other device
     *
     * @return true if the given device is already a member of the same wifi direct group, false otherwise
     */
    public abstract boolean isWifiDirectConnectionEstablished(String otherDevice);

    /**
     * Share the given feed using WiFi direct to the specified destination mac address.
     *
     * @param feedUuid
     * @param destinationMacAddr
     */
    public void shareFeed(String feedUuid, String destinationMacAddr) {
        setSharedFeed(feedUuid);
        if(!isWifiDirectConnectionEstablished(destinationMacAddr))
            connectToWifiDirectNode(destinationMacAddr);
    }

    @Deprecated
    public void shareEntries(String[] uuids, String title, String destinationMacAddr) {
        setSharedFeed(uuids, title);
        if(!isWifiDirectConnectionEstablished(destinationMacAddr))
            connectToWifiDirectNode(destinationMacAddr);
    }

    /**
     * Get the OPDS feed that is being shared by the group owner, if any. This feed can then be
     * acquired the normal way using requestAcquisition
     *
     * @return
     */
    public OpdsEntryWithChildEntries getOpdsFeedSharedByWifiP2pGroupOwner()throws IOException, XmlPullParserException {
        String groupOwner = getWifiDirectGroupOwnerIp();
        if(groupOwner == null) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 664,
                    "getOpdsFeedSharedByWifiP2pGroupOwner: group owner ip is null");
            return null;
        }

        UstadMobileSystemImpl.l(UMLog.INFO, 700, "Found group owner: " + groupOwner);

        //now get the feed itself
        IOException ioe = null;
        XmlPullParserException xe = null;
        InputStream feedIn = null;
        OpdsEntryWithChildEntries feed = null;
        String feedUrl = "http://" + groupOwner + ":" + NetworkManager.SHARED_FEED_PORT +"/";


        HttpURLConnection feedConnection = null;
        try {
            URL feedUrlObj = new URL(feedUrl);
            feedConnection = (HttpURLConnection)feedUrlObj.openConnection(Proxy.NO_PROXY);
            feedConnection.setUseCaches(false);
            feedConnection.setConnectTimeout(15000);
            feedConnection.setReadTimeout(15000);
            feedIn = feedConnection.getInputStream();
            feed = new OpdsEntryWithChildEntries();
            feed.setUrl(feedUrl);
            XmlPullParser xpp = UstadMobileSystemImpl.getInstance().newPullParser(feedIn, "UTF-8");
            feed.load(xpp, null);
        }catch(IOException e) {
            ioe = e;
            UstadMobileSystemImpl.l(UMLog.ERROR, 665, "Exception loading opds shared feed", e);
        }catch(XmlPullParserException x) {
            xe = x;
            UstadMobileSystemImpl.l(UMLog.ERROR, 665, "Exception loading opds shared feed", x);
        }finally{
            UMIOUtils.closeInputStream(feedIn);
            UMIOUtils.throwIfNotNullIO(ioe);
            if(feedConnection != null)
                feedConnection.disconnect();

            if(xe != null)
                throw xe;
        }

        return feed;
    }

    /**
     * Simple utility method to check if a wifi direct mac address is in the given list
     *
     * @param list
     * @param macAddr
     * @return
     */
    public static boolean isMacAddrInList(List<NetworkNode> list, String macAddr) {
        if(list == null)
            return false;

        String nodeMacAddr;
        for(NetworkNode node : list) {
            nodeMacAddr = node.getWifiDirectMacAddress();
            if(nodeMacAddr != null && nodeMacAddr.equalsIgnoreCase(macAddr))
                return true;
        }

        return false;
    }

}
