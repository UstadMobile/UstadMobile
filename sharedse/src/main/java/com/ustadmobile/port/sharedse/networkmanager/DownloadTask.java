package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.dao.DownloadJobItemHistoryDao;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.AcquisitionTaskHistoryEntry;
import com.ustadmobile.core.networkmanager.AcquisitionTaskStatus;
import com.ustadmobile.core.networkmanager.NetworkManagerListener;
import com.ustadmobile.core.networkmanager.NetworkManagerTaskListener;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadJobItemHistory;
import com.ustadmobile.lib.db.entities.DownloadJobWithRelations;
import com.ustadmobile.lib.db.entities.EntryStatusResponseWithNode;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ustadmobile.port.sharedse.networkmanager.BluetoothServer.CMD_SEPARATOR;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManager.DOWNLOAD_FROM_CLOUD;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManager.DOWNLOAD_FROM_PEER_ON_DIFFERENT_NETWORK;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManager.DOWNLOAD_FROM_PEER_ON_SAME_NETWORK;
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
public class DownloadTask extends NetworkTask implements BluetoothConnectionHandler,NetworkManagerListener{


    private DownloadJobWithRelations downloadJob;

    private DownloadJobItem currentDownloadJobItem;

    protected NetworkManagerTaskListener listener;

    private static final int DOWNLOAD_TASK_UPDATE_TIME=500;

    private int currentEntryIdIndex;

    private String currentGroupIPAddress;

    private String currentGroupSSID;

    private static long TIME_PASSED_FOR_PROGRESS_UPDATE = Calendar.getInstance().getTimeInMillis();

    private Timer updateTimer=null;

    private Thread entryAcquisitionThread =null;

    private String message=null;


    private EntryStatusResponseWithNode entryStatusResponse;

    private DownloadJobItemHistory currentJobItemHistory;

    private static final int MAXIMUM_ATTEMPT_COUNT = 10;

    private static final int WAITING_TIME_BEFORE_RETRY = 2 * 1000;

    private int attemptCount = 0;

    private ResumableHttpDownload httpDownload=null;

//    private boolean localNetworkDownloadEnabled = true;
//
//    private boolean wifiDirectDownloadEnabled = true;

    /**
     * Map all entry download statuses to their entry ID.
     * Useful when retrieving current status of entry acquisition task.
     */
    protected Map<String, Status> statusMap = new Hashtable<>();

    private String currentEntryTitle;

    /**
     * The task wants to connect to the "normal" wifi e.g. for download from the cloud or for
     * download from another peer on the same network
     */
    public static final String TARGET_NETWORK_NORMAL = "com.ustadmobile.network.normal";

    /**
     * The task wants to connect to another node not on the same network
     */
    public static final String TARGET_NETWORK_WIFIDIRECT_GROUP = "com.ustadmobile.network.connect";

    public static final String TARGET_NETWORK_MOBILE_DATA = "com.ustadmobile.network.mobiledata";

    /**
     * The task wants to use a "normal" wifi direct connection between two devices
     */
    public static final String TARGET_NETWORK_WIFIDIRECT = "com.ustadmobile.network.wifidirect";

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

    protected DbManager mDbManager;

    @Deprecated
    private AcquisitionTaskHistoryEntry currentHistoryEntry;

//    private DownloadJobItemHistory currentItemHistory;

    public static final int SAME_NET_SCORE = 600;
    public static final int WIFI_DIRECT_SCORE = 500;
    public static final int FAILED_NODE_SCORE = -400;
    public static final int FAILURE_MEMORY_TIME = 20 * 60 * 1000;

    private Timer wifiConnectTimeoutTimer;

    private TimerTask wifiConnectTimeoutTimerTask;

    private LocalMirrorFinder mirrorFinder;

