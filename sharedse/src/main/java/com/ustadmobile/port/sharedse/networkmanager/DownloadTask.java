package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.AcquisitionTaskHistoryEntry;
import com.ustadmobile.core.networkmanager.AcquisitionTaskStatus;
import com.ustadmobile.core.networkmanager.EntryCheckResponse;
import com.ustadmobile.core.networkmanager.NetworkManagerCore;
import com.ustadmobile.core.networkmanager.NetworkManagerListener;
import com.ustadmobile.core.networkmanager.NetworkManagerTaskListener;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobWithRelations;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.entities.UmOpdsLink;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
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

    private UstadJSOPDSFeed feed;

    private DownloadJobWithRelations downloadJob;

    protected NetworkManagerTaskListener listener;

    /**
     * Flag to indicate file destination index in array.
     */
    private static final int FILE_DESTINATION_INDEX=1;

    /**
     * Flag to indicate file download URL index in array
     */
    private static final int FILE_DOWNLOAD_URL_INDEX=0;

    private static final int DOWNLOAD_TASK_UPDATE_TIME=500;

    private int currentEntryIdIndex;

    private String currentGroupIPAddress;

    private String currentGroupSSID;

    private static long TIME_PASSED_FOR_PROGRESS_UPDATE = Calendar.getInstance().getTimeInMillis();

    private Timer updateTimer=null;

    private Thread entryAcquisitionThread =null;

    private String message=null;

    private EntryCheckResponse entryCheckResponse;

    private static final int MAXIMUM_ATTEMPT_COUNT = 10;

    private static final int WAITING_TIME_BEFORE_RETRY = 2 * 1000;

    private int attemptCount = 0;

    private ResumableHttpDownload httpDownload=null;

    private boolean localNetworkDownloadEnabled = true;

    private boolean wifiDirectDownloadEnabled = true;

    /**
     * Map all entry download statuses to their entry ID.
     * Useful when retrieving current status of entry acquisition task.
     */
    protected Map<String, Status> statusMap = new Hashtable<>();

    private Status currentEntryStatus =null;

    /**
     * Map all AcquisitionTaskHistoryEntry to their respective entry IDs
     */
    private Map<String, List<AcquisitionTaskHistoryEntry>> acquisitionHistoryMap = new HashMap<>();

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

    private AcquisitionTaskHistoryEntry currentHistoryEntry;

    public static final int SAME_NET_SCORE = 600;
    public static final int WIFI_DIRECT_SCORE = 500;
    public static final int FAILED_NODE_SCORE = -400;
    public static final float FAILURE_MEMORY_TIME = 20 * 60 * 1000;

    private Timer wifiConnectTimeoutTimer;

    private TimerTask wifiConnectTimeoutTimerTask;

    private LocalMirrorFinder mirrorFinder;


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
                currentEntryStatus.setDownloadedSoFar(httpDownload.getDownloadedSoFar());
                currentEntryStatus.setTotalSize(httpDownload.getTotalSize());
                currentEntryStatus.setStatus(UstadMobileSystemImpl.DLSTATUS_RUNNING);
                currentEntryStatus.setCurrentSpeed(httpDownload.getCurrentDownloadSpeed());

                networkManager.fireAcquisitionProgressUpdate(
                        getFeed().getEntry(currentEntryIdIndex).getItemId(), DownloadTask.this);
                networkManager.updateNotification(NOTIFICATION_TYPE_ACQUISITION,progress,
                        getFeed().getEntry(currentEntryIdIndex).getTitle(),message);

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
        this.downloadJob = downloadJob;

        this.networkManager = networkManager;

