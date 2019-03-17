package com.ustadmobile.port.sharedse.networkmanager;

import com.google.gson.Gson;
import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.db.WaitForLiveData;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.lib.db.entities.ConnectivityStatus;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5;
import com.ustadmobile.lib.db.entities.DownloadJobItemHistory;
import com.ustadmobile.lib.db.entities.DownloadJobItemWithDownloadSetItem;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.port.sharedse.container.ContainerManager;
import com.ustadmobile.port.sharedse.impl.http.IContainerEntryListService;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static com.ustadmobile.lib.db.entities.ConnectivityStatus.STATE_DISCONNECTED;
import static com.ustadmobile.lib.db.entities.ConnectivityStatus.STATE_METERED;
import static com.ustadmobile.lib.db.entities.DownloadJobItemHistory.MODE_CLOUD;
import static com.ustadmobile.lib.db.entities.DownloadJobItemHistory.MODE_LOCAL;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_CREATION_RESPONSE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_REQUEST;

/**
 * Class which handles all file downloading tasks, it reacts to different status as changed
 * in the Db from either UI or Network change.
 *
 * i.e Decides where to get the file based on the entry status response,
 * connecting to the peer device via BLE and WiFiP2P for the actual download
 * and Change its status based on Network status.
 *
 * @author kileha3
 */
public class DownloadJobItemRunner implements Runnable {

    private NetworkManagerBle networkManager;

    private UmAppDatabase appDb;

    private UmAppDatabase appDbRepo;

    private DownloadJobItemWithDownloadSetItem downloadItem;

    private String endpointUrl;

    static final String CONTAINER_ENTRY_LIST_PATH = "ContainerEntryList/findByContainerWithMd5";

    static final String CONTAINER_ENTRY_FILE_PATH = "ContainerEntryFile/";

    private UmLiveData<ConnectivityStatus> statusLiveData;

    private UmObserver<ConnectivityStatus> statusObserver;

    //TODO: enable switching to local download when available after basic p2p cases complete
    //private UmObserver<EntryStatusResponse> entryStatusObserver;

    //private UmLiveData<EntryStatusResponse> entryStatusLiveData;

    private UmObserver<Integer> downloadJobItemObserver;

    private UmLiveData<Integer> downloadJobItemLiveData;

    private UmLiveData<Boolean> downloadSetConnectivityData;

    private UmObserver<Boolean> downloadSetConnectivityObserver;

    private volatile ResumableHttpDownload httpDownload;

    private AtomicReference<ResumableHttpDownload> httpDownloadRef;

    private AtomicLong completedEntriesBytesDownloaded = new AtomicLong();

    private Timer statusCheckTimer = new Timer();

    private AtomicInteger runnerStatus = new AtomicInteger(JobStatus.NOT_QUEUED);

    private ConnectivityStatus connectivityStatus;

    private AtomicInteger meteredConnectionAllowed = new AtomicInteger(-1);

    private int lWiFiConnectionTimeout = 30;

    private Object context;

    private AtomicReference<WiFiDirectGroupBle> wiFiDirectGroupBle = new AtomicReference<>();

    private NetworkNode currentNetworkNode;

    /**
     * Boolean to indicate if we are waiting for a local connection.
     */
    private AtomicBoolean waitingForLocalConnection = new AtomicBoolean(false);

    private static final int BAD_PEER_FAILURE_THRESHOLD = 2;

    private static final int CONNECTION_TIMEOUT = 60;

    private final Object downloadWiFiLock = new Object();

    private IContainerEntryListService containerEntryListService;

    private String destinationDir;

    /**
     * Timer task to keep track of the download status
     */
    private class StatusCheckTask extends TimerTask{

        @Override
        public void run() {
            ResumableHttpDownload httpDownload  = httpDownloadRef.get();
            if(httpDownload != null && runnerStatus.get() == JobStatus.RUNNING) {
                long bytesSoFar = completedEntriesBytesDownloaded.get() +
                        httpDownload.getDownloadedSoFar();
                appDb.getDownloadJobItemDao().updateDownloadJobItemProgress(
                        downloadItem.getDjiUid(), bytesSoFar,
                        httpDownload.getCurrentDownloadSpeed());
                appDb.getDownloadJobDao().updateBytesDownloadedSoFar
                        (downloadItem.getDjiDjUid(),
                                null);
            }
        }
    }


