package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.lib.db.entities.ConnectivityStatus;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobItemWithDownloadSetItem;
import com.ustadmobile.lib.db.entities.NetworkNode;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
public class DownloadJobItemRunner implements Runnable, BleMessageResponseListener{

    private NetworkManagerBle networkManager;

    private UmAppDatabase appDb;

    private DownloadJobItemWithDownloadSetItem downloadItem;

    private String endpointUrl;

    private UmLiveData<ConnectivityStatus> statusLiveData;

    private UmObserver<ConnectivityStatus> statusObserver;

    private UmObserver<DownloadJob> downloadJobObserver;

    private UmLiveData<DownloadJob> downloadJobLiveData;

    private UmLiveData<Boolean> downloadSetConnectivityData;

    private UmObserver<Boolean> downloadSetConnectivityObserver;

    private CountDownLatch localConnectLatch;

    private volatile ResumableHttpDownload httpDownload;

    private AtomicReference<ResumableHttpDownload> httpDownloadRef;

    private Timer statusCheckTimer = new Timer();

    private AtomicBoolean stopped = new AtomicBoolean(false);

    private AtomicBoolean completed = new AtomicBoolean(false);

    private DownloadJob downloadJob;

    private ConnectivityStatus connectivityStatus;

    private AtomicBoolean meteredConnectionAllowed = new AtomicBoolean(false);

    private Object context;


    /**
     * Timer task to keep track of the download status
     */
    private class StatusCheckTask extends TimerTask{

