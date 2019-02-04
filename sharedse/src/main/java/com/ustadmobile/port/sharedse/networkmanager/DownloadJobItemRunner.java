package com.ustadmobile.port.sharedse.networkmanager;

import com.google.gson.Gson;
import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.lib.db.entities.ConnectivityStatus;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;
import com.ustadmobile.lib.db.entities.DownloadJobItemWithDownloadSetItem;
import com.ustadmobile.lib.db.entities.EntryStatusResponse;
import com.ustadmobile.lib.db.entities.NetworkNode;

import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.ustadmobile.lib.db.entities.ConnectivityStatus.STATE_CONNECTED_LOCAL;
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
public class DownloadJobItemRunner implements Runnable, BleMessageResponseListener,
        WiFiDirectConnectionListener{

    private NetworkManagerBle networkManager;

    private UmAppDatabase appDb;

    private DownloadJobItemWithDownloadSetItem downloadItem;

    private String endpointUrl;

    private UmLiveData<ConnectivityStatus> statusLiveData;

    private UmObserver<ConnectivityStatus> statusObserver;

    private UmObserver<EntryStatusResponse> entryStatusObserver;

    private UmLiveData<EntryStatusResponse> entryStatusLiveData;

    private UmObserver<Integer> downloadJobItemObserver;

    private UmLiveData<Integer> downloadJobItemLiveData;

    private UmLiveData<Boolean> downloadSetConnectivityData;

    private UmObserver<Boolean> downloadSetConnectivityObserver;

    private CountDownLatch localConnectLatch = new CountDownLatch(1);

    private volatile ResumableHttpDownload httpDownload;

    private AtomicReference<ResumableHttpDownload> httpDownloadRef;

    private Timer statusCheckTimer = new Timer();

    private AtomicInteger runnerStatus = new AtomicInteger(JobStatus.NOT_QUEUED);

    private ConnectivityStatus connectivityStatus;

    private AtomicInteger meteredConnectionAllowed = new AtomicInteger(-1);

    private AtomicInteger availableLocally = new AtomicInteger(-1);

    private Object context;

    private NetworkManagerBle.WiFiP2PGroupResponse p2PGroupResponse;

    private NetworkNode currentNetworkNode;

    private String serverIpAddress = null;

    private EntryStatusResponse currentContentEntryFileStatus;

    private final int MAX_CONNECTION_FAILURE_RETRY_LIMIT = 3;

    private int connectionFailureRetryCount = 1;

    /**
     * Timer task to keep track of the download status
     */
    private class StatusCheckTask extends TimerTask{

        @Override
        public void run() {
            //check for null httpdownload
            ResumableHttpDownload httpDownload  = httpDownloadRef.get();
            if(httpDownload != null && runnerStatus.get() == JobStatus.RUNNING) {
                appDb.getDownloadJobItemDao().updateDownloadJobItemStatus(downloadItem.getDjiUid(),
                        JobStatus.RUNNING,httpDownload.getDownloadedSoFar(),
                        httpDownload.getTotalSize(),httpDownload.getCurrentDownloadSpeed());
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
                                 String endpointUrl) {
        this.networkManager = networkManager;
        this.downloadItem = downloadItem;
        this.appDb = appDb;
        this.endpointUrl = endpointUrl;
        this.context = context;
        this.httpDownloadRef = new AtomicReference<>();
    }

    /**
     * Handle changes triggered when connectivity status changes.
     * @param newStatus changed connectivity status
     */
    private void handleConnectivityStatusChanged(ConnectivityStatus newStatus) {
        this.connectivityStatus = newStatus;
        if(connectivityStatus != null ){
            switch(newStatus.getConnectivityState()) {
                case ConnectivityStatus.STATE_METERED:
                    if(meteredConnectionAllowed.get() == 0) {
                        stopAsync(JobStatus.WAITING_FOR_CONNECTION);
                    }
                    break;

                case ConnectivityStatus.STATE_DISCONNECTED:
                    stopAsync(JobStatus.WAITING_FOR_CONNECTION);
                    break;

                case STATE_CONNECTED_LOCAL:
                    localConnectLatch.countDown();
                    startDownload(false);
                    break;

            }
        }
    }

    /**
     * Handle changes triggered when Download set metered connection flag changes
     * @param meteredConnection changed metered connection flag.
     */
    private void handleDownloadSetMeteredConnectionAllowedChanged(boolean meteredConnection){
        meteredConnectionAllowed.set(meteredConnection ? 1 : 0);

        if(meteredConnectionAllowed.get() == 0 && connectivityStatus != null
            && connectivityStatus.getConnectivityState() == ConnectivityStatus.STATE_METERED) {
            stopAsync(JobStatus.WAITING_FOR_CONNECTION);
        }

    }

    /**
     * Handle changes triggered when the download job item status changes
     * @param newDownloadStatus changed download job item status
     */

    private void handleDownloadJobItemStatusChanged(Integer newDownloadStatus){
        if(newDownloadStatus == JobStatus.STOPPING){
            stopAsync(JobStatus.STOPPED);
        }
    }

    /**
     * Handle changes triggered when file which wasn't available locally changes
     * @param entryStatusResponse new file entry status
     */
    private void handleContentEntryFileStatus(EntryStatusResponse entryStatusResponse){
        availableLocally.set(entryStatusResponse.isAvailable() ? 1:0);
        if(availableLocally.get() == 1 && !currentContentEntryFileStatus.isAvailable()){
            this.currentNetworkNode =
                    appDb.getNetworkNodeDao().findNodeById(entryStatusResponse.getErNodeId());
            stopAsync(JobStatus.WAITING_FOR_CONNECTION);

            startLocalConnectionHandShake();
        }
    }


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
            entryStatusLiveData.removeObserver(entryStatusObserver);

            statusCheckTimer.cancel();

            updateItemStatus(newStatus);
        }
    }


    @Override
    public void run() {
        runnerStatus.set(JobStatus.RUNNING);
        updateItemStatus(JobStatus.RUNNING);
        networkManager.startMonitoringAvailability(this,
                Arrays.asList(downloadItem.getDjiContentEntryFileUid()));

        statusLiveData = appDb.getConnectivityStatusDao().getStatusLive();
        downloadJobItemLiveData = appDb.getDownloadJobItemDao().getLiveStatus(downloadItem.getDjiUid());

        //get the download set
        downloadSetConnectivityData =
                appDb.getDownloadSetDao().getLiveMeteredNetworkAllowed(downloadItem
                        .getDownloadSetItem().getDsiDsUid());

        entryStatusLiveData = appDb.getEntryStatusResponseDao()
                .getLiveEntryStatus(downloadItem.getDjiContentEntryFileUid());

        downloadSetConnectivityObserver = this::handleDownloadSetMeteredConnectionAllowedChanged;
        statusObserver = this::handleConnectivityStatusChanged;
        downloadJobItemObserver = this::handleDownloadJobItemStatusChanged;
        entryStatusObserver = this::handleContentEntryFileStatus;
        statusLiveData.observeForever(statusObserver);
        downloadJobItemLiveData.observeForever(downloadJobItemObserver);
        downloadSetConnectivityData.observeForever(downloadSetConnectivityObserver);
        entryStatusLiveData.observeForever(entryStatusObserver);

        currentContentEntryFileStatus = appDb.getEntryStatusResponseDao()
                .findByContentEntryFileUid(downloadItem.getDjiContentEntryFileUid());

        long currentTimeStamp = System.currentTimeMillis();
        long minLastSeen = currentTimeStamp - TimeUnit.MINUTES.toMillis(2);
        long maxFailureFromTimeStamp = currentTimeStamp - TimeUnit.MINUTES.toMillis(10);

        currentNetworkNode = appDb.getNetworkNodeDao()
                .findNodeWithContentFileEntry(downloadItem.getDjiContentEntryFileUid(),
                        minLastSeen,3,maxFailureFromTimeStamp);

        if(currentContentEntryFileStatus == null || currentNetworkNode == null){
            startDownload(true);
        }else{
            startLocalConnectionHandShake();
        }
    }

    private String getFileUrl(boolean isFromCloud){
        if(isFromCloud){
           return endpointUrl + "ContentEntryFileServer/" + downloadItem
                    .getDjiContentEntryFileUid();
        }else{
            return serverIpAddress + ":" + p2PGroupResponse.getPort()+"/"
                    + downloadItem.getDjiContentEntryFileUid();
        }
    }

    /**
     * Start local peers connection handshake
     */
    private void startLocalConnectionHandShake(){
        BleMessage requestGroupCreation = new BleMessage(WIFI_GROUP_REQUEST,new byte[]{0});
        networkManager.sendMessage(context,requestGroupCreation,
                currentNetworkNode,this);
        try {
            localConnectLatch.await(10,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start downloading a file
     * @param fromCloud TRUE when downloading from cloud otherwise is from peer device
     */
    private void startDownload(boolean fromCloud){

        int attemptsRemaining = 3;

        boolean downloaded = false;
        StatusCheckTask statusCheckTask = new StatusCheckTask();
        statusCheckTimer.scheduleAtFixedRate(statusCheckTask,
                0, TimeUnit.SECONDS.toMillis(1));
        do {
            try {
                appDb.getDownloadJobItemDao().incrementNumAttempts(downloadItem.getDjiUid());
                httpDownload = new ResumableHttpDownload(getFileUrl(fromCloud),
                        downloadItem.getDestinationFile());
                httpDownloadRef.set(httpDownload);
                downloaded = httpDownload.download();
            }catch(IOException e) {
                e.printStackTrace();
                statusCheckTask.cancel();
            }

            attemptsRemaining--;
        }while(runnerStatus.get() == JobStatus.RUNNING && !downloaded && attemptsRemaining > 0);

        statusCheckTask.cancel();

        if(downloaded){
            ContentEntryFileStatus fileStatus = new ContentEntryFileStatus();
            fileStatus.setFilePath(downloadItem.getDestinationFile());
            fileStatus.setCefsContentEntryFileUid(downloadItem.getDjiContentEntryFileUid());
            appDb.getContentEntryFileStatusDao().insert(fileStatus);
        }

        if(p2PGroupResponse != null && !downloaded){
            startDownload(true);
        }else{
            stop(downloaded ? JobStatus.COMPLETE : JobStatus.FAILED);
        }
    }

    /**
     * Update status of the currently downloading job item.
     * @param itemStatus new status to be set
     * @see JobStatus
     */
    private void updateItemStatus(int itemStatus) {
        appDb.getDownloadJobItemDao().updateStatus(downloadItem.getDjiUid(), itemStatus);
    }

    @Override
    public void onConnected(String ipAddress, String groupSSID) {
        if(p2PGroupResponse.getGroupSsid().equals(groupSSID)){
            this.serverIpAddress = ipAddress;
            appDb.getConnectivityStatusDao().update(STATE_CONNECTED_LOCAL);
        }
    }

    @Override
    public void onFailure(String groupSSID) {
        if(p2PGroupResponse.getGroupSsid().equals(groupSSID)){
            if(connectionFailureRetryCount < MAX_CONNECTION_FAILURE_RETRY_LIMIT){
                connectionFailureRetryCount++;
                startLocalConnectionHandShake();
            }else{
                stopAsync(JobStatus.WAITING_FOR_CONNECTION);
                localConnectLatch.countDown();
            }
        }
    }


    @Override
    public void onResponseReceived(String sourceDeviceAddress, BleMessage response) {
        if(response.getRequestType() == WIFI_GROUP_CREATION_RESPONSE){
            this.p2PGroupResponse = new Gson().fromJson(new String(response.getPayload()),
                            NetworkManagerBle.WiFiP2PGroupResponse.class);

            networkManager.connectToWiFi(p2PGroupResponse.getGroupSsid(),
                    p2PGroupResponse.getGroupPassphrase(),this);

            appDb.getConnectivityStatusDao().update(ConnectivityStatus.STATE_CONNECTING_LOCAL);
        }
    }
}