    /**
     * Constructor to be used when creating new instance of the runner.
     * @param downloadItem Item to be downloaded
     * @param networkManager BLE network manager for network operation controls.
     * @param appDb Application database instance
     * @param endpointUrl Endpoint to get the file from.
     */
    public DownloadJobItemRunner(Object context,DownloadJobItemWithDownloadSetItem downloadItem,
                                 NetworkManagerBle networkManager, UmAppDatabase appDb,
                                 UmAppDatabase appDbRepo,
                                 String endpointUrl, ConnectivityStatus initialConnectivityStatus) {
        this.networkManager = networkManager;
        this.downloadItem = downloadItem;
        this.appDb = appDb;
        this.appDbRepo = appDbRepo;
        this.endpointUrl = endpointUrl;
        this.context = context;
        this.httpDownloadRef = new AtomicReference<>();
        this.connectivityStatus = initialConnectivityStatus;

        //Note: the url is passed as a parameter at runtime
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://localhost/dummy/").build();
        containerEntryListService = retrofit.create(IContainerEntryListService.class);
    }


    public void setWiFiConnectionTimeout(int lWiFiConnectionTimeout) {
        this.lWiFiConnectionTimeout = lWiFiConnectionTimeout;
    }


    /**
     * Handle changes triggered when connectivity status changes.
     * @param newStatus changed connectivity status
     */
    private void handleConnectivityStatusChanged(ConnectivityStatus newStatus) {
        this.connectivityStatus = newStatus;
        UstadMobileSystemImpl.l(UMLog.DEBUG, 699, mkLogPrefix() +
                " Connectivity state changed: " + newStatus);
        if(waitingForLocalConnection.get())
            return;

        if(connectivityStatus != null ){
            switch(newStatus.getConnectivityState()) {
                case STATE_METERED:
                    if(meteredConnectionAllowed.get() == 0) {
                        stopAsync(JobStatus.WAITING_FOR_CONNECTION);
                    }
                    break;

                case STATE_DISCONNECTED:
                    stopAsync(JobStatus.WAITING_FOR_CONNECTION);
                    break;

                //TODO: check CONNECTING_LOCAL - if the status changed, but we are not the job that asked for that
            }
        }
    }

    /**
     * Handle changes triggered when Download set metered connection flag changes
     * @param meteredConnection changed metered connection flag.
     */
    private void handleDownloadSetMeteredConnectionAllowedChanged(Boolean meteredConnection){
        if(meteredConnection != null){
            meteredConnectionAllowed.set(meteredConnection ? 1 : 0);
            if(meteredConnectionAllowed.get() == 0 && connectivityStatus != null
                    && connectivityStatus.getConnectivityState() == STATE_METERED) {
                UstadMobileSystemImpl.l(UMLog.DEBUG, 699, mkLogPrefix() +
                        " : no longer allowed to run on metered network - stopping");
                stopAsync(JobStatus.WAITING_FOR_CONNECTION);
            }
        }
    }

    /**
     * Handle changes triggered when the download job item status changes
     * @param newDownloadStatus changed download job item status
     */

    private void handleDownloadJobItemStatusChanged(Integer newDownloadStatus){
        if(newDownloadStatus != null && newDownloadStatus == JobStatus.STOPPING){
            stopAsync(JobStatus.STOPPED);
        }
    }

    //TODO: re-enable when we add support for switching dynamically
//    /**
//     * Handle changes triggered when file which wasn't available locally changes
//     * @param entryStatusResponse new file entry status
//     */
//    private void handleContentEntryFileStatus(EntryStatusResponse entryStatusResponse){
//        if(entryStatusResponse != null){
//            availableLocally.set(entryStatusResponse.isAvailable() ? 1:0);
//            if(availableLocally.get() == 1 && currentEntryStatusResponse!= null
//                    && !currentEntryStatusResponse.isAvailable()){
//                this.currentNetworkNode =
//                        appDb.getNetworkNodeDao().findNodeById(entryStatusResponse.getErNodeId());
//                connectToLocalNodeNetwork();
//            }
//        }
//    }