//        this.feed=feed;
        networkManager.addNetworkManagerListener(this);
        this.mirrorFinder = networkManager;

        //mark entries as about to be acquired
        UmOpdsLink entryAcquireLink;
        Status entryStatus;
        for(int i = 0; i < feed.size(); i++) {
            CatalogEntryInfo info = CatalogPresenter.getEntryInfo(feed.getEntry(i).getItemId(),
                    CatalogPresenter.SHARED_RESOURCE,networkManager.getContext());

            if(info == null) {
                info = new CatalogEntryInfo();
                info.acquisitionStatus = CatalogPresenter.STATUS_NOT_ACQUIRED;
            }

            if(info.acquisitionStatus == CatalogPresenter.STATUS_NOT_ACQUIRED) {
                entryAcquireLink = feed.getEntry(i).getFirstAcquisitionLink(null);
                info.acquisitionStatus = CatalogPresenter.STATUS_ACQUISITION_IN_PROGRESS;
                info.mimeType = entryAcquireLink.getMimeType();
                info.srcURLs = new String[]{UMFileUtil.resolveLink(
                    feed.getAbsoluteSelfLink().getHref(),
                    entryAcquireLink.getHref())};
            }

            CatalogPresenter.setEntryInfo(feed.getEntry(i).getItemId(), info, CatalogPresenter.SHARED_RESOURCE,
                networkManager.getContext());
            entryStatus = new Status();
            entryStatus.setStatus(UstadMobileSystemImpl.DLSTATUS_PENDING);
            statusMap.put(feed.getEntry(i).getItemId(), new Status());
        }
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

        CatalogEntryInfo info;
        for(int i = 0; i < feed.size(); i++) {
            info = CatalogPresenter.getEntryInfo(
                    feed.getEntry(i).getItemId(), CatalogPresenter.SHARED_RESOURCE,
                    networkManager.getContext());
            info.downloadID = getTaskId();
            CatalogPresenter.setEntryInfo(feed.getEntry(i).getItemId(), info,
                    CatalogPresenter.SHARED_RESOURCE, networkManager.getContext());
        }
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

        //mark entries not completed as not acquired instead of in progress
        for(int i = currentEntryIdIndex; i < feed.size(); i++) {
            CatalogEntryInfo entryInfo = CatalogPresenter.getEntryInfo(feed.getEntry(i).getItemId(),
                    CatalogPresenter.SHARED_RESOURCE, networkManager.getContext());
            if(entryInfo.acquisitionStatus == CatalogPresenter.STATUS_ACQUISITION_IN_PROGRESS) {
                UstadMobileSystemImpl.l(UMLog.INFO, 700, getLogPrefix() + ": marking entry not acquired: #"
                        + i + ": " + feed.getEntry(i).getItemId());
                entryInfo.acquisitionStatus = CatalogPresenter.STATUS_NOT_ACQUIRED;
                CatalogPresenter.setEntryInfo(feed.getEntry(i).getItemId(), entryInfo,
                        CatalogPresenter.SHARED_RESOURCE, networkManager.getContext());
            }
        }

        if(updateTimer!=null){
            updateTimer.cancel();
            updateTimerTask.cancel();
            updateTimerTask =null;
            updateTimer=null;
        }

        setWaitingForWifiConnection(false);

        String feedSelfUrl = feed.getAbsoluteSelfLink().getHref();
        if(feedSelfUrl.startsWith("p2p://")) {
            networkManager.removeWiFiDirectGroup();
        }


        if(wifiConnectTimeoutTimer != null) {
            wifiConnectTimeoutTimer.cancel();
            wifiConnectTimeoutTimer = null;
        }

        setStatus(status);
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
        if(isStopped())
            return;

        if(index < feed.size()) {
            currentEntryIdIndex = index;
            currentEntryStatus = statusMap.get(feed.getEntry(index).getItemId());
            currentHistoryEntry = new AcquisitionTaskHistoryEntry(feed.getEntry(index).getItemId());
            attemptCount++;
            currentGroupSSID = null;

            List<AcquisitionTaskHistoryEntry> entryHistoryList = acquisitionHistoryMap.get(
                    feed.getEntry(currentEntryIdIndex).getItemId());
            if(entryHistoryList == null) {
                entryHistoryList = new ArrayList<>();
                acquisitionHistoryMap.put(feed.getEntry(currentEntryIdIndex).getItemId(), entryHistoryList);
            }
            entryHistoryList.add(currentHistoryEntry);

            UstadMobileSystemImpl.l(UMLog.INFO, 303, getLogPrefix() + ": acquireFile: file:" + index + " id: "
                    + feed.getEntry(currentEntryIdIndex).getItemId());
            setWaitingForWifiConnection(false);

            if(httpDownload!=null){
                currentEntryStatus.setDownloadedSoFar(httpDownload.getDownloadedSoFar());
                currentEntryStatus.setTotalSize(httpDownload.getTotalSize());
            }

            currentEntryStatus.setStatus(UstadMobileSystemImpl.DLSTATUS_RUNNING);
            networkManager.fireAcquisitionStatusChanged(getFeed().getEntry(currentEntryIdIndex).getItemId(),
                    currentEntryStatus);

            //TODO: Move hardcoded strings to locale constants
            message=getFeed().size() > 1 ? "Downloading "+(currentEntryIdIndex+1)+" of "
                    +getFeed().size() +" files":"Downloading file";

            networkManager.addNotification(NOTIFICATION_TYPE_ACQUISITION,
                    getFeed().getEntry(currentEntryIdIndex).getTitle(),message);
            long currentDownloadId = new AtomicInteger().incrementAndGet();
            String entryId = feed.getEntry(currentEntryIdIndex).getItemId();
            entryCheckResponse = selectEntryCheckResponse(feed.getEntry(index),
                    mirrorFinder.getEntryResponsesWithLocalFile(feed.getEntry(index).getItemId()));

            //TODO: fix this to work using db
//            NetworkNode responseNode = entryCheckResponse != null ? entryCheckResponse.getNetworkNode() : null;
            NetworkNode responseNode = null;
            String currentSsid = networkManager.getCurrentWifiSsid();
            boolean wifiAvailable = currentSsid != null
                    || networkManager.getActionRequiredAfterGroupConnection() == NetworkManager.AFTER_GROUP_CONNECTION_RESTORE;
            String feedEntryAcquisitionUrl =  UMFileUtil.resolveLink(
                    feed.getAbsoluteSelfLink().getHref(),
                    feed.getEntry(currentEntryIdIndex).getFirstAcquisitionLink(null).getHref());

            if(feedEntryAcquisitionUrl.startsWith("p2p://")) {
                targetNetwork = TARGET_NETWORK_WIFIDIRECT;
                currentDownloadUrl = feedEntryAcquisitionUrl.replace("p2p://", "http://");
                String groupOwnerIp = networkManager.getWifiDirectGroupOwnerIp();
                if(groupOwnerIp != null) {
                    currentDownloadUrl = currentDownloadUrl.replace("groupowner", groupOwnerIp);
                }else {
                    //TODO: If this happens - try to reconnect to the group owner.
                    UstadMobileSystemImpl.l(UMLog.ERROR, 667, getLogPrefix() + " p2p download, group owner IP is null!");
                    handleAttemptFailed();
                }
            }else if(localNetworkDownloadEnabled && entryCheckResponse != null
                    && networkManager.getCurrentWifiSsid() != null
                    && responseNode.getTimeSinceNetworkServiceLastUpdated() < NetworkManager.ALLOWABLE_DISCOVERY_RANGE_LIMIT){
                targetNetwork = TARGET_NETWORK_NORMAL;
                currentDownloadUrl = "http://"+entryCheckResponse.getNetworkNode().getDeviceIpAddress()+":"
                        +entryCheckResponse.getNetworkNode().getPort()+"/catalog/entry/"+entryId;
                currentDownloadMode = DOWNLOAD_FROM_PEER_ON_SAME_NETWORK;
            }else if(wifiDirectDownloadEnabled && entryCheckResponse != null
                    && networkManager.isWiFiEnabled()
                    && responseNode.getTimeSinceWifiDirectLastUpdated() < NetworkManager.ALLOWABLE_DISCOVERY_RANGE_LIMIT){
                targetNetwork = TARGET_NETWORK_WIFIDIRECT_GROUP;
                currentDownloadMode = DOWNLOAD_FROM_PEER_ON_DIFFERENT_NETWORK;
            }else if(wifiAvailable|| isDownloadOnMobileDataEnabled()){
                //download from cloud
                targetNetwork = wifiAvailable ? TARGET_NETWORK_NORMAL : TARGET_NETWORK_MOBILE_DATA;
                currentDownloadUrl = feedEntryAcquisitionUrl;
                currentDownloadMode = DOWNLOAD_FROM_CLOUD;
            }else {
                //we're stuck -
                UstadMobileSystemImpl.l(UMLog.INFO, 0, getLogPrefix() + " download over data disabled, no wifi available - cleanup and wait");
                cleanup(STATUS_WAITING_FOR_NETWORK);
                return;
            }

            currentHistoryEntry.setMode(currentDownloadMode);
            currentHistoryEntry.setUrl(currentDownloadUrl);
            if(entryCheckResponse != null)
                entryCheckResponse.getNetworkNode().addAcquisitionHistoryEntry(currentHistoryEntry);

            UstadMobileSystemImpl.l(UMLog.INFO, 336, getLogPrefix() + ": acquire item " + index +
                    "  id " + feed.getEntry(index).getItemId() + " Mode = " + currentDownloadMode
                    + " target network = " + targetNetwork);

            if(targetNetwork.equals(TARGET_NETWORK_WIFIDIRECT)) {
                UstadMobileSystemImpl.l(UMLog.INFO, 316, getLogPrefix() + " : use WiFi direct");
                downloadCurrentFile(currentDownloadUrl, NetworkManager.DOWNLOAD_FROM_PEER_WIFIDIRECT);
            }else if(targetNetwork.equals(TARGET_NETWORK_WIFIDIRECT_GROUP)) {
                UstadMobileSystemImpl.l(UMLog.INFO, 316,getLogPrefix() +  ": Connect bluetooth");
                networkManager.handleFileAcquisitionInformationAvailable(entryId, currentDownloadId,
                        currentDownloadMode);
                networkManager.connectBluetooth(entryCheckResponse.getNetworkNode().getDeviceBluetoothMacAddress()
                        ,this);
            }else if(targetNetwork.equals(TARGET_NETWORK_NORMAL)) {
                //String currentSsid = networkManager.getCurrentWifiSsid();
                boolean isConnectedToWifiDirectGroup = networkManager.isConnectedToWifiDirectLegacyGroup();

                if(currentSsid != null && !isConnectedToWifiDirectGroup){
                    UstadMobileSystemImpl.l(UMLog.INFO, 316, getLogPrefix() + ": use current normal network");
                    networkManager.handleFileAcquisitionInformationAvailable(entryId, currentDownloadId,
                            currentDownloadMode);
                    downloadCurrentFile(currentDownloadUrl, currentDownloadMode);
                }else {
                    UstadMobileSystemImpl.l(UMLog.INFO, 316, getLogPrefix() + ": restore wifi");
                    setWaitingForWifiConnection(true);
                    networkManager.restoreWifi();
                }
            }else if(targetNetwork.equals(TARGET_NETWORK_MOBILE_DATA)){
                UstadMobileSystemImpl.l(UMLog.VERBOSE, 0, getLogPrefix() + " download via mobile data");
                downloadCurrentFile(currentDownloadUrl, currentDownloadMode);
            }else {
                UstadMobileSystemImpl.l(UMLog.CRITICAL, 0, getLogPrefix() +  " invalid download outcome");
            }
        }else{
            cleanup(STATUS_COMPLETE);
        }
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
        entryAcquisitionThread =new Thread(new Runnable() {
            @Override
            public void run() {
                if(isStopped()) {
                    UstadMobileSystemImpl.l(UMLog.INFO, 317, getLogPrefix()
                        + " entry acquisition thread exiting - task is stopped");
                    return;
                }


                UstadMobileSystemImpl.l(UMLog.INFO, 317, getLogPrefix() + ":downloadCurrentFile: from "
                        + fileUrl + " mode: " + mode);
                String entryMimeType = feed.getEntry(currentEntryIdIndex).getFirstAcquisitionLink(
                        null).getMimeType();
                String filename = UMFileUtil.appendExtensionToFilenameIfNeeded(UMFileUtil.getFilename(fileUrl),
                        entryMimeType);

                File fileDestination = new File(getFileURIs()[FILE_DESTINATION_INDEX], filename);

                boolean downloadCompleted = false;
                currentHistoryEntry.setTimeStarted(Calendar.getInstance().getTimeInMillis());

                try {
                    networkManager.updateNotification(NOTIFICATION_TYPE_ACQUISITION,0,
                            getFeed().getEntry(currentEntryIdIndex).getTitle() ,message);
                    statusMap.put(getFeed().getEntry(currentEntryIdIndex).getItemId(), currentEntryStatus);
                    httpDownload=new ResumableHttpDownload(fileUrl,fileDestination.getAbsolutePath());
                    currentEntryStatus.setTotalSize(httpDownload.getTotalSize());
                    downloadCompleted = httpDownload.download();
                } catch (IOException e) {
                    UstadMobileSystemImpl.l(UMLog.ERROR, 661, getLogPrefix() + " : item " + currentEntryIdIndex +
                            " : IOException", e);
                    currentEntryStatus.setStatus(UstadMobileSystemImpl.DLSTATUS_FAILED);
                    networkManager.fireAcquisitionStatusChanged(getFeed().getEntry(currentEntryIdIndex).getItemId(),
                        currentEntryStatus);
                }

                currentHistoryEntry.setTimeEnded(Calendar.getInstance().getTimeInMillis());


                if(downloadCompleted){
                    CatalogEntryInfo info = CatalogPresenter.getEntryInfo(
                        feed.getEntry(currentEntryIdIndex).getItemId(), CatalogPresenter.SHARED_RESOURCE,
                        networkManager.getContext());
                    info.acquisitionStatus = CatalogPresenter.STATUS_ACQUIRED;
                    info.fileURI = fileDestination.getAbsolutePath();
                    info.mimeType = feed.getEntry(currentEntryIdIndex).getFirstAcquisitionLink(
                            null).getMimeType();
                    CatalogPresenter.setEntryInfo(feed.getEntry(currentEntryIdIndex).getItemId(), info,
                            CatalogPresenter.SHARED_RESOURCE, networkManager.getContext());
                    UstadMobileSystemImpl.l(UMLog.INFO, 331, getLogPrefix() + ": item " + currentEntryIdIndex +
                            " id " + feed.getEntry(currentEntryIdIndex).getItemId() + " : Download successful ");
                    currentEntryStatus.setStatus(UstadMobileSystemImpl.DLSTATUS_SUCCESSFUL);
                    currentHistoryEntry.setStatus(UstadMobileSystemImpl.DLSTATUS_SUCCESSFUL);

                    networkManager.fireAcquisitionStatusChanged(feed.getEntry(currentEntryIdIndex).getItemId(),
                            currentEntryStatus);
                    attemptCount = 0;
                    entryAcquisitionThread =null;
                    acquireFile(currentEntryIdIndex + 1);
                }else {
                    UstadMobileSystemImpl.l(UMLog.ERROR, 660, getLogPrefix() + " : item " + currentEntryIdIndex +
                        " : Download did not complete");
                    handleAttemptFailed();
                }

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
        currentHistoryEntry.setStatus(UstadMobileSystemImpl.DLSTATUS_FAILED);
        currentHistoryEntry.setTimeEnded(Calendar.getInstance().getTimeInMillis());
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
     * Method used to get file destination (Destination directory) and File source URL.
     * @return String [] : Array of file URLS;
     */
    private String []  getFileURIs(){
        Vector downloadDestVector = getFeed().getLinks(NetworkManagerCore.LINK_REL_DOWNLOAD_DESTINATION,
                null);
        if(downloadDestVector.isEmpty()) {
            throw new IllegalArgumentException("No download destination in acquisition feed for acquireCatalogEntries");
        }
        File downloadDestDir = new File(((UmOpdsLink)downloadDestVector.get(0)).getHref());
        UmOpdsLink selfLink = getFeed().getAbsoluteSelfLink();

        if(selfLink == null)
            throw new IllegalArgumentException("No absolute self link on feed - required to resolve links");

        String feedHref = selfLink.getHref();
        UmOpdsLink acquisitionLink=getFeed().getEntry(currentEntryIdIndex).getFirstAcquisitionLink(null);
        return new String[]{UMFileUtil.resolveLink(feedHref,acquisitionLink.getHref()),
                downloadDestDir.getAbsolutePath()};
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
                currentDownloadUrl = "http://"+ currentGroupIPAddress +":"+
                        entryCheckResponse.getNetworkNode().getPort()+"/catalog/entry/"
                        +getFeed().getEntry(currentEntryIdIndex).getItemId();
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

    public UstadJSOPDSFeed getFeed() {
        return feed;
    }

    public void setFeed(UstadJSOPDSFeed feed) {
        this.feed = feed;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof DownloadTask && getFeed().equals(this.feed);
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

    /**
     * If enabled the task will attempt to acquire the requested entries from another node on the same
     * wifi network directly (nodes discovered using Network Service Discovery - NSD).
     *
     * @return boolean: True if enabled, false otherwise
     */
    public boolean isLocalNetworkDownloadEnabled() {
        return localNetworkDownloadEnabled;
    }

    public void setLocalNetworkDownloadEnabled(boolean localNetworkDownloadEnabled) {
        this.localNetworkDownloadEnabled = localNetworkDownloadEnabled;
    }

    /**
     * If enabled the task will attempt to acquire the requested entries from another node using
     * wifi direct. The node will be contacted using bluetooth and then a wifi group connection
     * will be created.
     *
     * @return boolean: True if enabled, false otherwise
     */
    public boolean isWifiDirectDownloadEnabled() {
        return wifiDirectDownloadEnabled;
    }

    /**
     * Method which decide whether Wi-Fi direct should be used as one
     * of file acquisition mode or not. By default it is allowed but otherwise
     * it will be disabled hence only other ways can be used to acquire a file.
     * @param wifiDirectDownloadEnabled: Logical boolean to enable and disable downloading a file using Wi-Fi direct
     */
    public void setWifiDirectDownloadEnabled(boolean wifiDirectDownloadEnabled) {
        this.wifiDirectDownloadEnabled = wifiDirectDownloadEnabled;
    }

    public LocalMirrorFinder getMirrorFinder() {
        return mirrorFinder;
    }

    public void setMirrorFinder(LocalMirrorFinder mirrorFinder) {
        this.mirrorFinder = mirrorFinder;
    }

    /**
     * Gets the AcquisitionTaskHistory of a particular entry. The history is a list of
     * AcquisitionTaskHistoryEntry from the first activity to the last activity (e.g. most recent).
     *
     * @param entryId OPDS Entry ID to check on
     * @return List of AcquisitionTaskHisotryEntry if this entry is part of the task and activity
     * has taken place, null otherwise.
     */
    public List<AcquisitionTaskHistoryEntry> getAcquisitionHistoryByEntryId(String entryId) {
        return acquisitionHistoryMap.get(entryId);
    }

    /**
     * Check if the given entry id is part of this acquisition task or not
     *
     * @param entryId
     *
     * @return true if the given entry id is included in this acquisition task, false otherwise
     */
    public boolean taskIncludesEntry(String entryId) {
        return feed.getEntryById(entryId) != null;
    }

    /**
     * Selects the optimal entry check response to download if any
     *
     * @param responses
     * @return
     */
    public EntryCheckResponse selectEntryCheckResponse(UstadJSOPDSEntry entry, List<EntryCheckResponse> responses) {
        if(responses == null || responses.size() == 0) {
            return null;
        }

        if(responses.size() == 1) {
            return scoreEntryCheckResponse(responses.get(0)) > 0 ? responses.get(0) : null;
        }

        final HashMap<EntryCheckResponse, Integer> entryCheckScores = new HashMap<>();

        ArrayList<EntryCheckResponse> listToSort = new ArrayList<>(responses);
        Collections.sort(listToSort, new Comparator<EntryCheckResponse>() {
            @Override
            public int compare(EntryCheckResponse response1, EntryCheckResponse response2) {
                if(!entryCheckScores.containsKey(response1))
                    entryCheckScores.put(response1, scoreEntryCheckResponse(response1));

                if(!entryCheckScores.containsKey(response2))
                    entryCheckScores.put(response2, scoreEntryCheckResponse(response2));

                return entryCheckScores.get(response2) - entryCheckScores.get(response1);
            }
        });

        EntryCheckResponse bestResponse = listToSort.get(0);
        return bestResponse != null && entryCheckScores.get(bestResponse) >0 ? bestResponse : null;
    }

    private int scoreEntryCheckResponse(EntryCheckResponse response) {
        if(!response.isFileAvailable())
            return 0;

        //TODO: fix this to work using db
        return -1;
//        int score = 0;
//        NetworkNode node = response.getNetworkNode();
//        if(response.getNetworkNode().isNsdActive()) {
//            score += SAME_NET_SCORE;
//        }else if(response.getNetworkNode().isWifiDirectActive()) {
//            score += WIFI_DIRECT_SCORE;
//        }
//
//
//        if(node.getAcquisitionHistory() != null) {
//            Iterator<AcquisitionTaskHistoryEntry> historyIterator = node.getAcquisitionHistory().iterator();
//            AcquisitionTaskHistoryEntry entry;
//            while(historyIterator.hasNext()) {
//                entry = historyIterator.next();
//                if(entry.getStatus() == UstadMobileSystemImpl.DLSTATUS_FAILED) {
//                    long timeSinceFail = Calendar.getInstance().getTimeInMillis() - entry.getTimeEnded();
//                    score += (1 - Math.min((float)timeSinceFail / FAILURE_MEMORY_TIME, 1)) * FAILED_NODE_SCORE;
//                }
//            }
//        }
//
//        return score;
    }

    public boolean isDownloadOnMobileDataEnabled() {
        return false;
    }

    protected String getLogPrefix() {
        return "AcquisitionTask #" + getTaskId() + " Item # " + currentEntryIdIndex + " Attempt # "
                + attemptCount;
    }


}
