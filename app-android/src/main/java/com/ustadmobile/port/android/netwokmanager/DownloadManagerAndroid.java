package com.ustadmobile.port.android.netwokmanager;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.impl.AcquisitionManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.port.sharedse.networkmanager.AcquisitionTask;
import com.ustadmobile.port.sharedse.networkmanager.BluetoothConnectionHandler;
import com.ustadmobile.port.sharedse.networkmanager.EntryCheckResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import edu.rit.se.wifibuddy.WifiDirectHandler;

import static com.ustadmobile.port.sharedse.networkmanager.NetworkManager.NOTIFICATION_TYPE_SERVER;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManager.QUEUE_ENTRY_ACQUISITION;


/**
 * Created by kileha3 on 07/03/2017.
 */

public class DownloadManagerAndroid extends AcquisitionTask implements BluetoothConnectionHandler{

    private NetworkManagerAndroid managerAndroid;
    public static final String TAG="DownloadManagerAndroid";
    private File currentDownloadFileInfo=null;



    private static int currentStatus=-1;
    /**
     * Broadcast sent when the file download task is finished
     */
    public static final String ACTION_DOWNLOAD_COMPLETED ="action_download_completed";


    /**
     * Broadcast sent when actual file download starts (It will be used
     * to notify the Download manager once if the device is connected to No-Prompt network)
     */
    public static final String ACTION_DOWNLOAD_STARTING ="action_download_starts";

    /**
     * Extra data flag (Feed Download ID) which will passed to the intent
     * for the purpose of updating the UI)
     */
    public static final String EXTRA_DOWNLOAD_ID ="extra_download_id";

    /**
     * Extra data flag (entry Download ID) which will passed to the intent
     * for the purpose of updating the UI)
     */
    public static final String EXTRA_ENTRY_ID ="extra_entry_id";
    /**
     * Extra data flag (Server Address) which will passed to the broadcast intent
     */
    public static final String EXTRA_DOWNLOAD_SOURCE_ADDRESS ="extra_download_source_address";

    /**
     * Main directory where all entries can be obtained from on the server device
     */
    public static final String SERVER_MAIN_FIL_DIR ="/catalog/entry/";

    /**
     * Default server IP address
     */
    public static  String SERVER_ADDRESS ="192.168.49.1";

    /**
     * Tag to indicate part of the file to be downloaded when resuming
     */
    public static final String HEADER_RANGE="Range";
    /**
     * Tag to indicate when was the last file modified date
     */
    public static final String HEADER_LAST_MODIFIED ="Last-Modified";

    /**
     * Extension set to the file which keeps information of the unfinished file.
     */
    public static final String INFO_FILE_EXTENSION=".dlinfo";
    /**
     * Extension set to unfinished file so that it can't be readable
     */
    public static final String UNFINISHED_FILE_EXTENSION=".part";

    /**
     * HTTP request method
     */
    public static final String DOWNLOAD_REQUEST_METHOD="GET";

    /**
     * Flag to indicate that file will be downloaded from the cloud
     */
    public static final int DOWNLOAD_SOURCE_CLOUD=2;
    /**
     * Flag to indicate that the file will be downloaded from the peer device
     */
    public static final int DOWNLOAD_SOURCE_PEER_WIFI=0;

    public static final int DOWNLOAD_SOURCE_PEER_WIFI_DIRECT=1;

    /**
     * Flag to indicate that the file is resuming
     * - it was downloaded partially before
     */
    private boolean isFileResuming=false;

    private File fileDestination=null;

    private Context mContext;

    private DownLoadTask downloadTask;

    private String notificationTitle=null,notificationMessage=null;

    /**
     * Time passed before sending an update to the progressbar
     */
    private static  Long TIME_PASSED_FOR_PROGRESS_UPDATE = Calendar.getInstance().getTimeInMillis();