    /**
     * Stop download task Async
     * @param newStatus net status
     */
    private void stopAsync(int newStatus){
        runnerStatus.set(JobStatus.STOPPING);
        new Thread(() -> stop(newStatus)).start();
    }

    /**
     * Stop the download task from continuing (if not already stopped). Calling stop for a second
     * time will have no effect.
     *
     * @param newStatus new status to be set
     */
    private void stop(int newStatus) {
        if(runnerStatus.get() != JobStatus.STOPPED){
            runnerStatus.set(JobStatus.STOPPED);

            if(httpDownload != null){
                httpDownload.stop();
            }

            statusLiveData.removeObserver(statusObserver);
            downloadJobItemLiveData.removeObserver(downloadJobItemObserver);
            downloadSetConnectivityData.removeObserver(downloadSetConnectivityObserver);
            //entryStatusLiveData.removeObserver(entryStatusObserver);

            statusCheckTimer.cancel();

            updateItemStatus(newStatus);
            appDb.getDownloadJobDao().updateJobStatusToCompleteIfAllItemsAreCompleted(
                    downloadItem.getDjiDjUid());
            networkManager.releaseWifiLock(this);
        }
    }


    @Override
    public void run() {
        runnerStatus.set(JobStatus.RUNNING);
        updateItemStatus(JobStatus.RUNNING);
        long downloadJobId = appDb.getDownloadJobDao().getLatestDownloadJobUidForDownloadSet(
                downloadItem.getDownloadSetItem().getDsiDsUid());
        appDb.getDownloadJobDao().update(downloadJobId, JobStatus.RUNNING);

        networkManager.startMonitoringAvailability(this,
                Arrays.asList(downloadItem.getDjiContainerUid()));

        statusLiveData = appDb.getConnectivityStatusDao().getStatusLive();
        downloadJobItemLiveData = appDb.getDownloadJobItemDao().getLiveStatus(downloadItem.getDjiUid());

        //get the download set
        downloadSetConnectivityData =
                appDb.getDownloadSetDao().getLiveMeteredNetworkAllowed(downloadItem
                        .getDownloadSetItem().getDsiDsUid());

        //TODO: re-enable after basic p2p cases run
//        entryStatusLiveData = appDb.getEntryStatusResponseDao()
//                .getLiveEntryStatus(downloadItem.getDjiContentEntryFileUid());

        downloadSetConnectivityObserver = this::handleDownloadSetMeteredConnectionAllowedChanged;
        statusObserver = this::handleConnectivityStatusChanged;
        downloadJobItemObserver = this::handleDownloadJobItemStatusChanged;
        //entryStatusObserver = this::handleContentEntryFileStatus;
        statusLiveData.observeForever(statusObserver);
        downloadJobItemLiveData.observeForever(downloadJobItemObserver);
        downloadSetConnectivityData.observeForever(downloadSetConnectivityObserver);
        //entryStatusLiveData.observeForever(entryStatusObserver);

        destinationDir = appDb.getDownloadSetDao().getDestinationDir(downloadItem
                .getDownloadSetItem().getDsiDsUid());

//        currentEntryStatusResponse = appDb.getEntryStatusResponseDao()
//                .findByContentEntryFileUid(downloadItem.getDjiContentEntryFileUid());

        startDownload();
    }




