package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.impl.AcquisitionManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ustadmobile.port.sharedse.networkmanager.BluetoothServer.CMD_SEPARATOR;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManager.DOWNLOAD_FROM_CLOUD;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManager.DOWNLOAD_FROM_PEER_ON_DIFFERENT_NETWORK;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManager.DOWNLOAD_FROM_PEER_ON_SAME_NETWORK;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManager.NOTIFICATION_TYPE_ACQUISITION;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManager.SERVICE_PORT;

/**
 * Created by kileha3 on 09/05/2017.
 */

public class AcquisitionTask extends NetworkTask implements BluetoothConnectionHandler,NetworkManagerListener{

    private static final int ALLOWABLE_DISCOVERY_RANGE_LIMIT =2 * 60 * 1000;
    private UstadJSOPDSFeed feed;
    protected NetworkManagerTaskListener listener;
    private static final int FILE_DESTINATION_INDEX=1;
    private static final int FILE_DOWNLOAD_URL_INDEX=0;
    private static final int DOWNLOAD_TASK_UPDATE_TIME=500;
    private int currentEntryAcquisitionTaskStatus =-1;
    private int currentEntryIdIndex;
    private static final String INFO_FILE_EXTENSION=".dlinfo";
    private static final String UNFINISHED_FILE_EXTENSION=".part";
    private static final String HEADER_RANGE="Range";
    private static final String HEADER_LAST_MODIFIED ="Last-Modified";
    private static final String DOWNLOAD_REQUEST_METHOD="GET";
    private String currentGroupIPAddress;
    private String currentGroupSSID;
    private static long TIME_PASSED_FOR_PROGRESS_UPDATE = Calendar.getInstance().getTimeInMillis();
    private long currentDownloadId=0L;
    private long totalBytesDownloadedSoFar =0L;
    private long totalBytesToDownload =0L;
    private Timer updateTimer=null;
    private Thread entryAcquisitionThread =null;
    private String message=null;

    /**
     * Monitor file acquisition task progress and report it.
     */
    private TimerTask updateTimerTask =new TimerTask() {
        @Override
        public void run() {
            if(totalBytesToDownload>0L){
                int progress=(int)((totalBytesDownloadedSoFar*100)/ totalBytesToDownload);
                long currentTime = Calendar.getInstance().getTimeInMillis();
                int progressLimit=100;
                if(((currentTime - TIME_PASSED_FOR_PROGRESS_UPDATE) < DOWNLOAD_TASK_UPDATE_TIME) ||
                        (progress < 0 && progress > progressLimit)) {
                    return;
                }
                System.out.print("Downloading "+progress);
                TIME_PASSED_FOR_PROGRESS_UPDATE = currentTime;
                currentEntryAcquisitionTaskStatus =UstadMobileSystemImpl.DLSTATUS_RUNNING;
                networkManager.updateNotification(NOTIFICATION_TYPE_ACQUISITION,progress,
                        getFeed().entries[currentEntryIdIndex].title,message);

            }
        }
    };


    public AcquisitionTask(UstadJSOPDSFeed feed,NetworkManager networkManager){
        super(networkManager);
        this.feed=feed;
        networkManager.addNetworkManagerListener(this);
    }

