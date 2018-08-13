package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.db.dao.DownloadJobItemHistoryDao;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.DownloadTaskListener;
import com.ustadmobile.core.networkmanager.NetworkManagerListener;
import com.ustadmobile.core.networkmanager.NetworkManagerTaskListener;
import com.ustadmobile.lib.db.entities.DownloadJobItemWithDownloadSetItem;
import com.ustadmobile.lib.db.entities.DownloadJobWithDownloadSet;
import com.ustadmobile.lib.db.entities.DownloadJobItemHistory;
import com.ustadmobile.lib.db.entities.EntryStatusResponseWithNode;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.port.sharedse.impl.http.CatalogUriResponder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static com.ustadmobile.core.networkmanager.NetworkManagerCore.CONNECTIVITY_STATE_DISCONNECTED;
import static com.ustadmobile.core.networkmanager.NetworkManagerCore.CONNECTIVITY_STATE_METERED;
import static com.ustadmobile.port.sharedse.networkmanager.BluetoothServer.CMD_SEPARATOR;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManager.DOWNLOAD_FROM_CLOUD;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManager.DOWNLOAD_FROM_PEER_ON_DIFFERENT_NETWORK;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManager.DOWNLOAD_FROM_PEER_ON_SAME_NETWORK;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManager.DOWNLOAD_FROM_PEER_WIFIDIRECT;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManager.NOTIFICATION_TYPE_ACQUISITION;

/**
 * <h1>AcquisitionTask</h1>
 *
 * Class which handles all file acquisition tasks.Also, it is responsible to decide where to
 * download a file whether is from the cloud, peer on the same network or peer on different network.
 *
 * Apart from that, this class is responsible to initiate bluetooth connection when file to be downloaded
 * is on peer from different network.
 *
 * @see com.ustadmobile.port.sharedse.networkmanager.BluetoothConnectionHandler
 * @see NetworkManagerListener
 * @see NetworkTask
 *
 * @author kileha3
 */