    /**
     * Start downloading a file
     */
    private void startDownload(){
        UstadMobileSystemImpl.l(UMLog.INFO, 699, mkLogPrefix() +
                " StartDownload: ContainerUid = " + downloadItem.getDjiContainerUid());
        int attemptsRemaining = 3;

        boolean downloaded = false;
        StatusCheckTask statusCheckTask = new StatusCheckTask();
        statusCheckTimer.scheduleAtFixedRate(statusCheckTask,
                0, TimeUnit.SECONDS.toMillis(1));

        Container container = appDbRepo.getContainerDao()
                .findByUid(downloadItem.getDjiContainerUid());

        ContainerManager containerManager = new ContainerManager(container, appDb, appDbRepo,
                destinationDir);

        do {
            long currentTimeStamp = System.currentTimeMillis();
            long minLastSeen = currentTimeStamp - TimeUnit.MINUTES.toMillis(1);
            long maxFailureFromTimeStamp = currentTimeStamp - TimeUnit.MINUTES.toMillis(5);

            //TODO: if the content is available on the node we already connected to, take that one
            currentNetworkNode = appDb.getNetworkNodeDao()
                    .findLocalActiveNodeByContainerUid(downloadItem.getDjiContainerUid(),
                            minLastSeen,BAD_PEER_FAILURE_THRESHOLD,maxFailureFromTimeStamp);

            boolean isFromCloud = (currentNetworkNode == null);
            DownloadJobItemHistory history = new DownloadJobItemHistory();
            history.setMode(isFromCloud ? MODE_CLOUD : MODE_LOCAL);
            history.setStartTime(System.currentTimeMillis());
            history.setDownloadJobItemId(downloadItem.getDjiUid());
            history.setNetworkNode(isFromCloud ? 0L: currentNetworkNode.getNodeId());
            history.setId((int) appDb.getDownloadJobItemHistoryDao().insert(history));

            String downloadEndpoint;
            URLConnectionOpener connectionOpener = null;
            if(isFromCloud){
                if(connectivityStatus.getWifiSsid() != null
                        && connectivityStatus.getWifiSsid().toUpperCase().startsWith("DIRECT-")){
                    //we are connected to a local peer, but need the normal wifi
                    //TODO: if the wifi is just not available and is required, don't mark as a failure of this job
                    // set status to waiting for connection and stop
                    if(!connectToCloudNetwork()) {
                        //connection has failed
                        attemptsRemaining--;
                        recordHistoryFinished(history, false);
                        continue;
                    }
                }
                downloadEndpoint = endpointUrl;
            }else{
                if(currentNetworkNode.getGroupSsid() == null
                        || !currentNetworkNode.getGroupSsid().equals(connectivityStatus.getWifiSsid())) {

                    if(!connectToLocalNodeNetwork()) {
                        //recording failure will push the node towards the bad threshold, after which
                        // the download will be attempted from the cloud
                        recordHistoryFinished(history, false);
                        continue;
                    }
                }

                downloadEndpoint = currentNetworkNode.getEndpointUrl();
                connectionOpener = networkManager.getLocalConnectionOpener();
            }

            Call<List<ContainerEntryWithMd5>> containerEntryListCall = containerEntryListService
                    .findByContainerWithMd5(downloadEndpoint + CONTAINER_ENTRY_LIST_PATH,
                            downloadItem.getDjiContainerUid());

            history.setUrl(downloadEndpoint);

            UstadMobileSystemImpl.l(UMLog.INFO, 699, mkLogPrefix() +
                    " starting download from" + downloadEndpoint + " FromCloud=" + isFromCloud +
                    " Attempts remaining= " + attemptsRemaining);

            try {
                appDb.getDownloadJobItemDao().incrementNumAttempts(downloadItem.getDjiUid());

                Response<List<ContainerEntryWithMd5>> response = containerEntryListCall.execute();
                if(response.isSuccessful()){
                    List<ContainerEntryWithMd5> containerEntryList =response.body();
                    Collection<ContainerEntryWithMd5> entriesToDownload = containerManager
                            .linkExistingItems(containerEntryList);//returns items we don't have yet
                    completedEntriesBytesDownloaded.set(appDb.getContainerEntryFileDao()
                            .sumContainerFileEntrySizes(container.getContainerUid()));
                    history.setStartTime(System.currentTimeMillis());

                    int downloadedCount = 0;
                    UstadMobileSystemImpl.l(UMLog.INFO, 699, "Downloading " +
                            entriesToDownload.size() + " ContainerEntryFiles from " + downloadEndpoint);
                    for(ContainerEntryWithMd5 entry : entriesToDownload) {
                        File destFile = new File(new File(destinationDir),
                                entry.getCeCefUid() +".tmp");
                        httpDownload = new ResumableHttpDownload(downloadEndpoint +
                                CONTAINER_ENTRY_FILE_PATH + entry.getCeCefUid(),
                                destFile.getAbsolutePath());
                        httpDownload.setConnectionOpener(connectionOpener);
                        httpDownloadRef.set(httpDownload);
                        if(httpDownload.download()) {
                            completedEntriesBytesDownloaded.addAndGet(destFile.length());
                            containerManager.addEntry(destFile, entry.getCePath(),
                                    ContainerManager.OPTION_COPY);
                            downloadedCount++;
                        }

                        if(!destFile.delete())
                            destFile.deleteOnExit();
                    }

                    downloaded = downloadedCount == entriesToDownload.size();
                }
            }catch(IOException e) {
                UstadMobileSystemImpl.l(UMLog.ERROR,699, mkLogPrefix() +
                        "Failed to download a file from " + endpointUrl, e);
            }

            if(!downloaded) {
                //wait before retry
                try { Thread.sleep(3000); }
                catch(InterruptedException ignored) {}
            }
            attemptsRemaining--;
            recordHistoryFinished(history, downloaded);
        }while(runnerStatus.get() == JobStatus.RUNNING && !downloaded && attemptsRemaining > 0);

        //httpdownloadref usage is finished
        httpDownloadRef.set(null);

        if(downloaded){
            appDb.getDownloadJobDao().updateBytesDownloadedSoFar(downloadItem.getDjiDjUid(),
                    null);
            long currentDownloadSpeed = httpDownload != null ? httpDownload.getCurrentDownloadSpeed() : 1;
            long totalDownloaded = completedEntriesBytesDownloaded.get() +
                    (httpDownload != null ? httpDownload.getDownloadedSoFar() : 0);
            long downloadTotalSize = httpDownload != null ? httpDownload.getTotalSize() : 0L;

            appDb.getDownloadJobItemDao().updateDownloadJobItemStatus(downloadItem.getDjiUid(),
                    JobStatus.COMPLETE, totalDownloaded,
                    downloadTotalSize, currentDownloadSpeed);
        }

        stop(downloaded ? JobStatus.COMPLETE : JobStatus.FAILED);

    }

