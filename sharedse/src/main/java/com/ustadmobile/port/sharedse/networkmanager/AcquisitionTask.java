package com.ustadmobile.port.sharedse.networkmanager;
import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.impl.AcquisitionManager;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.AcquisitionTaskStatus;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
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
 * @see com.ustadmobile.port.sharedse.networkmanager.NetworkManagerListener
 * @see com.ustadmobile.port.sharedse.networkmanager.NetworkTask
 *
 * @author kileha3
 */
public class AcquisitionTask extends NetworkTask implements BluetoothConnectionHandler,NetworkManagerListener{

    private UstadJSOPDSFeed feed;

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

    private static final int MAXIMUM_RETRY_COUNT = 5;

    private static final int WAITING_TIME_BEFORE_RETRY =10 * 1000;

    private int retryCount=0;

    private ResumableHttpDownload httpDownload=null;

    private boolean localNetworkDownloadEnabled = true;

    private boolean wifiDirectDownloadEnabled = true;

    /**
     * Map all entry download statuses to their entry ID.
     * Useful when retrieving current status of entry acquisition task.
     */
    protected Map<String, Status> statusMap = new Hashtable<>();

    private Status acquisitionStatus=null;

    private boolean isWifiDirectActive=false;

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



    /**
     * Monitor file acquisition task progress and report it to the rest of the app (UI).
     * <p>
     *     Status will be updated only if the progress
     *     status is less than maximum progress which is 100,
     *     and time passed is less than DOWNLOAD_TASK_UPDATE_TIME
     * </p>
     */
    private TimerTask updateTimerTask =new TimerTask() {
        @Override
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
                acquisitionStatus.setDownloadedSoFar(httpDownload.getDownloadedSoFar());
                acquisitionStatus.setTotalSize(httpDownload.getTotalSize());
                acquisitionStatus.setStatus(UstadMobileSystemImpl.DLSTATUS_RUNNING);
                statusMap.put(getFeed().entries[currentEntryIdIndex].id,acquisitionStatus);

                networkManager.fireAcquisitionProgressUpdate(
                    getFeed().entries[currentEntryIdIndex].id, AcquisitionTask.this);
                networkManager.updateNotification(NOTIFICATION_TYPE_ACQUISITION,progress,
                    getFeed().entries[currentEntryIdIndex].title,message);

            }
        }
    };


    public static class Status implements AcquisitionTaskStatus{

        long downloadedSoFar;

        long totalSize;

        int status;
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
    }


    /**
     * Create file acquisition task
     * @param feed OPDS feed
     * @param networkManager NetworkManager reference which handle all network operations.
     */
    public AcquisitionTask(UstadJSOPDSFeed feed,NetworkManager networkManager){
        super(networkManager);
        this.feed=feed;
        acquisitionStatus=new Status();
        networkManager.addNetworkManagerListener(this);

        //mark entries as about to be acquired
        String[] entryAcquireLink;
        for(int i = 0; i < feed.entries.length; i++) {
            CatalogEntryInfo info = CatalogController.getEntryInfo(feed.entries[i].id,
                    CatalogController.SHARED_RESOURCE,networkManager.getContext());

            if(info == null) {
                info = new CatalogEntryInfo();
                info.acquisitionStatus = CatalogController.STATUS_NOT_ACQUIRED;
            }

            if(info.acquisitionStatus == CatalogController.STATUS_NOT_ACQUIRED) {
                entryAcquireLink = feed.entries[i].getFirstAcquisitionLink(null);
                info.acquisitionStatus = CatalogController.STATUS_ACQUISITION_IN_PROGRESS;
                info.mimeType = entryAcquireLink[UstadJSOPDSEntry.LINK_MIMETYPE];
                info.srcURLs = new String[]{UMFileUtil.resolveLink(
                    feed.getAbsoluteSelfLink()[UstadJSOPDSEntry.LINK_HREF],
                    entryAcquireLink[UstadJSOPDSEntry.LINK_HREF])};
            }

            CatalogController.setEntryInfo(feed.entries[i].id, info, CatalogController.SHARED_RESOURCE,
                networkManager.getContext());
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
                updateTimer.scheduleAtFixedRate(updateTimerTask,DOWNLOAD_TASK_UPDATE_TIME,
                        DOWNLOAD_TASK_UPDATE_TIME);
            }
        }

        acquireFile(0);
    }

    /**
     * Cleanup: called when all downloads have been attempted and succeeded or permanently failed
     */
    protected synchronized void onDone() {
        //All entries complete
        networkManager.removeNotification(NOTIFICATION_TYPE_ACQUISITION);
        networkManager.removeNetworkManagerListener(this);

        if(updateTimer!=null){
            updateTimer.cancel();
            updateTimerTask.cancel();
            updateTimerTask =null;
            updateTimer=null;
        }

        networkManager.handleTaskCompleted(this);
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
        if(index < feed.entries.length) {
            currentEntryIdIndex = index;
            UstadMobileSystemImpl.l(UMLog.INFO, 303, "AcquisitionTask: acquireFile: file:" + index + " id: "
                    + feed.entries[currentEntryIdIndex].id);
            setWaitingForWifiConnection(false);

            networkManager.getEntryAcquisitionTaskMap().put(getFeed().entries[currentEntryIdIndex].id,this);
            if(httpDownload!=null){
                acquisitionStatus.setDownloadedSoFar(httpDownload.getDownloadedSoFar());
                acquisitionStatus.setTotalSize(httpDownload.getTotalSize());
            }
            acquisitionStatus.setStatus(UstadMobileSystemImpl.DLSTATUS_RUNNING);
            statusMap.put(getFeed().entries[currentEntryIdIndex].id,acquisitionStatus);
            networkManager.fireAcquisitionStatusChanged(getFeed().entries[currentEntryIdIndex].id, this);

            //TODO: Move hardcoded strings to locale constants
            message=getFeed().entries.length>1 ? "Downloading "+(currentEntryIdIndex+1)+" of "
                    +getFeed().entries.length+" files":"Downloading file";

            networkManager.addNotification(NOTIFICATION_TYPE_ACQUISITION,
                    getFeed().entries[currentEntryIdIndex].title,message);
            long currentDownloadId = new AtomicInteger().incrementAndGet();
            String entryId = feed.entries[currentEntryIdIndex].id;
            entryCheckResponse=networkManager.getEntryResponseWithLocalFile(entryId);


            if(localNetworkDownloadEnabled && entryCheckResponse != null && Calendar.getInstance().getTimeInMillis() - entryCheckResponse.getNetworkNode().getNetworkServiceLastUpdated() < NetworkManager.ALLOWABLE_DISCOVERY_RANGE_LIMIT){
                targetNetwork = TARGET_NETWORK_NORMAL;
                currentDownloadUrl = "http://"+entryCheckResponse.getNetworkNode().getDeviceIpAddress()+":"
                        +entryCheckResponse.getNetworkNode().getPort()+"/catalog/entry/"+entryId;
                currentDownloadMode = DOWNLOAD_FROM_PEER_ON_SAME_NETWORK;
            }else if(wifiDirectDownloadEnabled && entryCheckResponse != null){//TODO: Check freshness of wifi direct response?
                targetNetwork = TARGET_NETWORK_WIFIDIRECT_GROUP;
                currentDownloadMode = DOWNLOAD_FROM_PEER_ON_DIFFERENT_NETWORK;
            }else {
                targetNetwork = TARGET_NETWORK_NORMAL;
                currentDownloadUrl = UMFileUtil.resolveLink(
                    feed.getAbsoluteSelfLink()[UstadJSOPDSEntry.LINK_HREF],
                    feed.entries[currentEntryIdIndex].getFirstAcquisitionLink(null)[UstadJSOPDSEntry.LINK_HREF]);
                currentDownloadMode = DOWNLOAD_FROM_CLOUD;
            }

            if(targetNetwork.equals(TARGET_NETWORK_WIFIDIRECT_GROUP)) {
                networkManager.handleFileAcquisitionInformationAvailable(entryId, currentDownloadId,
                        currentDownloadMode);
                UstadMobileSystemImpl.l(UMLog.INFO, 316, "AcquisitionTask: Connect bluetooth");
                networkManager.connectBluetooth(entryCheckResponse.getNetworkNode().getDeviceBluetoothMacAddress()
                        ,this);
            }else if(targetNetwork.equals(TARGET_NETWORK_NORMAL)) {
                String currentSsid = networkManager.getCurrentWifiSsid();
                boolean isConnectedToWifiDirectGroup = networkManager.isConnectedToWifiDirectGroup();

                if(currentSsid != null && !isConnectedToWifiDirectGroup){
                    UstadMobileSystemImpl.l(UMLog.INFO, 316, "AcquisitionTask: use current normal network");
                    networkManager.handleFileAcquisitionInformationAvailable(entryId, currentDownloadId,
                            currentDownloadMode);
                    downloadCurrentFile(currentDownloadUrl, currentDownloadMode);
                }else if(isConnectedToWifiDirectGroup){
                    UstadMobileSystemImpl.l(UMLog.INFO, 316, "AcquisitionTask: restore wifi");
                    setWaitingForWifiConnection(true);
                    networkManager.restoreWifi();
                }
            }
        }else{
            onDone();
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
                UstadMobileSystemImpl.l(UMLog.INFO, 317, "AcquisitionTask: downloadCurrentFile: from "
                        + fileUrl + " mode: " + mode);
                File fileDestination = new File(getFileURIs()[FILE_DESTINATION_INDEX],
                        UMFileUtil.getFilename(fileUrl));

                boolean downloadCompleted = false;
                AcquisitionTaskHistoryEntry historyEntry = new AcquisitionTaskHistoryEntry(fileUrl,
                        mode, Calendar.getInstance().getTimeInMillis());
                try {
                    networkManager.updateNotification(NOTIFICATION_TYPE_ACQUISITION,0,
                            getFeed().entries[currentEntryIdIndex].title,message);
                    statusMap.put(getFeed().entries[currentEntryIdIndex].id,acquisitionStatus);
                    httpDownload=new ResumableHttpDownload(fileUrl,fileDestination.getAbsolutePath());
                    acquisitionStatus.setTotalSize(httpDownload.getTotalSize());
                    downloadCompleted = httpDownload.download();
                } catch (IOException e) {
                    acquisitionStatus.setStatus(UstadMobileSystemImpl.DLSTATUS_FAILED);
                    networkManager.fireAcquisitionStatusChanged(getFeed().entries[currentEntryIdIndex].id,
                        AcquisitionTask.this);
                }

                historyEntry.setTimeEnded(Calendar.getInstance().getTimeInMillis());
                List<AcquisitionTaskHistoryEntry> entryHistoryList = acquisitionHistoryMap.get(feed.entries[currentEntryIdIndex].id);
                if(entryHistoryList == null) {
                    entryHistoryList = new ArrayList<>();
                    acquisitionHistoryMap.put(feed.entries[currentEntryIdIndex].id, entryHistoryList);
                }
                entryHistoryList.add(historyEntry);

                if(downloadCompleted){
                    //TODO : Needs to set the entry status with CatalogController
                    CatalogEntryInfo info = CatalogController.getEntryInfo(
                        feed.entries[currentEntryIdIndex].id, CatalogController.SHARED_RESOURCE,
                        networkManager.getContext());
                    info.acquisitionStatus = CatalogController.STATUS_ACQUIRED;
                    info.fileURI = fileDestination.getAbsolutePath();
                    info.mimeType = feed.entries[currentEntryIdIndex].getFirstAcquisitionLink(
                            null)[UstadJSOPDSEntry.LINK_MIMETYPE];
                    CatalogController.setEntryInfo(feed.entries[currentEntryIdIndex].id, info,
                            CatalogController.SHARED_RESOURCE, networkManager.getContext());
                    acquisitionStatus.setStatus(UstadMobileSystemImpl.DLSTATUS_SUCCESSFUL);
                    networkManager.fireAcquisitionStatusChanged(getFeed().entries[currentEntryIdIndex].id,
                            AcquisitionTask.this);
                    retryCount = 0;
                    entryAcquisitionThread =null;

                    historyEntry.setStatus(UstadMobileSystemImpl.DLSTATUS_SUCCESSFUL);
                    acquireFile(currentEntryIdIndex + 1);
                }else if(!downloadCompleted && retryCount < MAXIMUM_RETRY_COUNT){
                   try { Thread.sleep(WAITING_TIME_BEFORE_RETRY); }
                   catch(InterruptedException e) {}
                   retryCount++;
                   historyEntry.setStatus(UstadMobileSystemImpl.DLSTATUS_PENDING);
                   acquireFile(currentEntryIdIndex);
               }else{
                    //retry count exceeded
                    historyEntry.setStatus(UstadMobileSystemImpl.DLSTATUS_FAILED);
                    acquireFile(currentEntryIdIndex + 1);
               }

            }
        });
        entryAcquisitionThread.start();
    }


    /**
     * Method used to get file destination (Destination directory) and File source URL.
     * @return String [] : Array of file URLS;
     */
    private String []  getFileURIs(){
        Vector downloadDestVector = getFeed().getLinks(AcquisitionManager.LINK_REL_DOWNLOAD_DESTINATION,
                null);
        if(downloadDestVector.isEmpty()) {
            throw new IllegalArgumentException("No download destination in acquisition feed for acquireCatalogEntries");
        }
        File downloadDestDir = new File(((String[])downloadDestVector.get(0))[UstadJSOPDSEntry.LINK_HREF]);
        String[] selfLink = getFeed().getAbsoluteSelfLink();

        if(selfLink == null)
            throw new IllegalArgumentException("No absolute self link on feed - required to resolve links");

        String feedHref = selfLink[UstadJSOPDSEntry.LINK_HREF];
        String [] acquisitionLink=getFeed().entries[currentEntryIdIndex].getFirstAcquisitionLink(null);
        return new String[]{UMFileUtil.resolveLink(feedHref,acquisitionLink[UstadJSOPDSEntry.LINK_HREF]),
                downloadDestDir.getAbsolutePath()};
    }


    /**
     * @exception  IOException
     * @param inputStream InputStream to read data from.
     * @param outputStream OutputStream to write data to.
     */
    @Override
    public void onConnected(final InputStream inputStream, final OutputStream outputStream) {
        UstadMobileSystemImpl.l(UMLog.INFO, 317, "AcquisitionTask : bluetooth connected");
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
                        +getFeed().entries[currentEntryIdIndex].id;
                UstadMobileSystemImpl.l(UMLog.INFO, 318, "AcquisitionTask : bluetooth says connected to '" +
                    currentGroupSSID + "' download Url = " + currentDownloadUrl);
            }
        }catch(IOException e) {
            e.printStackTrace();
        }finally {
            UMIOUtils.closeInputStream(inputStream);
            UMIOUtils.closeOutputStream(outputStream);
        }

        if(currentGroupSSID != null && passphrase != null) {
            String currentSsid = networkManager.getCurrentWifiSsid();
            if(currentSsid != null && currentGroupSSID.equals(currentSsid)) {
                UstadMobileSystemImpl.l(UMLog.INFO, 318,
                    "AcquisitionTask: already connected to WiFi direct group network: '" +
                    currentSsid + "' - continuing");
                downloadCurrentFile(currentDownloadUrl, currentDownloadMode);
            }else {
                UstadMobileSystemImpl.l(UMLog.INFO, 319,
                    "AcquisitionTask: connecting to WiFi direct group network : '" + currentGroupSSID+
                    "' - requesting connection");
                setWaitingForWifiConnection(true);
                networkManager.connectToWifiDirectGroup(currentGroupSSID, passphrase);
            }
        }
    }


    @Override
    public void cancel() {
        entryAcquisitionThread.interrupt();
        if(entryAcquisitionThread.isInterrupted()){
            acquisitionStatus.setStatus(UstadMobileSystemImpl.DLSTATUS_FAILED);
            networkManager.fireAcquisitionStatusChanged(getFeed().entries[currentEntryIdIndex].id,this);
            networkManager.removeNotification(NOTIFICATION_TYPE_ACQUISITION);
            if(updateTimer!=null){
                updateTimer.cancel();
                updateTimerTask.cancel();
                updateTimerTask =null;
                updateTimer=null;
            }
        }
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
    public int getTaskId() {
        return this.taskId;
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
        return object instanceof AcquisitionTask && getFeed().equals(this.feed);
    }

    @Override
    public void fileStatusCheckInformationAvailable(List<String> fileIds) {

    }

    @Override
    public void entryStatusCheckCompleted(NetworkTask task) {

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
        UstadMobileSystemImpl.l(UMLog.INFO, 320, "AcquisitionTask: wifiConnectionChanged : " +
                " ssid: " + ssid + " connected: " + connected + " connectedOrConnecting: " +
                connectedOrConnecting);
        if(!isWaitingForWifiConnection())
            return;

        if(connected && targetNetwork != null && ssid != null && targetNetwork.equals(TARGET_NETWORK_NORMAL)) {
            UstadMobileSystemImpl.l(UMLog.INFO, 321, "AcquisitionTask: 'normal' network restored - continue download");
            setWaitingForWifiConnection(false);
            downloadCurrentFile(currentDownloadUrl, currentDownloadMode);
        }

        if(connected && currentGroupSSID != null && currentGroupSSID.equals(ssid)){
            UstadMobileSystemImpl.l(UMLog.INFO, 322, "AcquisitionTask: requested WiFi direct group connection activated");
            setWaitingForWifiConnection(false);
            downloadCurrentFile(currentDownloadUrl, currentDownloadMode);
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
}