    /**
     * Time to wait before updating the progressbar
     */
    private final static int WAITING_TIME_TO_UPDATE = 500;

    /**
     * Keep the current task position - loop through feed entries
     */
    private int currentEntryIndex=0;

    private String srcFileURL =null;

    public DownloadManagerAndroid(UstadJSOPDSFeed feed, NetworkManagerAndroid managerAndroid, Object context) {
        super(feed);
        this.managerAndroid = managerAndroid;
        mContext=(Context) context;
    }

    @Override
    public void start() {
        prepareDownloadIds();
        checkDownloadSource();
    }

    private void checkDownloadSource(){
        setNotificationTitleMessage();
        String fileId=getFeed().entries[currentEntryIndex].id;
        setDownloadID(managerAndroid.getEntryIdToDownloadIdMap().get(fileId));
        EntryCheckResponse checkResponse= managerAndroid.entryCheckResponse(fileId);

        if(checkResponse!=null && checkResponse.isFileAvailable()) {

            String deviceAddress= managerAndroid.getIpAddress();
            String peerAddress=checkResponse.getNetworkNode().getDeviceIpAddress();

            if(managerAndroid.areOnTheSameNetwork(peerAddress,deviceAddress)){

                srcFileURL="http://"+peerAddress+":"+checkResponse.getNetworkNode().getPort()+"/"
                        +SERVER_MAIN_FIL_DIR;
                managerAndroid.setDownloadSource(DOWNLOAD_SOURCE_PEER_WIFI);
                downloadInBackground();
            }else{
                //download from peer using bluetooth handshakes
                String bluetoothAddress=checkResponse.getNetworkNode().getDeviceBluetoothMacAddress();
                managerAndroid.connectBluetooth(bluetoothAddress,this);
                managerAndroid.setDownloadSource(DOWNLOAD_SOURCE_PEER_WIFI_DIRECT);
            }



        }else {
            managerAndroid.setDownloadSource(DOWNLOAD_SOURCE_CLOUD);
            downloadInBackground();
        }
    }

    private void prepareDownloadIds(){

        for(int position = 0; position < getFeed().entries.length; position++) {
            long downloadId=(long) new AtomicInteger().incrementAndGet();
            managerAndroid.getEntryIdToDownloadIdMap().put(
                    getFeed().entries[position].id,downloadId);
            managerAndroid.getDownloadIdToEntryIdMap().put(downloadId,getFeed().entries[position].id);
            managerAndroid.getDownloadIdToDownloadStatusMap().put(downloadId,new int[3]);
        }
    }


    private void setNotificationTitleMessage(){
        notificationTitle=getFeed().entries[currentEntryIndex].title;
        notificationMessage= getFeed().entries.length > 1
                ? "Downloading "+((currentEntryIndex+1)/getFeed().entries.length)+" files"
                : "Download in progress";
    }


    /**
     * Initialize download task
     */
    private void downloadInBackground() {
        downloadTask=new DownLoadTask();
        downloadTask.execute();
    }

    @Override
    public void onConnected(InputStream inputStream, OutputStream outputStream) {
        //TODO: handle communication
    }

    private class DownLoadTask extends AsyncTask<String,Long,Boolean>{