    private void recordHistoryFinished(DownloadJobItemHistory history, boolean successful){
        history.setEndTime(System.currentTimeMillis());
        history.setSuccessful(successful);
        appDb.getDownloadJobItemHistoryDao().update(history);
    }

    /**
     * Try to connect to the 'normal' wifi
     *
     * @return true if file should be do downloaded from the cloud otherwise false.
     */
    private boolean connectToCloudNetwork() {
        UstadMobileSystemImpl.l(UMLog.DEBUG, 699, "Reconnecting cloud network");
        networkManager.restoreWifi();
        WaitForLiveData.observeUntil(statusLiveData, CONNECTION_TIMEOUT, TimeUnit.SECONDS,
                (connectivityStatus) -> {
                    if(connectivityStatus == null)
                        return false;

                    if(connectivityStatus.getConnectivityState()
                            == ConnectivityStatus.STATE_UNMETERED){
                        networkManager.lockWifi(downloadWiFiLock);
                        return true;
                    }

                    return connectivityStatus.getConnectivityState()
                            == ConnectivityStatus.STATE_METERED
                            && meteredConnectionAllowed.get() == 1;

                });

        return connectivityStatus.getConnectivityState() == ConnectivityStatus.STATE_UNMETERED
                || (meteredConnectionAllowed.get() == 1
                && connectivityStatus.getConnectivityState() == ConnectivityStatus.STATE_METERED);
    }

