package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.impl.AcquisitionManager;
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
import java.net.URLEncoder;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ustadmobile.port.sharedse.networkmanager.BluetoothServer.CMD_SEPARATOR;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManager.DOWNLOAD_FROM_CLOUD;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManager.DOWNLOAD_FROM_PEER_ON_DIFFERENT_NETWORK;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManager.DOWNLOAD_FROM_PEER_ON_SAME_NETWORK;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManager.SERVICE_PORT;

/**
 * Created by kileha3 on 09/05/2017.
 */

public class AcquisitionTask extends NetworkTask implements BluetoothConnectionHandler,NetworkManagerListener{
    private static final int MAX_DISCOVERY_DURATION=2 * 60 * 1000;
    private UstadJSOPDSFeed feed;
    protected NetworkManagerTaskListener listener;
    private int currentEntryIdIndex;
    private String groupIpAddress,networkSSID;
    private static final int FILE_DESTINATION_INDEX=1;
    private static final int FILE_DOWNLOAD_URL_INDEX=0;

    private static final String INFO_FILE_EXTENSION=".dlinfo";
    private static final String UNFINISHED_FILE_EXTENSION=".part";
    private static final String HEADER_RANGE="Range";
    private static final String HEADER_LAST_MODIFIED ="Last-Modified";
    private static final String DOWNLOAD_REQUEST_METHOD="GET";
    private long currentDownloadId=0L;



    public AcquisitionTask(UstadJSOPDSFeed feed,NetworkManager networkManager){
        super(networkManager);
        this.feed=feed;
    }

    /**
     * Start the download task
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
     * (Cloud, Peer on the same network or Peer on the different network)
     *
     * if is from Cloud: initiate download
     *            Peer on the same network: initiate download
     *            Peer on different network: initiate bluetooth connection in order to trigger
     *            WiFi-Direct group formation on the host device
     */
    private void acquireNextFile(){

        if(currentEntryIdIndex < feed.entries.length) {
            currentDownloadId= new AtomicInteger().incrementAndGet();
            String entryId = feed.entries[currentEntryIdIndex].id;

            EntryCheckResponse entryCheckResponse=networkManager.getEntryResponseWithLocalFile(entryId);

            if(entryCheckResponse!=null){
                NetworkNode node=entryCheckResponse.getNetworkNode();
                if(node.getNetworkServiceLastUpdated() < MAX_DISCOVERY_DURATION){
                    networkManager.handleFileAcquisitionInformationAvailable(entryId,currentDownloadId,
                            DOWNLOAD_FROM_PEER_ON_SAME_NETWORK);
                    String fileURI="http://"+node.getDeviceIpAddress()+":"+SERVICE_PORT+"/catalog/entry/"+entryId;
                    acquireEntry(fileURI);
                }else{
                    if(node.getWifiDirectLastUpdated() < MAX_DISCOVERY_DURATION){
                        networkManager.handleFileAcquisitionInformationAvailable(entryId,currentDownloadId,
                                DOWNLOAD_FROM_PEER_ON_DIFFERENT_NETWORK);
                        networkManager.connectBluetooth(node.getDeviceBluetoothMacAddress(),this);
                    }else{
                        networkManager.handleFileAcquisitionInformationAvailable(entryId,currentDownloadId,DOWNLOAD_FROM_CLOUD);
                        acquireEntry(getFileURIs()[FILE_DOWNLOAD_URL_INDEX]);

                    }
                }
            }else{
                networkManager.handleFileAcquisitionInformationAvailable(entryId,currentDownloadId,DOWNLOAD_FROM_CLOUD);
                acquireEntry(getFileURIs()[FILE_DOWNLOAD_URL_INDEX]);
            }

        }else{
            networkManager.handleTaskCompleted(this);
        }
    }


    private void acquireEntry(final String fileUrl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream in = null;
                HttpURLConnection con = null;
                OutputStream out = null;
                long existingFileLength,newFileLength;
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
                    URL url = new URL(URLEncoder.encode(fileUrl, "UTF-8"));
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
                    newFileLength = con.getContentLength();
                    in = con.getInputStream();
                    out = new FileOutputStream(fileDestination, true);
                    byte[] buffer = new byte[10240];
                    int total = 0;
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
                        //TODO: handle download update

                    }

                    if(isFileResuming){
                        isFileDownloaded= fileDestination.exists();
                    }else{
                        isFileDownloaded=fileDestination.exists() && fileDestination.length()>=newFileLength;
                    }

                    if(isFileDownloaded){
                        saveFileWithNewExtension(fileDestination.getAbsolutePath());
                        if(currentDownloadFileInfo!=null){
                            currentDownloadFileInfo.delete();
                        }
                        currentDownloadId++;
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
        }).start();
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
     * Create a temporally file to store file info.
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
     * Read the temporally (.dinfo) file content as JSON
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
    public void onConnected(InputStream inputStream, OutputStream outputStream) {
        String acquireCommand = BluetoothServer.CMD_ACQUIRE_ENTRY +"\n";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            outputStream.write(acquireCommand.getBytes());
            String response = reader.readLine();
            if(response.startsWith(BluetoothServer.CMD_ENTRY_STATUS_FEEDBACK)) {
               String [] groupInfo=response.substring((BluetoothServer.CMD_ACQUIRE_ENTRY_FEEDBACK.length()+1),
                       response.length()).split(CMD_SEPARATOR);
                groupIpAddress=groupInfo[2];
                networkSSID=groupInfo[0];
                networkManager.connectWifi(groupInfo[0],groupInfo[1]);
            }
        }catch(IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void cancel() {

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

    public synchronized boolean stop(){
        return false;
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
        if(networkSSID.equals(ssid)){
            String fileUrl="http://"+groupIpAddress+":"+SERVICE_PORT+"/catalog/entry/"+getFeed().entries[currentEntryIdIndex].id;
            acquireEntry(fileUrl);
        }
    }
}