        private long newFileLength=0;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            currentStatus=UstadMobileSystemImpl.DLSTATUS_PENDING;
            setDownloadStatus(currentStatus);
            setDownloadTotalBytes(0);
            updateDownloadStatus();
            if(managerAndroid.isSuperNodeEnabled()){
                managerAndroid.removeNotification(NOTIFICATION_TYPE_SERVER);
                managerAndroid.addNotification(QUEUE_ENTRY_ACQUISITION,
                        notificationTitle,notificationMessage);
            }else{
                managerAndroid.addNotification(QUEUE_ENTRY_ACQUISITION,notificationTitle,
                        notificationMessage);
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            InputStream in = null;
            HttpURLConnection con = null;
            OutputStream out = null;
            long existingFileLength=0;
            String fileInfoLastModified=null,downloadUri=null;
            JSONObject infoFromFile=null;
            int bytesDownloadedSoFar;

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
            String [] acquisitionLink=getFeed().
                    entries[currentEntryIndex].getFirstAcquisitionLink(null);
            String downloadUrl = UMFileUtil.resolveLink(feedHref,acquisitionLink[UstadJSOPDSEntry.LINK_HREF]);

            File infoFile=new File(downloadDestDir,UMFileUtil.getFilename(downloadUrl)+INFO_FILE_EXTENSION);
            fileDestination = new File(downloadDestDir,UMFileUtil.getFilename(downloadUrl)+UNFINISHED_FILE_EXTENSION);


            CatalogEntryInfo info = new CatalogEntryInfo();
            info.acquisitionStatus = CatalogEntryInfo.ACQUISITION_STATUS_INPROGRESS;
            info.downloadID = String.valueOf(getDownloadID());
            info.downloadTotalSize = -1;
            info.acquisitionStatus = CatalogEntryInfo.ACQUISITION_STATUS_INPROGRESS;
            info.fileURI = fileDestination.getAbsolutePath();
            info.srcURLs = new String[]{downloadUrl};
            CatalogController.setEntryInfo(getFeed().entries[currentEntryIndex].id, info,
                    CatalogController.SHARED_RESOURCE, mContext);


            try {

                if(managerAndroid.getDownloadSource()==DOWNLOAD_SOURCE_CLOUD){
                    downloadUri= downloadUrl;
                }else{
                    downloadUri= UMFileUtil.joinPaths(new String[]{srcFileURL,
                            getFeed().entries[currentEntryIndex].id});
                }

                //Format and encode URL's
                URL url = new URL(downloadUri.replaceAll("\\s+","%20"));
                Log.d(TAG,"Download URL :"+downloadUri);

                //If file was partially downloaded. get information and resume
                if(infoFile.exists()){

                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod(DOWNLOAD_REQUEST_METHOD);
                    con.connect();
                    fileInfoLastModified=con.getHeaderField(HEADER_LAST_MODIFIED);
                    existingFileLength=fileDestination.length();
                    infoFromFile= getFileContentFromFile(infoFile);
                    con.disconnect();
                    con=null;

                }


                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod(DOWNLOAD_REQUEST_METHOD);
                if(infoFromFile!=null && infoFromFile.get(HEADER_LAST_MODIFIED).equals(fileInfoLastModified)){
                    isFileResuming=true;
                    con.setRequestProperty(HEADER_RANGE,"bytes="+String.valueOf(existingFileLength)+"-");
                }else{
                    isFileResuming=false;
                }
                con.connect();
                newFileLength = con.getContentLength();
                setDownloadTotalBytes((int)newFileLength);
                in = con.getInputStream();
                out = new FileOutputStream(fileDestination,true);
                byte[] buffer = new byte[10240];
                long total=0L;
                Map<String,List<String>> headers= con.getHeaderFields();
                Log.d(TAG,headers.toString());
                fileInfoLastModified=con.getHeaderField(HEADER_LAST_MODIFIED);

                //check for file ETag or Last-modified date and log them to the temp file info
                if(fileInfoLastModified!=null && !isFileResuming){
                    currentDownloadFileInfo= saveFileInfoToFile(fileInfoLastModified);
                }else if(fileInfoLastModified!=null && isFileResuming){
                    currentDownloadFileInfo=infoFile;
                }

                while ((bytesDownloadedSoFar = in.read(buffer)) > 0 && currentStatus!=UstadMobileSystemImpl.DLSTATUS_FAILED) {
                    total+= bytesDownloadedSoFar;
                    out.write(buffer, 0, bytesDownloadedSoFar);
                    publishProgress(total);

                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                UMIOUtils.closeOutputStream(out);
                UMIOUtils.closeInputStream(in);
                if (con != null)
                    con.disconnect();
            }

            //check if the file is new or it was resuming and check file length accordingly
            if(isFileResuming){
                return fileDestination.exists();
            }else{
                return fileDestination.exists() && fileDestination.length()>=newFileLength;
            }
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            long progress=((values[0]*100L)/newFileLength);
            Log.d(WifiDirectHandler.TAG,":downloading ="+progress);
            setBytesDownloadedSoFar(Integer.parseInt(String.valueOf(values[0])));
            updateProgress(progress);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled() {
            currentStatus=UstadMobileSystemImpl.DLSTATUS_FAILED;
            setDownloadStatus(currentStatus);
            updateDownloadStatus();
            fireAcquisitionTaskCompleted();
        }

        @Override
        protected void onPostExecute(Boolean isFileAvailable) {

            if(isFileAvailable){
              if(getFeed().entries.length>1 &&  currentEntryIndex <=(getFeed().entries.length-2)){
                  currentEntryIndex++;
                  downloadTask=null;
                  downloadInBackground();

              }else{
                  if((getFeed().entries.length-1)==currentEntryIndex){
                      currentStatus= UstadMobileSystemImpl.DLSTATUS_SUCCESSFUL;
                      saveFileWithNewExtension(fileDestination.getAbsolutePath());
                      if(currentDownloadFileInfo!=null){
                          currentDownloadFileInfo.delete();
                      }
                      updateDownloadStatus();
                      Intent downloadCompletedIntent=new Intent(ACTION_DOWNLOAD_COMPLETED);
                      downloadCompletedIntent.putExtra(EXTRA_DOWNLOAD_ID, getDownloadID());
                      mContext.sendBroadcast(downloadCompletedIntent);
                      managerAndroid.removeNotification(QUEUE_ENTRY_ACQUISITION);

                      /*
                      dismissDownloadNotification();
                      if(currentDownloadSource==DOWNLOAD_SOURCE_PEER){
                          managerAndroid.reconnectToThePreviousNetwork();
                      }*/
                      fireAcquisitionTaskCompleted();

                  }
              }

            }else{
                /*Partial Download: download task was incomplete, possibly it was interrupted or cancelled*/
                currentStatus=UstadMobileSystemImpl.DLSTATUS_FAILED;
                setDownloadStatus(currentStatus);
            }

        }

    }

    /**
     * Update download percentage on the progress bar
     * @param progress - percentage completed so far
     */
    private void updateProgress(long progress){
        Long time_now = Calendar.getInstance().getTimeInMillis();
         int max_progress_status=100;
        if(((time_now - TIME_PASSED_FOR_PROGRESS_UPDATE) < WAITING_TIME_TO_UPDATE) || (progress < 0 && progress > max_progress_status)) {
            return;
        }
        TIME_PASSED_FOR_PROGRESS_UPDATE = time_now;
        currentStatus=UstadMobileSystemImpl.DLSTATUS_RUNNING;
        updateDownloadStatus();
        setDownloadStatus(currentStatus);
        setNotificationTitleMessage();
        managerAndroid.updateNotification(QUEUE_ENTRY_ACQUISITION,(int)progress,notificationTitle,notificationMessage);
    }

    /**
     * Update download status as the task is running
     */
    private void updateDownloadStatus(){
        managerAndroid.getDownloadIdToDownloadStatusMap().put(getDownloadID(),getDownloadStatus());
    }


    @Override
    public synchronized boolean stop() {
        if(downloadTask!=null && downloadTask.getStatus()!= AsyncTask.Status.FINISHED){
            downloadTask.cancel(true);
            return downloadTask.isCancelled();
        }else{
            return false;
        }
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
     * Create a temporally file to store file info.
     * @param fileInfoLastModified Header File last-modified
     * @return
     */
    private File saveFileInfoToFile(String fileInfoLastModified){
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



}