    private String currentExpectedMimeType;

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
            if(httpDownload != null && httpDownload.getTotalSize()>0L){
                int progress=(int)((httpDownload.getDownloadedSoFar()*100)/ httpDownload.getTotalSize());
                long currentTime = Calendar.getInstance().getTimeInMillis();
                int progressLimit=100;
                if(((currentTime - TIME_PASSED_FOR_PROGRESS_UPDATE) < DOWNLOAD_TASK_UPDATE_TIME) ||
                        (progress < 0 && progress > progressLimit)) {
                    return;
                }
                TIME_PASSED_FOR_PROGRESS_UPDATE = currentTime;

                currentDownloadJobItem.setDownloadedSoFar(httpDownload.getDownloadedSoFar());
                currentDownloadJobItem.setCurrentSpeed(httpDownload.getCurrentDownloadSpeed());
                currentDownloadJobItem.setStatus(STATUS_RUNNING);
                currentDownloadJobItem.setDownloadLength(httpDownload.getTotalSize());
                mDbManager.getDownloadJobItemDao().updateDownloadJobItemStatus(currentDownloadJobItem);
            }
        }
    }

    private TimerTask updateTimerTask;

    private class WifiConnectTimeoutTimerTask extends TimerTask {
        @Override
        public void run() {
            UstadMobileSystemImpl.l(UMLog.WARN, 213, getLogPrefix() + ": wifi connect timeout.");
            DownloadTask.this.handleAttemptFailed();
        }
    }


    @Deprecated
    public static class Status implements AcquisitionTaskStatus{

        long downloadedSoFar;

        long totalSize;

        int status;

        long currentSpeed;

        public Status() {

        }

        /**
         * Get all the bytes downloaded so far from the source file.
         * @return long: Total bytes downloaded so far
         */
        public synchronized long getDownloadedSoFar() {
            return downloadedSoFar;
        }

        /**
         * Set all the bytes downloaded so far from the source file
         * @param downloadedSoFar long: Total bytes downloaded so far
         *
         */
        protected synchronized void setDownloadedSoFar(long downloadedSoFar) {
            this.downloadedSoFar = downloadedSoFar;
        }

        /**
         * Get file size (Total bytes in the file)
         * @return long: File size
         */
        public long getTotalSize() {
            return totalSize;
        }

        /**
         * Set total file size.
         * @param totalSize long: total bytes in the file.
         */
        protected synchronized void setTotalSize(long totalSize) {
            this.totalSize = totalSize;
        }


        /**
         * Get file acquisition status
         * @return
         */
        public int getStatus() {
            return status;
        }

        /**
         * Set file acquisition status
         * @param status int:
         */
        protected synchronized void setStatus(int status) {
            this.status = status;
        }

        public long getCurrentSpeed() {
            return currentSpeed;
        }

        protected void setCurrentSpeed(long currentSpeed) {
            this.currentSpeed = currentSpeed;
        }
    }


    /**
     * Create file acquisition task
     * @param downloadJob downloadJob
     * @param networkManager NetworkManager reference which handle all network operations.
     */
    public DownloadTask(DownloadJobWithRelations downloadJob, NetworkManager networkManager){
        super(networkManager);
        mDbManager = DbManager.getInstance(networkManager.getContext());
        this.downloadJob = downloadJob;

        this.networkManager = networkManager;

        networkManager.addNetworkManagerListener(this);
        this.mirrorFinder = networkManager;
    }

    /**
     * Method which start file acquisition task
     */
    public synchronized void start() {
        currentEntryIdIndex =0;
        synchronized (this){
            if(updateTimer==null){
                updateTimer=new Timer();
                updateTimerTask = new UpdateTimerTask();
                updateTimer.scheduleAtFixedRate(updateTimerTask,DOWNLOAD_TASK_UPDATE_TIME,
                        DOWNLOAD_TASK_UPDATE_TIME);
            }
        }

        mDbManager.getDownloadJobDao().updateJobStatus(downloadJob.getId(), STATUS_RUNNING);
        setStatus(STATUS_RUNNING);
        networkManager.networkTaskStatusChanged(this);


        acquireFile(0);
    }

    /**
     * Cleanup: called when all downloads have been attempted and succeeded or permanently failed
     */
    protected synchronized void cleanup(int status) {
        //All entries complete
        networkManager.removeNotification(NOTIFICATION_TYPE_ACQUISITION);
        networkManager.removeNetworkManagerListener(this);

        if(updateTimer!=null){
            updateTimer.cancel();
            updateTimerTask.cancel();
            updateTimerTask =null;
            updateTimer=null;
        }

        setWaitingForWifiConnection(false);

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

        networkManager.networkTaskStatusChanged(this);
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
     */
    private void acquireFile(int index){
        new Thread(() -> {
            if (isStopped())
                return;

            if (index < downloadJob.getDownloadJobItems().size()) {
                currentEntryIdIndex = index;
                currentDownloadJobItem = downloadJob.getDownloadJobItems().get(currentEntryIdIndex);
                attemptCount++;
                currentGroupSSID = null;


                UstadMobileSystemImpl.l(UMLog.INFO, 303, getLogPrefix() + ": acquireFile: file:" + index + " id: "
                        + downloadJob.getDownloadJobItems().get(currentEntryIdIndex).getEntryId());
                setWaitingForWifiConnection(false);

                //TODO: Move hardcoded strings to locale constants
                message = downloadJob.getDownloadJobItems().size() > 1 ? "Downloading " + (currentEntryIdIndex + 1) + " of "
                        + downloadJob.getDownloadJobItems().size() + " files" : "Downloading file";

                currentEntryTitle = DbManager.getInstance(networkManager.getContext()).getOpdsEntryDao()
                        .findTitleByUuid(currentDownloadJobItem.getOpdsEntryUuid());
                networkManager.addNotification(NOTIFICATION_TYPE_ACQUISITION,
                        currentEntryTitle, message);
                long currentDownloadId = new AtomicInteger().incrementAndGet();
                String entryId = currentDownloadJobItem.getEntryId();


                DbManager dbManager = DbManager.getInstance(networkManager.getContext());
                List<EntryStatusResponseWithNode> statusResponses = dbManager.getEntryStatusResponseDao()
                        .findByEntryIdAndAvailability(currentDownloadJobItem.getEntryId(), true);

                entryStatusResponse = selectEntryStatusResponse(statusResponses,
                        dbManager.getDownloadJobItemHistoryDao());

                NetworkNode responseNode = entryStatusResponse != null ?
                        entryStatusResponse.getNetworkNode() : null;
                String currentSsid = networkManager.getCurrentWifiSsid();
                boolean wifiAvailable = currentSsid != null
                        || networkManager.getActionRequiredAfterGroupConnection() == NetworkManager.AFTER_GROUP_CONNECTION_RESTORE;

                OpdsEntryWithRelations entryWithRelations = dbManager.getOpdsEntryWithRelationsDao()
                        .getEntryByUuidStatic(currentDownloadJobItem.getOpdsEntryUuid());
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
                        handleAttemptFailed();
                    }
                } else if (downloadJob.isLanDownloadEnabled() && entryStatusResponse != null
                        && networkManager.getCurrentWifiSsid() != null
                        && responseNode.getTimeSinceNetworkServiceLastUpdated() < NetworkManager.ALLOWABLE_DISCOVERY_RANGE_LIMIT) {
                    targetNetwork = TARGET_NETWORK_NORMAL;
                    currentDownloadUrl = "http://" + entryStatusResponse.getNetworkNode().getIpAddress() + ":"
                            + entryStatusResponse.getNetworkNode().getPort() + "/catalog/entry/" + entryId;
                    currentDownloadMode = DOWNLOAD_FROM_PEER_ON_SAME_NETWORK;
                } else if (downloadJob.isWifiDirectDownloadEnabled() && entryStatusResponse != null
                        && networkManager.isWiFiEnabled()
                        && responseNode.getTimeSinceWifiDirectLastUpdated() < NetworkManager.ALLOWABLE_DISCOVERY_RANGE_LIMIT) {
                    targetNetwork = TARGET_NETWORK_WIFIDIRECT_GROUP;
                    currentDownloadMode = DOWNLOAD_FROM_PEER_ON_DIFFERENT_NETWORK;
                } else if (wifiAvailable || isDownloadOnMobileDataEnabled()) {
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
                        currentDownloadJobItem, currentDownloadMode, System.currentTimeMillis());

                UstadMobileSystemImpl.l(UMLog.INFO, 336, getLogPrefix() + ": acquire item " + index +
                        "  id " + currentDownloadJobItem.getEntryId() + " Mode = " + currentDownloadMode
                        + " target network = " + targetNetwork);

                if (targetNetwork.equals(TARGET_NETWORK_WIFIDIRECT)) {
                    UstadMobileSystemImpl.l(UMLog.INFO, 316, getLogPrefix() + " : use WiFi direct");
                    downloadCurrentFile(currentDownloadUrl, NetworkManager.DOWNLOAD_FROM_PEER_WIFIDIRECT);
                } else if (targetNetwork.equals(TARGET_NETWORK_WIFIDIRECT_GROUP)) {
                    UstadMobileSystemImpl.l(UMLog.INFO, 316, getLogPrefix() + ": Connect bluetooth");
                    networkManager.handleFileAcquisitionInformationAvailable(entryId, currentDownloadId,
                            currentDownloadMode);
                    networkManager.connectBluetooth(entryStatusResponse.getNetworkNode().getBluetoothMacAddress()
                            , this);
                } else if (targetNetwork.equals(TARGET_NETWORK_NORMAL)) {
                    //String currentSsid = networkManager.getCurrentWifiSsid();
                    boolean isConnectedToWifiDirectGroup = networkManager.isConnectedToWifiDirectLegacyGroup();

                    if (currentSsid != null && !isConnectedToWifiDirectGroup) {
                        UstadMobileSystemImpl.l(UMLog.INFO, 316, getLogPrefix() + ": use current normal network");
                        networkManager.handleFileAcquisitionInformationAvailable(entryId, currentDownloadId,
                                currentDownloadMode);
                        downloadCurrentFile(currentDownloadUrl, currentDownloadMode);
                    } else {
                        UstadMobileSystemImpl.l(UMLog.INFO, 316, getLogPrefix() + ": restore wifi");
                        setWaitingForWifiConnection(true);
                        networkManager.restoreWifi();
                    }
                } else if (targetNetwork.equals(TARGET_NETWORK_MOBILE_DATA)) {
                    UstadMobileSystemImpl.l(UMLog.VERBOSE, 0, getLogPrefix() + " download via mobile data");
                    downloadCurrentFile(currentDownloadUrl, currentDownloadMode);
                } else {
                    UstadMobileSystemImpl.l(UMLog.CRITICAL, 0, getLogPrefix() + " invalid download outcome");
                }
            } else {
                cleanup(STATUS_COMPLETE);
            }
        }).start();
    }

    /**
     * Method which start file acquisition from the specified source.
     * @param fileUrl : File source URL
     * @param mode: Mode in which the file will be acquired as DOWNLOAD_FROM_CLOUD,
     *            DOWNLOAD_FROM_PEER_ON_SAME_NETWORK and DOWNLOAD_FROM_PEER_ON_DIFFERENT_NETWORK
     *
     * @exception InterruptedException
     */
    private void downloadCurrentFile(final String fileUrl, final int mode) {
        entryAcquisitionThread =new Thread(() -> {
            if(isStopped()) {
                UstadMobileSystemImpl.l(UMLog.INFO, 317, getLogPrefix()
                    + " entry acquisition thread exiting - task is stopped");
                return;
            }


            UstadMobileSystemImpl.l(UMLog.INFO, 317, getLogPrefix() + ":downloadCurrentFile: from "
                    + fileUrl + " mode: " + mode);
            String filename = UMFileUtil.appendExtensionToFilenameIfNeeded(UMFileUtil.getFilename(fileUrl),
                    currentExpectedMimeType);

            File fileDestination = new File(downloadJob.getDestinationDir(), filename);

            boolean downloadCompleted = false;

            try {
                networkManager.updateNotification(NOTIFICATION_TYPE_ACQUISITION,0,
                        currentEntryTitle ,message);
                httpDownload=new ResumableHttpDownload(fileUrl,fileDestination.getAbsolutePath());

                if(currentDownloadMode == DOWNLOAD_FROM_PEER_ON_DIFFERENT_NETWORK)
                    httpDownload.setConnectionOpener(networkManager.getWifiUrlConnectionOpener());

                if(httpDownload.getTotalSize() > 0){
                    currentDownloadJobItem.setDownloadLength(httpDownload.getTotalSize());
                }
                currentDownloadJobItem.setStatus(STATUS_RUNNING);
                currentDownloadJobItem.setCurrentSpeed(0);
                currentDownloadJobItem.setDownloadedSoFar(httpDownload.getDownloadedSoFar());
                mDbManager.getDownloadJobItemDao().updateDownloadJobItemStatus(currentDownloadJobItem);

                downloadCompleted = httpDownload.download();
            } catch (IOException e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 661, getLogPrefix() + " : item " + currentEntryIdIndex +
                        " : IOException", e);
            }

            currentJobItemHistory.setEndTime(System.currentTimeMillis());
            currentJobItemHistory.setSuccessful(downloadCompleted);
            mDbManager.getDownloadJobItemHistoryDao().insert(currentJobItemHistory);

            currentDownloadJobItem.setStatus(downloadCompleted ? STATUS_COMPLETE : STATUS_RUNNING);
            currentDownloadJobItem.setDownloadedSoFar(httpDownload.getDownloadedSoFar());
            mDbManager.getDownloadJobItemDao().updateDownloadJobItemStatus(currentDownloadJobItem);




            if(downloadCompleted){
                mDbManager.getOpdsEntryWithRelationsRepository().
                        findEntriesByContainerFileNormalizedPath(fileDestination.getAbsolutePath());
                attemptCount = 0;
                entryAcquisitionThread =null;
                acquireFile(currentEntryIdIndex + 1);
            }else {
                UstadMobileSystemImpl.l(UMLog.ERROR, 660, getLogPrefix() + " : item " + currentEntryIdIndex +
                    " : Download did not complete");
                handleAttemptFailed();
            }


        });
        UstadMobileSystemImpl.l(UMLog.DEBUG, 649, "Start entry acquisition thread");
        entryAcquisitionThread.start();
    }

    protected synchronized void handleAttemptFailed() {
        if(isStopped()) {
            UstadMobileSystemImpl.l(UMLog.DEBUG, 649, getLogPrefix() + " handleAttemptFailed - taking no action - task is stopped.");
            return;
        }

        UstadMobileSystemImpl.l(UMLog.DEBUG, 650, getLogPrefix() + " handleAttemptFailed.");

        setWaitingForWifiConnection(false);
        if(attemptCount < MAXIMUM_ATTEMPT_COUNT){
            UstadMobileSystemImpl.l(UMLog.DEBUG, 650, getLogPrefix()
                    + " handleAttemptFailed - waiting " + WAITING_TIME_BEFORE_RETRY + "ms then retrying.");
            try { Thread.sleep(WAITING_TIME_BEFORE_RETRY); }
            catch(InterruptedException e) {}
            acquireFile(currentEntryIdIndex);
        }else {
            //retry count exceeded - move on to next file
            UstadMobileSystemImpl.l(UMLog.DEBUG, 650, getLogPrefix()
                    + " handleAttemptFailed - attempt retry count exceeded - moving to next item");
            acquireFile(currentEntryIdIndex + 1);
        }

    }

    /**
     * @exception  IOException
     * @param inputStream InputStream to read data from.
     * @param outputStream OutputStream to write data to.
     */
    @Override
    public void onBluetoothConnected(final InputStream inputStream, final OutputStream outputStream) {
        UstadMobileSystemImpl.l(UMLog.INFO, 317, getLogPrefix() + ": bluetooth connected");
        String acquireCommand = BluetoothServer.CMD_ACQUIRE_ENTRY +" "+networkManager.getDeviceIPAddress()+"\n";
        String response=null;
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
                        entryStatusResponse.getNetworkNode().getPort() + "/catalog/entry/" +
                        currentDownloadJobItem.getEntryId();

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
            return;
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
            handleAttemptFailed();
        }
    }

    @Override
    public void onBluetoothConnectionFailed(Exception exception) {
        UstadMobileSystemImpl.l(UMLog.ERROR, 77, getLogPrefix() + " bluetooth connection failed");
        handleAttemptFailed();
    }

    @Override
    public void stop(int statusAfterStopped) {
        UstadMobileSystemImpl.l(UMLog.INFO, 321, getLogPrefix() + " task stop called.");
        setStopped(true);
//        super.stop(statusAfterStopped);
        if(httpDownload != null)
            httpDownload.stop();

        cleanup(statusAfterStopped);
    }

    /**
     * Get specific entry status.
     * @param entryId: Entry Id which identifies an Entry
     * @return Status: Status object in which extra information
     *                 for the particular entry acquisition
     *                 can be found
     */
    public Status getStatusByEntryId(String entryId) {
        return statusMap.get(entryId);
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
        UstadMobileSystemImpl.l(UMLog.INFO, 320, getLogPrefix()+ ": wifiConnectionChanged : " +
                " ssid: " + ssid + " connected: " + connected + " connectedOrConnecting: " +
                connectedOrConnecting);
        if(!isWaitingForWifiConnection())
            return;

        if(connected && targetNetwork != null && ssid != null && targetNetwork.equals(TARGET_NETWORK_NORMAL)) {
            UstadMobileSystemImpl.l(UMLog.INFO, 321, getLogPrefix() + ": 'normal' network restored - continue download");
            setWaitingForWifiConnection(false);
            downloadCurrentFile(currentDownloadUrl, currentDownloadMode);
        }

        if(connected && currentGroupSSID != null){
            if(currentGroupSSID.equals(ssid)) {
                UstadMobileSystemImpl.l(UMLog.INFO, 322, getLogPrefix()
                    + ": requested WiFi direct group connection activated");
                setWaitingForWifiConnection(false);
                downloadCurrentFile(currentDownloadUrl, currentDownloadMode);
            }else {
                UstadMobileSystemImpl.l(UMLog.INFO, 322, getLogPrefix()
                    + ": requested WiFi direct group connection failed : connected to other network");
                handleAttemptFailed();
            }


        }
    }

    protected synchronized boolean isWaitingForWifiConnection() {
        return waitingForWifiConnection;
    }

    /**
     * Sometimes this task needs to change the wifi connection in order to continue. It *MUST*
     * set this flag so that the event listener observing wifi connections will know to continue
     *
     * @param waitingForWifiConnection
     */
    protected synchronized void setWaitingForWifiConnection(boolean waitingForWifiConnection) {
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

    public LocalMirrorFinder getMirrorFinder() {
        return mirrorFinder;
    }

    public void setMirrorFinder(LocalMirrorFinder mirrorFinder) {
        this.mirrorFinder = mirrorFinder;
    }

    /**
     * Check if the given entry id is part of this acquisition task or not
     *
     * @param entryId
     *
     * @return true if the given entry id is included in this acquisition task, false otherwise
     */
    public boolean taskIncludesEntry(String entryId) {
        return downloadJob.getJobItemByEntryId(entryId) != null;
    }

    /**
     * Selects the optimal entry check response to download if any
     *
     * @param responses
     * @return
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

    public boolean isDownloadOnMobileDataEnabled() {
        return false;
    }

    protected String getLogPrefix() {
        return "AcquisitionTask #" + getTaskId() + " Item # " + currentEntryIdIndex + " Attempt # "
                + attemptCount;
    }


}