    /**
     * Start file acquisition task
     */
    public synchronized void start() {
        currentEntryIdIndex =0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                acquireNextFile();
            }
        }).start();
    }

    /**
     * Determine where to download the file from
     * (Cloud, Peer on the same network or Peer on different network)
     *
     * if is from Cloud: initiate download task
     *            Peer on the same network: initiate download task
     *            Peer on different network: initiate bluetooth connection in order to trigger
     *            WiFi-Direct group creation on the host device
     */
    private void acquireNextFile(){
      currentEntryAcquisitionTaskStatus =UstadMobileSystemImpl.DLSTATUS_PENDING;
        //TODO: Move hardcoded strings to locale constants
        message=getFeed().entries.length>1 ? "Downloading "+(currentEntryIdIndex+1)+"/"
                +getFeed().entries.length+" Files":"Downloading File";
        networkManager.addNotification(NOTIFICATION_TYPE_ACQUISITION,getFeed().entries[currentEntryIdIndex].title,message);
        if(currentEntryIdIndex < feed.entries.length) {
            currentDownloadId= new AtomicInteger().incrementAndGet();
            String entryId = feed.entries[currentEntryIdIndex].id;

            EntryCheckResponse entryCheckResponse=networkManager.getEntryResponseWithLocalFile(entryId);

            if(entryCheckResponse!=null){
                NetworkNode node=entryCheckResponse.getNetworkNode();
                if(node.getNetworkServiceLastUpdated() < ALLOWABLE_DISCOVERY_RANGE_LIMIT){
                    networkManager.handleFileAcquisitionInformationAvailable(entryId,currentDownloadId,
                            DOWNLOAD_FROM_PEER_ON_SAME_NETWORK);
                    String fileURI="http://"+node.getDeviceIpAddress()+":"+SERVICE_PORT+"/catalog/entry/"+entryId;
                    downloadCurrentFile(fileURI);
                }else{
                    if(node.getWifiDirectLastUpdated() < ALLOWABLE_DISCOVERY_RANGE_LIMIT){
                        networkManager.handleFileAcquisitionInformationAvailable(entryId,currentDownloadId,
                                DOWNLOAD_FROM_PEER_ON_DIFFERENT_NETWORK);
                        networkManager.connectBluetooth(node.getDeviceBluetoothMacAddress(),this);
                    }else{
                        networkManager.handleFileAcquisitionInformationAvailable(entryId,
                                currentDownloadId,DOWNLOAD_FROM_CLOUD);
                        downloadCurrentFile(getFileURIs()[FILE_DOWNLOAD_URL_INDEX]);

                    }
                }
            }else{
                networkManager.handleFileAcquisitionInformationAvailable(entryId,
                        currentDownloadId,DOWNLOAD_FROM_CLOUD);
                downloadCurrentFile(getFileURIs()[FILE_DOWNLOAD_URL_INDEX]);
            }


        }else{
            listener.handleTaskCompleted(this);
        }
    }


    private void downloadCurrentFile(final String fileUrl) {
        synchronized (this){
            if(updateTimer==null){
                updateTimer=new Timer();
                updateTimer.scheduleAtFixedRate(updateTimerTask,DOWNLOAD_TASK_UPDATE_TIME,
                        DOWNLOAD_TASK_UPDATE_TIME);
            }
        }
        entryAcquisitionThread =new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream in = null;
                HttpURLConnection con = null;
                OutputStream out = null;
                long existingFileLength;
                String fileInfoLastModified = null;
                JSONObject infoFromFile = null;
                int bytesDownloadedSoFar;
                boolean isFileResuming,isFileDownloaded;
                File currentDownloadFileInfo = null;
                File infoFile = new File(getFileURIs()[FILE_DESTINATION_INDEX],
                        UMFileUtil.getFilename(fileUrl) + INFO_FILE_EXTENSION);
                File fileDestination = new File(getFileURIs()[FILE_DESTINATION_INDEX],
                        UMFileUtil.getFilename(fileUrl) + UNFINISHED_FILE_EXTENSION);

                CatalogEntryInfo info = new CatalogEntryInfo();
                info.acquisitionStatus = CatalogEntryInfo.ACQUISITION_STATUS_INPROGRESS;
                info.downloadID = String.valueOf(currentDownloadId);
                info.downloadTotalSize = -1;
                info.acquisitionStatus = CatalogEntryInfo.ACQUISITION_STATUS_INPROGRESS;
                info.fileURI = fileDestination.getAbsolutePath();
                info.srcURLs = new String[]{fileUrl};
                CatalogController.setEntryInfo(getFeed().entries[currentEntryIdIndex].id, info,
                        CatalogController.SHARED_RESOURCE, networkManager.getContext());

                try {
                    URL url = new URL(fileUrl);
                    if (infoFile.exists()) {
                        con = (HttpURLConnection) url.openConnection();
                        con.setRequestMethod(DOWNLOAD_REQUEST_METHOD);
                        con.connect();
                        fileInfoLastModified = con.getHeaderField(HEADER_LAST_MODIFIED);
                        infoFromFile = getFileContentFromFile(infoFile);
                        con.disconnect();
                        con = null;

                    }

                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod(DOWNLOAD_REQUEST_METHOD);
                    existingFileLength = new File(UMFileUtil.getFilename(getFileURIs()[FILE_DESTINATION_INDEX])
                            + UNFINISHED_FILE_EXTENSION).length();
                    if (infoFromFile != null && infoFromFile.get(HEADER_LAST_MODIFIED).equals(fileInfoLastModified)) {
                        isFileResuming = true;
                        con.setRequestProperty(HEADER_RANGE, "bytes=" + String.valueOf(existingFileLength) + "-");
                    } else {
                        isFileResuming = false;
                    }
                    con.connect();
                    totalBytesToDownload = con.getContentLength();
                    in = con.getInputStream();
                    out = new FileOutputStream(fileDestination, true);
                    byte[] buffer = new byte[10240];
                    long total = 0;
                    fileInfoLastModified = con.getHeaderField(HEADER_LAST_MODIFIED);

                    if (fileInfoLastModified != null && !isFileResuming) {
                        currentDownloadFileInfo = saveFileInfoToFile(fileInfoLastModified,fileDestination);
                    }
                    if (fileInfoLastModified != null && isFileResuming) {
                        currentDownloadFileInfo = infoFile;
                    }
                    while ((bytesDownloadedSoFar = in.read(buffer)) > 0) {
                        total += bytesDownloadedSoFar;
                        out.write(buffer, 0, bytesDownloadedSoFar);
                        totalBytesDownloadedSoFar=total;
                    }

                    if(isFileResuming){
                        isFileDownloaded= fileDestination.exists();
                    }else{
                        isFileDownloaded=fileDestination.exists() && fileDestination.length()>= totalBytesToDownload;
                    }

                    if(isFileDownloaded){
                        saveFileWithNewExtension(fileDestination.getAbsolutePath());
                        if(currentDownloadFileInfo!=null){
                            currentDownloadFileInfo.delete();
                        }
                        currentEntryAcquisitionTaskStatus =UstadMobileSystemImpl.DLSTATUS_SUCCESSFUL;

                        if(currentEntryIdIndex==(getFeed().entries.length-1)){
                            updateTimer.cancel();
                            updateTimer=null;
                            networkManager.removeNotification(NOTIFICATION_TYPE_ACQUISITION);
                        }
                        currentDownloadId++;
                        entryAcquisitionThread =null;
                        acquireNextFile();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                } finally {
                    UMIOUtils.closeOutputStream(out);
                    UMIOUtils.closeInputStream(in);
                    if (con != null)
                        con.disconnect();
                }
            }
        });
        entryAcquisitionThread.start();
    }


    /**
     * Save file with it's original extension after success downloaded
     * @param fileUri - Current file URL to be saved with new extension
     * @return
     */
    private File saveFileWithNewExtension(String fileUri){
        File oldFile=new File(fileUri);
        File newFile=new File(fileUri.substring(0,fileUri.length()-UNFINISHED_FILE_EXTENSION.length()));
        boolean success=oldFile.renameTo(newFile);
        if(success){
            return newFile;
        }
        return null;
    }

    /**
     * Get file download absolute URI's (Download destination & File URL)
     * @return
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
     * Create a temporally file to store current partial file information.
     * @param fileInfoLastModified Header File last-modified
     * @return
     */
    private File saveFileInfoToFile(String fileInfoLastModified,File fileDestination){
        String infoLastModified=fileInfoLastModified==null? null:fileInfoLastModified;
        File tempFileInfo=null;
        try{
            String destinationPath=fileDestination.getAbsolutePath();
            tempFileInfo=new File(destinationPath.substring(0,
                    destinationPath.length()-UNFINISHED_FILE_EXTENSION.length())+INFO_FILE_EXTENSION);
            FileWriter writer = new FileWriter(tempFileInfo.getAbsolutePath());
            JSONObject fileObject=new JSONObject();
            fileObject.put(HEADER_LAST_MODIFIED,infoLastModified);
            writer.append(fileObject.toString());
            writer.flush();
            writer.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return tempFileInfo;

    }

    /**
     * Read temporally file content as JSON and get last modified dates
     * @param file - temporally file
     * @return
     */
    private JSONObject getFileContentFromFile(File file){

        StringBuilder fileText = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
            String line;
            while ((line = reader.readLine()) != null) {
                fileText.append(line);
                fileText.append('\n');
            }
            reader.close() ;
            return new JSONObject(fileText.toString());
        }catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public void onConnected(final InputStream inputStream, final OutputStream outputStream) {
        String acquireCommand = BluetoothServer.CMD_ACQUIRE_ENTRY +" "+networkManager.getDeviceIPAddress()+"\n";
        try {
            outputStream.write(acquireCommand.getBytes());
            outputStream.flush();
            System.out.print("AcquisitionTask: Sending Command "+acquireCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String response = reader.readLine();
            if(response.startsWith(BluetoothServer.CMD_ACQUIRE_ENTRY_FEEDBACK)) {
                System.out.print("AcquisitionTask: Receive Response "+response);
                String [] groupInfo=response.substring((BluetoothServer.CMD_ACQUIRE_ENTRY_FEEDBACK.length()+1),
                        response.length()).split(CMD_SEPARATOR);
                currentGroupIPAddress =groupInfo[2].replace("/","");
                currentGroupSSID =groupInfo[0];
                networkManager.connectWifi(groupInfo[0],groupInfo[1]);
                UMIOUtils.closeInputStream(inputStream);
                UMIOUtils.closeOutputStream(outputStream);
                networkManager.disconnectBluetooth();
            }
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cancel() {
        entryAcquisitionThread.interrupt();
        if(entryAcquisitionThread.isInterrupted()){
            currentEntryAcquisitionTaskStatus =UstadMobileSystemImpl.DLSTATUS_FAILED;
        }
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
    public void wifiConnectionChanged(String ssid) {
        if(currentGroupSSID.equals(ssid)){
            String fileUrl="http://"+ currentGroupIPAddress +":"+SERVICE_PORT+"/catalog/entry/"
                    +getFeed().entries[currentEntryIdIndex].id;
            downloadCurrentFile(fileUrl);
        }
    }

    /**
     * Get entry acquisition task status values of which contain amount of bytes downloaded so far,
     * total bytes to be downloaded and the current status
     * @return
     */

    public int [] getEntryAcquisitionStatus(){
        int statusVal[]=new int[3];
        statusVal[UstadMobileSystemImpl.IDX_BYTES_TOTAL]=(int) totalBytesToDownload;
        statusVal[UstadMobileSystemImpl.IDX_STATUS]= currentEntryAcquisitionTaskStatus;
        statusVal[UstadMobileSystemImpl.IDX_DOWNLOADED_SO_FAR]= (int)totalBytesDownloadedSoFar;
        return statusVal;
    }
}