        @Override
        public void run() {
            //check for null httpdownload
            ResumableHttpDownload httpDownload  = httpDownloadRef.get();
            if(httpDownload != null && !completed.get() && !stopped.get()) {
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
        this.localConnectLatch = new CountDownLatch(1);
        this.context = context;
        this.httpDownloadRef = new AtomicReference<>();
    }

    /**
     * Handle changes triggered when connectivity status changes.
     * @param newStatus changed connectivity status
     */
    private void handleConnectivityStatusChanged(ConnectivityStatus newStatus) {
        this.connectivityStatus = newStatus;
        if(connectivityStatus != null){
            if(!meteredConnectionAllowed.get() &&
                    newStatus.getConnectivityState() == ConnectivityStatus.STATE_METERED){
                stop(JobStatus.WAITING_FOR_CONNECTION);
            }else if(newStatus.getConnectivityState() == ConnectivityStatus.STATE_CONNECTED_LOCAL){
                localConnectLatch.countDown();
            }
        }
    }

    /**
     * Handle changes triggered when Download set metered connection flag changes
     * @param meteredConnection changed metered connection flag.
     */
    private void handleDownloadSetMeteredConnectionAllowedChanged(boolean meteredConnection){
        meteredConnectionAllowed.set(meteredConnection);
        if(connectivityStatus.getConnectivityState() == ConnectivityStatus.STATE_METERED
            && !meteredConnectionAllowed.get()) {
            stop(JobStatus.WAITING_FOR_CONNECTION);
        }
    }

    /**
     * Handle changes triggered when the download job status changes
     * @param newDownloadJob changed download job
     */
    private void handleDownloadJobStatusChanged(DownloadJob newDownloadJob){
        this.downloadJob = newDownloadJob;
        if(newDownloadJob.getDjStatus() == JobStatus.STOPPING){
            stop(JobStatus.STOPPING);
        }
    }


    /**
     * Stop the download task from continuing
     * @param newStatus new status to be set
     */
    private void stop(int newStatus) {
        stopped.set(true);
        completed.set(true);
        if(httpDownload != null){
            httpDownload.stop();
        }
        updateItemStatus(newStatus);
    }


    @Override
    public void run() {
        updateItemStatus(JobStatus.RUNNING);
        statusLiveData = appDb.getConnectivityStatusDao().getStatusLive();
        downloadJobLiveData = appDb.getDownloadJobDao().getJobLive(downloadItem.getDjiDjUid());

        //get the download set
        downloadSetConnectivityData =
                appDb.getDownloadSetDao().liveMeteredNetworkAllowed(downloadItem
                        .getDownloadSetItem().getDsiDsUid());

        downloadSetConnectivityObserver = this::handleDownloadSetMeteredConnectionAllowedChanged;
        statusObserver = this::handleConnectivityStatusChanged;
        downloadJobObserver = this::handleDownloadJobStatusChanged;
        statusLiveData.observeForever(statusObserver);
        downloadJobLiveData.observeForever(downloadJobObserver);
        downloadSetConnectivityData.observeForever(downloadSetConnectivityObserver);


        ContentEntryFileStatus entryFileStatus = appDb.getContentEntryFileStatusDao().
                findByContentEntryFileUid(downloadItem.getDjiContentEntryFileUid());
        if(entryFileStatus == null){
            startDownload();
        }else{
            //request connection info from the peer node
            /*List<EntryStatusResponseWithNode> statusResponseWithNodes =
                    appDb.getEntryStatusResponseDao().findByEntryIdAndAvailability(entryFileStatus.)*/

            try {
                NetworkNode networkNode = new NetworkNode();
                BleMessage requestGroupCreation = new BleMessage(WIFI_GROUP_REQUEST,new byte[]{});
                networkManager.sendMessage(context,requestGroupCreation,
                        networkNode,this);

                localConnectLatch.wait(TimeUnit.SECONDS.toMillis(10));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //start download only if WiFiP2P is connected successfully
            if(connectivityStatus.getConnectivityState()
                    == ConnectivityStatus.STATE_CONNECTED_LOCAL){
                startDownload();
            }
        }
    }

    /**
     * Start downloading a file
     */
    private void startDownload(){
        String fileHref = endpointUrl + "ContentEntryFileServer/" + downloadItem
                .getDjiContentEntryFileUid();

        int attemptsRemaining = 3;

        boolean downloaded = false;
        StatusCheckTask statusCheckTask = new StatusCheckTask();
        statusCheckTimer.scheduleAtFixedRate(statusCheckTask,
                0, TimeUnit.SECONDS.toMillis(1));
        do {
            try {
                appDb.getDownloadJobItemDao().incrementNumAttempts(downloadItem.getDjiUid());
                httpDownload = new ResumableHttpDownload(fileHref, downloadItem.getDestinationFile());
                httpDownloadRef.set(httpDownload);
                downloaded = httpDownload.download();
            }catch(IOException e) {
                e.printStackTrace();
                statusCheckTask.cancel();
            }

            attemptsRemaining--;
        }while(!stopped.get() && !downloaded && attemptsRemaining > 0);

        if(!stopped.get()) {
            statusCheckTask.cancel();
            int downloadStatus = attemptsRemaining >= 0 && !downloaded ? JobStatus.FAILED :
                    (httpDownload.isStopped() ? JobStatus.STOPPED : JobStatus.COMPLETE);
            updateItemStatus(downloadStatus);
        }

        if(downloaded){
            ContentEntryFileStatus fileStatus = new ContentEntryFileStatus();
            fileStatus.setFilePath(downloadItem.getDestinationFile());
            fileStatus.setCefsContentEntryFileUid(downloadItem.getDjiContentEntryFileUid());
            appDb.getContentEntryFileStatusDao().insert(fileStatus);
        }

    }

    /**
     * Update status of the currently downloading job item.
     * @param itemStatus new status to be set
     * @see JobStatus
     */
    private void updateItemStatus(int itemStatus) {
        appDb.getDownloadJobItemDao().updateStatus(downloadItem.getDjiUid(), itemStatus);

        if(itemStatus == JobStatus.STOPPING && downloadJob != null){
            appDb.getDownloadJobItemDao().updateStatusByJobId(downloadJob.getDjUid()
                    ,JobStatus.STOPPED);
        }

        if(itemStatus > JobStatus.RUNNING_MAX){
            completed.set(true);
            statusCheckTimer.cancel();
            statusLiveData.removeObserver(statusObserver);
            downloadJobLiveData.removeObserver(downloadJobObserver);
            downloadSetConnectivityData.removeObserver(downloadSetConnectivityObserver);
        }
    }


    @Override
    public void onResponseReceived(String sourceDeviceAddress, BleMessage response) {
        if(response.getRequestType() == WIFI_GROUP_CREATION_RESPONSE){
            appDb.getConnectivityStatusDao().update(ConnectivityStatus.STATE_CONNECTING_LOCAL);
        }
    }
}