    /**
     * Start local peers connection handshake
     *
     * @return true if successful, false otherwise
     */
    private boolean connectToLocalNodeNetwork(){
        waitingForLocalConnection.set(true);
        BleMessage requestGroupCreation = new BleMessage(WIFI_GROUP_REQUEST,
                BleMessageUtil.bleMessageLongToBytes(Collections.singletonList(1L)));
        UstadMobileSystemImpl.l(UMLog.DEBUG,699, mkLogPrefix() +
                " connecting local network: requesting group credentials ");
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean connectionRequestActive = new AtomicBoolean(true);
        networkManager.lockWifi(downloadWiFiLock);

        networkManager.sendMessage(context,requestGroupCreation, currentNetworkNode,
                ((sourceDeviceAddress, response, error) ->  {
                    UstadMobileSystemImpl.l(UMLog.INFO, 699, mkLogPrefix() +
                            " BLE response received: from " + sourceDeviceAddress + ":" + response +
                            " error: " + error);
                    if(latch.getCount() > 0 && connectionRequestActive.get()
                            && response != null
                            && response.getRequestType() == WIFI_GROUP_CREATION_RESPONSE){
                        connectionRequestActive.set(false);
                        WiFiDirectGroupBle lWifiDirectGroup = new Gson().fromJson(new String(response.getPayload()),
                                WiFiDirectGroupBle.class);
                        wiFiDirectGroupBle.set(lWifiDirectGroup);
                        currentNetworkNode.setEndpointUrl(lWifiDirectGroup.getEndpoint());
                        appDb.getNetworkNodeDao().updateNetworkNodeGroupSsid(currentNetworkNode.getNodeId(),
                                lWifiDirectGroup.getSsid(), lWifiDirectGroup.getEndpoint());
                        UstadMobileSystemImpl.l(UMLog.INFO,699, mkLogPrefix() +
                                "Connecting to P2P group network with SSID "+lWifiDirectGroup.getSsid());
                    }
                    latch.countDown();
                }));
        try { latch.await(20, TimeUnit.SECONDS); }
        catch(InterruptedException ignored) {}
        connectionRequestActive.set(false);


        //There was an exception trying to communicate with the peer to get the wifi direct group network
        if(wiFiDirectGroupBle.get() == null) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 699, mkLogPrefix() +
                    "Requested group network" +
                    "from bluetooth address " + currentNetworkNode.getBluetoothMacAddress() +
                    "but did not receive group network credentials");
            return false;
        }

        //disconnect first
        if(connectivityStatus.getConnectivityState() != ConnectivityStatus.STATE_DISCONNECTED
                && connectivityStatus.getWifiSsid() != null) {
            WaitForLiveData.observeUntil(statusLiveData, 10, TimeUnit.SECONDS,
                    (connectivityStatus) -> connectivityStatus != null
                            && connectivityStatus.getConnectivityState() != ConnectivityStatus.STATE_UNMETERED);
            UstadMobileSystemImpl.l(UMLog.INFO, 699, "Disconnected existing wifi network");
        }

        networkManager.connectToWiFi(wiFiDirectGroupBle.get().getSsid(),
                wiFiDirectGroupBle.get().getPassphrase());

        AtomicReference<ConnectivityStatus> statusRef = new AtomicReference<>();
        WaitForLiveData.observeUntil(statusLiveData, lWiFiConnectionTimeout, TimeUnit.SECONDS,
                (connectivityStatus) -> {
                    statusRef.set(connectivityStatus);
                    if(connectivityStatus == null)
                        return false;

                    //connected OK and ready to go
                    return isExpectedWifiDirectGroup(connectivityStatus);

                    //TODO: pin down what status messages to expect to know that the attempt has failed
//                    return connectivityStatus.getConnectivityState() == ConnectivityStatus.STATE_DISCONNECTED
//                            || connectivityStatus.getConnectivityState() ==  ConnectivityStatus.STATE_UNMETERED;
                });

        waitingForLocalConnection.set(false);
        return statusRef.get() != null && isExpectedWifiDirectGroup(statusRef.get());
    }


    /**
     * Update status of the currently downloading job item.
     * @param itemStatus new status to be set
     * @see JobStatus
     */
    private void updateItemStatus(int itemStatus) {
        appDb.getDownloadJobItemDao().updateStatus(downloadItem.getDjiUid(), itemStatus);
        appDb.getContentEntryStatusDao().updateDownloadStatus(
                downloadItem.getDownloadSetItem().getDsiContentEntryUid(), itemStatus);
    }

    private boolean isExpectedWifiDirectGroup(ConnectivityStatus status){
        WiFiDirectGroupBle lWifiDirectGroupBle = wiFiDirectGroupBle.get();
        return status.getConnectivityState() == ConnectivityStatus.STATE_CONNECTED_LOCAL
                && status.getWifiSsid() != null
                && lWifiDirectGroupBle != null
                && status.getWifiSsid().equals(lWifiDirectGroupBle.getSsid());
    }


    private String mkLogPrefix() {
        return "DownloadJobItem #" + downloadItem.getDjiUid() + ":";
    }
}