public class DownloadTask extends NetworkTask implements BluetoothConnectionHandler,
        NetworkManagerListener, ConnectivityListener{

    private DownloadJobWithDownloadSet downloadJob;

    private boolean allowMeteredDataUsage;

    private UmObserver<Boolean> allowMeteredDataUsageObserver;

    private UmLiveData<Boolean> allowMeteredDataUsageLiveData;

    private DownloadJobItemWithDownloadSetItem currentDownloadJobItem;

    private int currentEntryStatusCacheId;

    protected NetworkManagerTaskListener listener;

    private DownloadTaskListener downloadTaskListener;

    private static final int DOWNLOAD_TASK_UPDATE_TIME=500;

    @SuppressWarnings("FieldCanBeLocal")
    private String currentGroupIPAddress;

    private String currentGroupSSID;

    private Timer updateTimer;

    private String message = null;

    private EntryStatusResponseWithNode entryStatusResponse;

    private DownloadJobItemHistory currentJobItemHistory;

    private static final int MAXIMUM_ATTEMPT_COUNT = 10;

    private static final int WAITING_TIME_BEFORE_RETRY = 2 * 1000;

    private static final List<Integer> HTTP_HARD_ERROR = Arrays.asList(400, 401, 403, 404, 405,
            406, 410);

    private ResumableHttpDownload httpDownload=null;

    private String currentEntryTitle;

    /**
     * The task wants to connect to the "normal" wifi e.g. for download from the cloud or for
     * download from another peer on the same network
     */
    public static final String TARGET_NETWORK_NORMAL = "com.ustadmobile.network.normal";

    /**
     * The task wants to connect to another node not on the same network
     */
    private static final String TARGET_NETWORK_WIFIDIRECT_GROUP = "com.ustadmobile.network.connect";

    private static final String TARGET_NETWORK_MOBILE_DATA = "com.ustadmobile.network.mobiledata";

    /**
     * The task wants to use a "normal" wifi direct connection between two devices
     */
    private static final String TARGET_NETWORK_WIFIDIRECT = "com.ustadmobile.network.wifidirect";

    /**
     * The network that this task wants to connect with for the upcoming/current download
     */
    private String targetNetwork;

    /**
     * The url from which we are going to try downloading next.
     */
    private String currentDownloadUrl;

    private int currentDownloadMode;

    /**
     * Indicates if the task is currently waiting for a wifi connection to be established in order
     * to continue.
     */
    private boolean waitingForWifiConnection = true;

    protected NetworkManager networkManager;


    protected UmAppDatabase mDbManager;


    private static final int SAME_NET_SCORE = 600;
    private static final int WIFI_DIRECT_SCORE = 500;
    private static final int FAILED_NODE_SCORE = -400;

    public static final int FAILURE_MEMORY_TIME = 20 * 60 * 1000;

    private Timer wifiConnectTimeoutTimer;

    private TimerTask wifiConnectTimeoutTimerTask;

    private String currentExpectedMimeType;

    private ScheduledExecutorService executor;

    private ExecutorService dbExecutor;

    private final ReentrantLock statusLock = new ReentrantLock();

    private volatile boolean stopped;

    /**
     * Monitor file acquisition task progress and report it to the rest of the app (UI).
     * <p>
     *     Status will be updated only if the progress
     *     status is less than maximum progress which is 100,
     *     and time passed is less than DOWNLOAD_TASK_UPDATE_TIME
     * </p>
     */
    private class UpdateTimerTask extends TimerTask{
        public void run() {
            try {
                statusLock.lock();

                if(httpDownload != null && httpDownload.getTotalSize() > 0 && !isStopped()
                        && currentDownloadJobItem != null){

                    currentDownloadJobItem.setDownloadedSoFar(httpDownload.getDownloadedSoFar());
                    currentDownloadJobItem.setCurrentSpeed(httpDownload.getCurrentDownloadSpeed());
                    currentDownloadJobItem.setStatus(STATUS_RUNNING);
                    currentDownloadJobItem.setDownloadLength(httpDownload.getTotalSize());
                    mDbManager.getDownloadJobItemDao().updateDownloadJobItemStatus(currentDownloadJobItem);

                    mDbManager.getOpdsEntryStatusCacheDao().handleDownloadJobProgress(
                            currentEntryStatusCacheId, currentDownloadJobItem.getDownloadJobItemId());

                }
            }finally {
                statusLock.unlock();
            }

        }
    }

    private TimerTask updateTimerTask;

    //TODO: refactor this to use the scheduled executor
    private class WifiConnectTimeoutTimerTask extends TimerTask {
        @Override
        public void run() {
            UstadMobileSystemImpl.l(UMLog.WARN, 213, getLogPrefix() + ": wifi connect timeout.");
            DownloadTask.this.handleAttemptFailed();
        }
    }




    /**
     * Create file acquisition task
     * @param downloadJob downloadJob
     * @param networkManager NetworkManager reference which handle all network operations.
     */
    public DownloadTask(DownloadJobWithDownloadSet downloadJob, NetworkManager networkManager,
                        DownloadTaskListener downloadTaskListener, ExecutorService dbExecutor){
        super(networkManager, downloadJob.getDownloadJobId());
        mDbManager = UmAppDatabase.getInstance(networkManager.getContext());
        this.downloadJob = downloadJob;
        allowMeteredDataUsage = downloadJob.isAllowMeteredDataUsage();

        this.networkManager = networkManager;
        networkManager.addNetworkManagerListener(this);
        networkManager.addConnectivityListener(this);

        this.downloadTaskListener = downloadTaskListener;
        this.updateTimer = new Timer();
        this.dbExecutor = dbExecutor;
    }

    protected void executeIfNotStopped(Runnable runnable) {
        try {
            statusLock.lock();
            if(!isStopped())
                executor.execute(runnable);
        }finally {
            statusLock.unlock();
        }
    }


    public void handleAllowMeteredDataUsageUpdate(boolean allowMeteredDataUsage) {
        if(this.allowMeteredDataUsage != allowMeteredDataUsage) {
            //TODO: if metered data usage has just been turned off, and we're using it, we need to stop
            this.allowMeteredDataUsage = allowMeteredDataUsage;
        }
    }

    /**
     * Method which start file acquisition task
     */
    public void start() {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.execute(() -> {
            allowMeteredDataUsageObserver = this::handleAllowMeteredDataUsageUpdate;
            allowMeteredDataUsageLiveData = UmAppDatabase.getInstance(networkManager.getContext())
                    .getDownloadJobDao().findAllowMeteredDataUsageLive(downloadJob.getDownloadJobId());
            allowMeteredDataUsageLiveData.observeForever(allowMeteredDataUsageObserver);

            updateTimerTask = new UpdateTimerTask();
            updateTimer.scheduleAtFixedRate(updateTimerTask, DOWNLOAD_TASK_UPDATE_TIME,
                    DOWNLOAD_TASK_UPDATE_TIME);
            setStatus(STATUS_RUNNING);
            mDbManager.getDownloadJobDao().updateJobStatus(downloadJob.getDownloadJobId(),
                    STATUS_RUNNING);

            findNextDownloadJobItem();
            executeIfNotStopped(this::startNextDownload);
        });

    }



    /**
     * Cleanup: called when all downloads have been attempted and succeeded or permanently failed
     */
    protected void cleanup(int status) {
        try {
            statusLock.lock();

            //All entries complete
            networkManager.removeNotification(NOTIFICATION_TYPE_ACQUISITION);
            networkManager.removeNetworkManagerListener(this);
            networkManager.removeConnectivityListener(this);

            updateTimer.cancel();

            setWaitingForWifiConnection(false);

            allowMeteredDataUsageLiveData.removeObserver(allowMeteredDataUsageObserver);

            //TODO: Handle p2p downloads here
//        String feedSelfUrl = feed.getAbsoluteSelfLink().getHref();
//        if(feedSelfUrl.startsWith("p2p://")) {
//            networkManager.removeWiFiDirectGroup();
//        }


            if(wifiConnectTimeoutTimer != null) {
                wifiConnectTimeoutTimer.cancel();
                wifiConnectTimeoutTimer = null;
            }

            setStatus(status);
            downloadJob.setStatus(status);
            if(status == STATUS_COMPLETE || status == STATUS_FAILED)
                downloadJob.setTimeCompleted(System.currentTimeMillis());

            mDbManager.getDownloadJobDao().update(downloadJob);

            downloadTaskListener.handleDownloadTaskStatusChanged(this, getStatus());
        }finally {
            statusLock.unlock();
        }

    }

    /**
     * Method determine where to download the file from
     * (Cloud, Peer on the same network or Peer on different network)
     *
     * <p>
     *     If is from Cloud: initiate download task
     *            Peer on the same network: initiate download task
     *            Peer on different network: initiate bluetooth connection in order to trigger
     *            WiFi-Direct group creation on the host device
     * </p>
     *
     * MUST run on the executor thread
     */
    private void startNextDownload(){
        try {
            statusLock.lock();
            if (currentDownloadJobItem != null) {
                currentDownloadJobItem.setNumAttempts(currentDownloadJobItem.getNumAttempts() + 1);
                currentGroupSSID = null;

                UstadMobileSystemImpl.l(UMLog.INFO, 303, getLogPrefix() +
                        ": startNextDownload: DownloadJobItem: #" + currentDownloadJobItem.getDownloadJobItemId() +
                        " entry id: " + currentDownloadJobItem.getDownloadSetItem().getEntryId());
                setWaitingForWifiConnection(false);

                message = "Downloading...";

                currentEntryTitle = UmAppDatabase.getInstance(networkManager.getContext()).getOpdsEntryDao()
                        .findTitleByUuid(currentDownloadJobItem.getDownloadSetItem().getOpdsEntryUuid());
                networkManager.addNotification(NOTIFICATION_TYPE_ACQUISITION,
                        currentEntryTitle, message);

                String entryId = currentDownloadJobItem.getDownloadSetItem().getEntryId();


                UmAppDatabase dbManager = UmAppDatabase.getInstance(networkManager.getContext());
                List<EntryStatusResponseWithNode> statusResponses = dbManager.getEntryStatusResponseDao()
                        .findByEntryIdAndAvailability(entryId, true);

                entryStatusResponse = selectEntryStatusResponse(statusResponses,
                        dbManager.getDownloadJobItemHistoryDao());

                NetworkNode responseNode = entryStatusResponse != null ?
                        entryStatusResponse.getNetworkNode() : null;
                String currentSsid = networkManager.getCurrentWifiSsid();
                boolean wifiAvailable = currentSsid != null
                        || networkManager.getActionRequiredAfterGroupConnection() == NetworkManager.AFTER_GROUP_CONNECTION_RESTORE;

                OpdsEntryWithRelations entryWithRelations = dbManager.getOpdsEntryWithRelationsDao()
                        .getEntryByUuidStatic(currentDownloadJobItem.getDownloadSetItem().getOpdsEntryUuid());
                OpdsLink acquisitionLink = entryWithRelations.getAcquisitionLink(null, true);
                currentExpectedMimeType = acquisitionLink.getMimeType();
                String feedEntryAcquisitionUrl = acquisitionLink.getHref();
                if (!UMFileUtil.isUriAbsolute(feedEntryAcquisitionUrl)) {
                    if (entryWithRelations.getUrl() != null) {
                        feedEntryAcquisitionUrl = UMFileUtil.resolveLink(entryWithRelations.getUrl(),
                                feedEntryAcquisitionUrl);
                    } else {
                        String parentUrl = dbManager.getOpdsEntryWithRelationsDao()
                                .findParentUrlByChildUuid(entryWithRelations.getUuid());
                        if (parentUrl != null) {
                            feedEntryAcquisitionUrl = UMFileUtil.resolveLink(parentUrl,
                                    feedEntryAcquisitionUrl);
                        }
                    }
                }

                if (feedEntryAcquisitionUrl.startsWith("p2p://")) {
                    targetNetwork = TARGET_NETWORK_WIFIDIRECT;
                    currentDownloadUrl = feedEntryAcquisitionUrl.replace("p2p://", "http://");
                    String groupOwnerIp = networkManager.getWifiDirectGroupOwnerIp();
                    if (groupOwnerIp != null) {
                        currentDownloadUrl = currentDownloadUrl.replace("groupowner", groupOwnerIp);
                    } else {
                        //TODO: If this happens - try to reconnect to the group owner.
                        UstadMobileSystemImpl.l(UMLog.ERROR, 667, getLogPrefix() + " p2p download, group owner IP is null!");
                        executeIfNotStopped(this::handleAttemptFailed);
                    }
                } else if (downloadJob.getDownloadSet().isLanDownloadEnabled() && entryStatusResponse != null
                        && networkManager.getCurrentWifiSsid() != null
                        && responseNode.getTimeSinceNetworkServiceLastUpdated() < NetworkManager.ALLOWABLE_DISCOVERY_RANGE_LIMIT) {
                    targetNetwork = TARGET_NETWORK_NORMAL;
                    currentDownloadUrl = "http://" + entryStatusResponse.getNetworkNode().getIpAddress() + ":"
                            + entryStatusResponse.getNetworkNode().getPort() + "/catalog/container-dl/" +
                            CatalogUriResponder.doubleUrlEncode(entryId);
                    currentDownloadMode = DOWNLOAD_FROM_PEER_ON_SAME_NETWORK;
                } else if (downloadJob.getDownloadSet().isWifiDirectDownloadEnabled() && entryStatusResponse != null
                        && networkManager.isWiFiEnabled()
                        && responseNode.getTimeSinceWifiDirectLastUpdated() < NetworkManager.ALLOWABLE_DISCOVERY_RANGE_LIMIT) {
                    targetNetwork = TARGET_NETWORK_WIFIDIRECT_GROUP;
                    currentDownloadMode = DOWNLOAD_FROM_PEER_ON_DIFFERENT_NETWORK;
                } else if (wifiAvailable || downloadJob.getDownloadSet().isMobileDataEnabled()) {
                    //download from cloud
                    targetNetwork = wifiAvailable ? TARGET_NETWORK_NORMAL : TARGET_NETWORK_MOBILE_DATA;
                    currentDownloadUrl = feedEntryAcquisitionUrl;
                    currentDownloadMode = DOWNLOAD_FROM_CLOUD;
                } else {
                    //we're stuck -
                    UstadMobileSystemImpl.l(UMLog.INFO, 0, getLogPrefix() + " download over data disabled, no wifi available - cleanup and wait");
                    //TODO: change this for new status flags
                    cleanup(STATUS_WAITING_FOR_CONNECTION);
                    return;
                }

                currentJobItemHistory = new DownloadJobItemHistory(
                        entryStatusResponse != null ? entryStatusResponse.getNetworkNode() : null,
                        currentDownloadJobItem.getDownloadSetItem(), currentDownloadMode,
                        System.currentTimeMillis());

                UstadMobileSystemImpl.l(UMLog.INFO, 336, getLogPrefix() +
                        ": DownloadJobItem: #" + currentDownloadJobItem.getDownloadJobItemId() +
                        "  id " + currentDownloadJobItem.getDownloadSetItem().getEntryId() +
                        " Mode = " + currentDownloadMode + " target network = " + targetNetwork);

                if (targetNetwork.equals(TARGET_NETWORK_WIFIDIRECT)) {
                    UstadMobileSystemImpl.l(UMLog.INFO, 316, getLogPrefix() + " : use WiFi direct");
                    executeIfNotStopped( ()->
                        downloadCurrentFile(currentDownloadUrl, DOWNLOAD_FROM_PEER_WIFIDIRECT));
                } else if (targetNetwork.equals(TARGET_NETWORK_WIFIDIRECT_GROUP)) {
                    UstadMobileSystemImpl.l(UMLog.INFO, 316, getLogPrefix() + ": Connect bluetooth");
                    networkManager.connectBluetooth(entryStatusResponse.getNetworkNode().getBluetoothMacAddress()
                            , this);
                } else if (targetNetwork.equals(TARGET_NETWORK_NORMAL)) {
                    //String currentSsid = networkManager.getCurrentWifiSsid();
                    boolean isConnectedToWifiDirectGroup = networkManager.isConnectedToWifiDirectLegacyGroup();

                    if (currentSsid != null && !isConnectedToWifiDirectGroup) {
                        UstadMobileSystemImpl.l(UMLog.INFO, 316, getLogPrefix() + ": use current normal network");
                        executeIfNotStopped(() ->
                                downloadCurrentFile(currentDownloadUrl, currentDownloadMode));
                    } else {
                        UstadMobileSystemImpl.l(UMLog.INFO, 316, getLogPrefix() + ": restore wifi");
                        setWaitingForWifiConnection(true);
                        networkManager.restoreWifi();
                    }
                } else if (targetNetwork.equals(TARGET_NETWORK_MOBILE_DATA)) {
                    UstadMobileSystemImpl.l(UMLog.VERBOSE, 0, getLogPrefix() + " download via mobile data");
                    executeIfNotStopped(() ->
                            downloadCurrentFile(currentDownloadUrl, currentDownloadMode));
                } else {
                    UstadMobileSystemImpl.l(UMLog.CRITICAL, 0, getLogPrefix() + " invalid download outcome");
                }
            } else {
                UstadMobileSystemImpl.l(UMLog.INFO, 0, getLogPrefix() +
                        " no further DownloadJobItems found, complete");
                cleanup(STATUS_COMPLETE);
            }
        }finally {
            statusLock.unlock();
        }
    }

    /**
     * Find the next downloadjobitem. This is called before marking the previous download as finished
     * to avoid the UI showing entries as 'queued' in the time between one job finishing and the next
     * starting.
     */
    private void findNextDownloadJobItem() {
        try {
            statusLock.lock();
            currentDownloadJobItem =  mDbManager.getDownloadJobItemDao().findNextByDownloadJobAndStatusRange(
                    downloadJob.getDownloadJobId(), 0, NetworkTask.STATUS_RUNNING_MIN-1);
            if(currentDownloadJobItem != null) {
                currentEntryStatusCacheId = mDbManager.getOpdsEntryStatusCacheDao().findUidByEntryId(
                        currentDownloadJobItem.getDownloadSetItem().getEntryId());
                mDbManager.getOpdsEntryStatusCacheDao().handleDownloadJobStarted(currentEntryStatusCacheId);
            }
        }finally {
            statusLock.unlock();
        }
    }

    /**
     * Method which start file acquisition from the specified source.
     * @param fileUrl : File source URL
     * @param mode: Mode in which the file will be acquired as DOWNLOAD_FROM_CLOUD,
     *            DOWNLOAD_FROM_PEER_ON_SAME_NETWORK and DOWNLOAD_FROM_PEER_ON_DIFFERENT_NETWORK
     *
     * MUST be executed on executor
     */
    private void downloadCurrentFile(final String fileUrl, final int mode) {
        boolean downloadCompleted = false;
        File fileDestination;
        try {
            statusLock.lock();

            UstadMobileSystemImpl.l(UMLog.INFO, 317, getLogPrefix() + ":downloadCurrentFile: from "
                    + fileUrl + " mode: " + mode);
            String filename = UMFileUtil.appendExtensionToFilenameIfNeeded(UMFileUtil.getFilename(
                    fileUrl), currentExpectedMimeType);

            fileDestination = new File(downloadJob.getDownloadSet().getDestinationDir(), filename);
            mDbManager.getDownloadJobItemDao().updateDestinationFile(
                    currentDownloadJobItem.getDownloadJobItemId(), fileDestination.getAbsolutePath());




            networkManager.updateNotification(NOTIFICATION_TYPE_ACQUISITION, 0,
                    currentEntryTitle, message);
            httpDownload = new ResumableHttpDownload(fileUrl, fileDestination.getAbsolutePath());

            if (currentDownloadMode == DOWNLOAD_FROM_PEER_ON_DIFFERENT_NETWORK)
                httpDownload.setConnectionOpener(networkManager.getWifiUrlConnectionOpener());

            if (httpDownload.getTotalSize() > 0) {
                currentDownloadJobItem.setDownloadLength(httpDownload.getTotalSize());
            }
            currentDownloadJobItem.setStatus(STATUS_RUNNING);
            currentDownloadJobItem.setCurrentSpeed(0);
            currentDownloadJobItem.setDownloadedSoFar(httpDownload.getDownloadedSoFar());
            mDbManager.getDownloadJobItemDao().updateDownloadJobItemStatus(currentDownloadJobItem);

        }finally {
            statusLock.unlock();
        }

        try {
            downloadCompleted = httpDownload.download();
        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 661, getLogPrefix() + " : jobitem #" +
                    currentDownloadJobItem.getDownloadSetItem() + " : IOException", e);
        }

        try {
            statusLock.lock();

            //if the task was stopped during download, everything has been handled by the stop() method
            if(isStopped()) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 0, getLogPrefix() +
                        " stopped during download - aborting downloadCurrentFile");
                return;
            }

            currentJobItemHistory.setEndTime(System.currentTimeMillis());
            currentJobItemHistory.setSuccessful(downloadCompleted);
            mDbManager.getDownloadJobItemHistoryDao().insert(currentJobItemHistory);

            currentDownloadJobItem.setStatus(downloadCompleted ? STATUS_COMPLETE : STATUS_RUNNING);
            currentDownloadJobItem.setDownloadedSoFar(httpDownload.getDownloadedSoFar());
            mDbManager.getDownloadJobItemDao().updateDownloadJobItemStatus(currentDownloadJobItem);
            mDbManager.getDownloadJobItemDao().updateNumAttempts(
                    currentDownloadJobItem.getDownloadJobItemId(),
                    currentDownloadJobItem.getNumAttempts());

            if(downloadCompleted) {
                UstadMobileSystemImpl.l(UMLog.INFO, 3010, getLogPrefix() +  " : item " +
                        " : Download completed successfully, saved to " + fileDestination.getAbsolutePath());
                findNextDownloadJobItem();

                UstadMobileSystemImpl.getInstance().getOpdsAtomFeedRepository(networkManager.getContext())
                        .findEntriesByContainerFileNormalizedPath(fileDestination.getAbsolutePath());
                UstadMobileSystemImpl.l(UMLog.INFO, 3010, getLogPrefix() +  " : item " +
                        " : indexed in database");

                UstadMobileSystemImpl.l(UMLog.INFO, 0, getLogPrefix() +
                    " download complete - calling executeIfNotStopped startNextDownload");
                executeIfNotStopped(this::startNextDownload);
            }else{
                UstadMobileSystemImpl.l(UMLog.ERROR, 660, getLogPrefix() + " : item " +
                        " : Download did not complete, download is not stopped, handle failed attempt");
                executeIfNotStopped(this::handleAttemptFailed);
            }
        }finally {
            statusLock.unlock();
        }
    }

    private void handleAttemptFailed() {
        try {
            statusLock.lock();

            UstadMobileSystemImpl.l(UMLog.DEBUG, 650, getLogPrefix() + " handleAttemptFailed.");

            setWaitingForWifiConnection(false);

            if((httpDownload == null || !HTTP_HARD_ERROR.contains(httpDownload.getResponseCode()))
                && currentDownloadJobItem.getNumAttempts() < MAXIMUM_ATTEMPT_COUNT){
                UstadMobileSystemImpl.l(UMLog.DEBUG, 650, getLogPrefix()
                        + " handleAttemptFailed - waiting " + WAITING_TIME_BEFORE_RETRY + "ms then retrying.");

                Callable<Void> retryCallable = () -> {
                    findNextDownloadJobItem();
                    startNextDownload();
                    return null;
                };

                executor.schedule(retryCallable, WAITING_TIME_BEFORE_RETRY,
                        TimeUnit.MILLISECONDS);
            }else {
                //retry count exceeded - move on to next file
                UstadMobileSystemImpl.l(UMLog.DEBUG, 650, getLogPrefix()
                        + " handleAttemptFailed - attempt retry count exceeded, or hard error response code - moving to next item");
                mDbManager.getDownloadJobItemDao().updateStatus(
                        currentDownloadJobItem.getDownloadJobItemId(), NetworkTask.STATUS_FAILED);
                executeIfNotStopped(() -> {
                    findNextDownloadJobItem();
                    startNextDownload();
                });
            }
        }finally {
            statusLock.unlock();
        }


    }

    /**
     * Handle when we have a bluetooth connection with a node that we intend to download from,
     * and request a WiFi direct group network.
     *
     * @param inputStream InputStream to read data from.
     * @param outputStream OutputStream to write data to.
     */
    @Override
    public void onBluetoothConnected(final InputStream inputStream, final OutputStream outputStream) {
        UstadMobileSystemImpl.l(UMLog.INFO, 317, getLogPrefix() + ": bluetooth connected");
        String acquireCommand = BluetoothServer.CMD_ACQUIRE_ENTRY +" "+networkManager.getDeviceIPAddress()+"\n";
        String response;
        String passphrase = null;
        targetNetwork = null;
        try {
            outputStream.write(acquireCommand.getBytes());
            outputStream.flush();
            System.out.print("AcquisitionTask: Sending Command "+acquireCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            response = reader.readLine();
            if(response.startsWith(BluetoothServer.CMD_ACQUIRE_ENTRY_FEEDBACK)) {
                System.out.print("AcquisitionTask: Receive Response "+response);
                String [] groupInfo=response.substring((BluetoothServer.CMD_ACQUIRE_ENTRY_FEEDBACK.length()+1),
                        response.length()).split(CMD_SEPARATOR);
                currentGroupIPAddress =groupInfo[2].replace("/","");
                currentGroupSSID =groupInfo[0];
                passphrase = groupInfo[1];
                currentDownloadUrl = "http://" + currentGroupIPAddress + ":" +
                        entryStatusResponse.getNetworkNode().getPort() + "/catalog/container-dl/" +
                        CatalogUriResponder.doubleUrlEncode(currentDownloadJobItem.getDownloadSetItem()
                                .getEntryId());

                UstadMobileSystemImpl.l(UMLog.INFO, 318, getLogPrefix() + ": bluetooth says connect to '" +
                    currentGroupSSID + "' download Url = " + currentDownloadUrl);
            }
        }catch(IOException e) {
            e.printStackTrace();
        }finally {
            UMIOUtils.closeInputStream(inputStream);
            UMIOUtils.closeOutputStream(outputStream);
        }

        if(isStopped()) {
            UstadMobileSystemImpl.l(UMLog.DEBUG, 650, getLogPrefix() +
                    "onBluetoothConnected: task has been stopped - aborting");
        }else  if(currentGroupSSID != null && passphrase != null) {
            String currentSsid = networkManager.getCurrentWifiSsid();
            if(currentSsid != null && currentGroupSSID.equals(currentSsid)) {
                UstadMobileSystemImpl.l(UMLog.INFO, 318,
                        getLogPrefix() + ": already connected to WiFi direct group network: '" +
                    currentSsid + "' - continuing");
                downloadCurrentFile(currentDownloadUrl, currentDownloadMode);
            }else {
                UstadMobileSystemImpl.l(UMLog.INFO, 319,
                        getLogPrefix() + ": connecting to WiFi direct group network : '" + currentGroupSSID+
                    "' - requesting connection");
                setWaitingForWifiConnection(true);
                networkManager.connectToWifiDirectGroup(currentGroupSSID, passphrase);
            }
        }else {
            UstadMobileSystemImpl.l(UMLog.ERROR, 662, getLogPrefix() +
                    " bluetooth connection did not provide group ssid and passphrase");
            executeIfNotStopped(this::handleAttemptFailed);
        }
    }

    @Override
    public void onBluetoothConnectionFailed(Exception exception) {
        UstadMobileSystemImpl.l(UMLog.ERROR, 77, getLogPrefix() + " bluetooth connection failed");
        executeIfNotStopped(this::handleAttemptFailed);
    }

    /**
     * Stop the DownloadTask. This will immediately flush everything that has been downloaded so far
     * to the output file, and close the output file. If a download is ongoing, it will be
     *
     * @param statusAfterStopped The status to set the DownloadJob to after the task has been stopped.
     *                           Only the DownloadJob stautus will be changed, not the DownloadJobItem
     *                           for any items that are part of the download job.
     */
    @Override
    public void stop(int statusAfterStopped) {
        try {
            statusLock.lock();

            UstadMobileSystemImpl.l(UMLog.INFO, 321, getLogPrefix() + " task stop called" +
                    " setting status to: " + statusAfterStopped);
            if(isStopped()) {
                UstadMobileSystemImpl.l(UMLog.INFO, 321, getLogPrefix() + " already stopped.");
                return;
            }

            setStopped(true);
            executor.shutdownNow();

            if(httpDownload != null) {
                long bytesSoFar = httpDownload.stop();
                currentDownloadJobItem.setDownloadedSoFar(bytesSoFar);

                currentJobItemHistory.setEndTime(System.currentTimeMillis());
                currentJobItemHistory.setSuccessful(true);
                mDbManager.getDownloadJobItemHistoryDao().insert(currentJobItemHistory);
            }

            cleanup(statusAfterStopped);
        }finally {
            statusLock.unlock();
        }
    }

    @Override
    public int getQueueId() {
        return this.queueId;
    }

    @Override
    public int getTaskType() {
        return this.taskType;
    }

    @Override
    public void fileStatusCheckInformationAvailable(String[] fileIds) {

    }

    @Override
    public void networkTaskStatusChanged(NetworkTask task) {

    }

    @Override
    public void networkNodeDiscovered(NetworkNode node) {

    }

    @Override
    public void networkNodeUpdated(NetworkNode node) {

    }

    @Override
    public void fileAcquisitionInformationAvailable(String entryId, long downloadId, int downloadSource) {

    }

    @Override
    public void wifiConnectionChanged(String ssid, boolean connected, boolean connectedOrConnecting) {
        //TODO: this should call download on the executor.
        UstadMobileSystemImpl.l(UMLog.INFO, 320, getLogPrefix()+ ": wifiConnectionChanged : " +
                " ssid: " + ssid + " connected: " + connected + " connectedOrConnecting: " +
                connectedOrConnecting);

        try {
            statusLock.lock();

            if(!isWaitingForWifiConnection())
                return;

            if(connected && targetNetwork != null && ssid != null
                    && targetNetwork.equals(TARGET_NETWORK_NORMAL)) {
                UstadMobileSystemImpl.l(UMLog.INFO, 321, getLogPrefix() + ": 'normal' network restored - continue download");
                setWaitingForWifiConnection(false);
                downloadCurrentFile(currentDownloadUrl, currentDownloadMode);
            }

            if(connected && currentGroupSSID != null){
                if(currentGroupSSID.equals(ssid)) {
                    UstadMobileSystemImpl.l(UMLog.INFO, 322, getLogPrefix()
                            + ": requested WiFi direct group connection activated");
                    setWaitingForWifiConnection(false);
                    executeIfNotStopped( () -> downloadCurrentFile(currentDownloadUrl, currentDownloadMode));
                }else {
                    UstadMobileSystemImpl.l(UMLog.INFO, 322, getLogPrefix()
                            + ": requested WiFi direct group connection failed : connected to other network");
                    executeIfNotStopped(this::handleAttemptFailed);
                }
            }
        }finally {
            statusLock.unlock();
        }

    }

    @Override
    public void onConnectivityChanged(int newState) {
        UstadMobileSystemImpl.l(UMLog.DEBUG, 0, getLogPrefix() +
            "onConnectivityChanged: new state : " + newState);
        UstadMobileSystemImpl.l(UMLog.DEBUG, 0, getLogPrefix() +
            " status locked? : " + statusLock.isLocked());
        dbExecutor.execute(() -> {
            try {
                statusLock.lock();

                if(isWaitingForWifiConnection()) {
                    UstadMobileSystemImpl.l(UMLog.ERROR, 0, getLogPrefix() +
                            " onConnectivityChanged: actually waiting for WiFi - do nothing");
                    return;
                }

                if(newState == CONNECTIVITY_STATE_DISCONNECTED
                        || (newState == CONNECTIVITY_STATE_METERED && !allowMeteredDataUsage)) {
                    UstadMobileSystemImpl.l(UMLog.ERROR, 0, getLogPrefix() +
                            " onConnectivityChanged: disconnected or metered data only on a unmetered only job");
                    stop(NetworkTask.STATUS_WAITING_FOR_CONNECTION);
                    List<DownloadJobItemWithDownloadSetItem> pausedItems = mDbManager.getDownloadJobItemDao()
                            .findByDownloadJobAndStatusRange(downloadJob.getDownloadJobId(),
                                    NetworkTask.STATUS_WAITING_MIN, NetworkTask.STATUS_COMPLETE_MIN-1);
                    UstadMobileSystemImpl.l(UMLog.ERROR, 0,
                            " setting waiting for connection status on " + pausedItems.size() +  " job items");
                    for(DownloadJobItemWithDownloadSetItem pausedItem : pausedItems) {
                        mDbManager.getDownloadJobItemDao().updateStatus(pausedItem.getDownloadJobItemId(),
                                NetworkTask.STATUS_WAITING_FOR_CONNECTION);
                        mDbManager.getOpdsEntryStatusCacheDao().handleContainerDownloadWaitingForNetwork(
                                pausedItem.getDownloadSetItem().getEntryId());
                    }
                }
            }finally {
                statusLock.unlock();
            }
        });
    }

    private boolean isWaitingForWifiConnection() {
        return waitingForWifiConnection;
    }

    /**
     * Sometimes this task needs to change the wifi connection in order to continue. It *MUST*
     * set this flag so that the event listener observing wifi connections will know to continue
     *
     * @param waitingForWifiConnection true if we are waiting for a wifi connection, false otherwise
     */
    private void setWaitingForWifiConnection(boolean waitingForWifiConnection) {
        this.waitingForWifiConnection = waitingForWifiConnection;

        if(wifiConnectTimeoutTimerTask != null) {
            wifiConnectTimeoutTimerTask.cancel();
            wifiConnectTimeoutTimerTask = null;
        }

        if(waitingForWifiConnection) {
            if(wifiConnectTimeoutTimer == null)
                wifiConnectTimeoutTimer = new Timer();

            wifiConnectTimeoutTimerTask = new WifiConnectTimeoutTimerTask();
            wifiConnectTimeoutTimer.schedule(wifiConnectTimeoutTimerTask,
                    networkManager.getWifiConnectionTimeout());
        }
    }

    /**
     * Selects the optimal entry check response to download if any
     *
     * @param responses responses to sort examine
     * @return the top scored response to attempt downloading from
     */
    public EntryStatusResponseWithNode selectEntryStatusResponse(List<EntryStatusResponseWithNode> responses,
                                                                DownloadJobItemHistoryDao historyDao) {
        if(responses == null || responses.size() == 0) {
            return null;
        }

        if(responses.size() == 1) {
            return scoreEntryStatusResponse(responses.get(0), new HashMap<>(), historyDao) > 0 ?
                    responses.get(0) : null;
        }

        final HashMap<EntryStatusResponseWithNode, Integer> entryCheckScores = new HashMap<>();

        final HashMap<Integer, List<DownloadJobItemHistory>> nodeHistoryCache = new HashMap<>();
        ArrayList<EntryStatusResponseWithNode> listToSort = new ArrayList<>(responses);


        //noinspection Java8ListSort
        Collections.sort(listToSort, (response1, response2) -> {
            if(!entryCheckScores.containsKey(response1))
                entryCheckScores.put(response1, scoreEntryStatusResponse(response1, nodeHistoryCache,
                        historyDao));

            if(!entryCheckScores.containsKey(response2))
                entryCheckScores.put(response2, scoreEntryStatusResponse(response2, nodeHistoryCache,
                        historyDao));

            return entryCheckScores.get(response2) - entryCheckScores.get(response1);
        });


        EntryStatusResponseWithNode bestResponse = listToSort.get(0);
        return bestResponse != null && entryCheckScores.get(bestResponse) >0 ? bestResponse : null;
    }

    private int scoreEntryStatusResponse(EntryStatusResponseWithNode response,
                                         HashMap<Integer, List<DownloadJobItemHistory>> nodeHistoryCache,
                                         DownloadJobItemHistoryDao historyDao) {
        if(!response.isAvailable())
            return 0;

        int score = 0;

        NetworkNode node = response.getNetworkNode();
        if(node == null)
            return -1;

        if(node.isNsdActive()) {
            score += SAME_NET_SCORE;
        }else if(node.isWifiDirectActive()) {
            score += WIFI_DIRECT_SCORE;
        }

        List<DownloadJobItemHistory> itemHistoryList = nodeHistoryCache.get(node.getNodeId());
        //computeIfAbsent is not actually supported on API < 21
        //noinspection Java8MapApi
        if(itemHistoryList == null) {
            itemHistoryList = historyDao.findHistoryItemsByNetworkNodeSince(node.getNodeId(),
                    System.currentTimeMillis() - FAILURE_MEMORY_TIME);
            nodeHistoryCache.put(node.getNodeId(), itemHistoryList);
        }

        for(DownloadJobItemHistory itemHistory : itemHistoryList) {
            if(!itemHistory.isSuccessful()) {
                long timeSinceFail = Calendar.getInstance().getTimeInMillis() - itemHistory.getEndTime();
                score += (1 - Math.min((float)timeSinceFail / FAILURE_MEMORY_TIME, 1)) * FAILED_NODE_SCORE;
            }
        }

        return score;
    }


    private String getLogPrefix() {
        int itemId = currentDownloadJobItem != null ? currentDownloadJobItem.getDownloadJobItemId() : -1;
        int numAttempts = currentDownloadJobItem != null ? currentDownloadJobItem.getNumAttempts() : -1;
        return "DownloadTask (" + System.identityHashCode(this) + ") #"  + getTaskId() +
                " Item id# " + itemId + " Attempt # " + numAttempts;
    }

    @Override
    protected void setStopped(boolean stopped) {
        try {
            statusLock.lock();
            this.stopped = stopped;
        }finally {
            statusLock.unlock();
        }

    }

    @Override
    public boolean isStopped() {
        try {
            statusLock.lock();
            return stopped;
        }finally {
            statusLock.unlock();
        }

    }
}
