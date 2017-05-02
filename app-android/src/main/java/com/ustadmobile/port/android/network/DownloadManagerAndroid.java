package com.ustadmobile.port.android.network;

import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.impl.AcquisitionManager;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.port.sharedse.network.DownloadTask;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import edu.rit.se.wifibuddy.WifiDirectHandler;

import static com.ustadmobile.port.android.network.BluetoothConnectionManager.BLUETOOTH_ADDRESS;
import static com.ustadmobile.port.android.network.BluetoothConnectionManager.BLUETOOTH_TASK_TYPE_ACQUIRE;
import static com.ustadmobile.port.android.network.BluetoothConnectionManager.COMMAND_TAG_FILE_ACQUIRE_REQUEST;
import static com.ustadmobile.port.android.network.BluetoothConnectionManager.FILE_AVAILABILITY_RESPONSE;
import static com.ustadmobile.port.android.network.BluetoothConnectionManager.FILE_AVAILABLE_COMMAND_SEPARATOR;
import static com.ustadmobile.port.android.network.BluetoothConnectionManager.FILE_IDS_SEPARATOR;
import static com.ustadmobile.port.android.network.BluetoothConnectionManager.STATE_CONNECTED;


/**
 * Created by kileha3 on 07/03/2017.
 */

public class DownloadManagerAndroid extends DownloadTask {

    private NetworkManagerAndroid p2pManager;
    private File currentDownloadFileInfo=null;
    private BluetoothConnectionManager bConnectionManager;



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
    public static final String EXTRA_DOWNLOAD_ENTRY_ID ="extra_download_entry_id";
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
    public static final int DOWNLOAD_SOURCE_CLOUD=1;
    /**
     * Flag to indicate that the file will be downloaded from the peer device
     */
    public static final int DOWNLOAD_SOURCE_PEER=0;

    /**
     * Flag to indicate that the file is resuming
     * - it was downloaded partially before
     */
    private boolean isFileResuming=false;

    private File fileDestination=null;

    private Context context;

    private DownLoadTask downloadTask;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    /**
     * Time passed before sending an update to the progressbar
     */
    private static  Long TIME_PASSED_FOR_PROGRESS_UPDATE = Calendar.getInstance().getTimeInMillis();

    /**
     * Time to wait before updating the progressbar
     */
    private final static int WAITING_TIME_TO_UPDATE = 500;

    /**
     * Port on which NanoHTTP server will be listening for the HTTP request
     */
    public static final int COMMUNICATION_PORT=8001;

    /**
     * Keep the current task position - loop through feed entries
     */
    private int currentEntryIndex=0;

    /**
     * Track current download source as per flags set above.
     */
    private int currentDownloadSource;
    private static final  int NOTIFICATION_ID=1;
    private String srcfileURL=null;


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            Log.d(WifiDirectHandler.TAG,"Action Note "+action);
            switch (action){

                case BluetoothConnectionManager.ACTION_DEVICE_BLUETOOTH_CONNECTIVITY_CHANGE:
                    int state=intent.getIntExtra(BluetoothConnectionManager.EXTRA_DEVICE_BLUETOOTH_CONNECTIVITY_FLAG,
                            BluetoothConnectionManager.STATE_NONE);

                    //Check if the device is connected to server side bluetooth
                    // and send file acquisition request command
                    if(state==STATE_CONNECTED){
                        String taskType=intent.getStringExtra(BluetoothConnectionManager.EXTRA_BLUETOOTH_TASK_TYPE);

                        if(BLUETOOTH_TASK_TYPE_ACQUIRE.equals(taskType)){

                            String acquireCommand=BLUETOOTH_TASK_TYPE_ACQUIRE + FILE_AVAILABLE_COMMAND_SEPARATOR
                                    + COMMAND_TAG_FILE_ACQUIRE_REQUEST + FILE_AVAILABLE_COMMAND_SEPARATOR
                                    +p2pManager.getP2pService().getWifiDirectHandlerAPI().getThisDevice().deviceAddress.replace(
                                    FILE_AVAILABLE_COMMAND_SEPARATOR,FILE_IDS_SEPARATOR);
                            bConnectionManager.sendCommandMessage(acquireCommand);
                        }

                    }
                    break;
                case ACTION_DOWNLOAD_STARTING:
                    //with  wifi-direct IP address will not be null
                    String ipAddress=intent.getStringExtra(EXTRA_DOWNLOAD_SOURCE_ADDRESS);
                    //If the IP address is not null then the WiFi direct
                    // technique is used other wise No-Prompt connection is made
                    if(ipAddress!=null && !ipAddress.isEmpty()){
                        srcfileURL="http://"+ipAddress+":"+COMMUNICATION_PORT+ SERVER_MAIN_FIL_DIR;

                    }else{
                        srcfileURL="http://"+SERVER_ADDRESS+":"+COMMUNICATION_PORT+ SERVER_MAIN_FIL_DIR;

                    }
                    Log.d(BluetoothConnectionManager.TAG,srcfileURL);
                    downloadInBackground();
            }


        }
    };

    public DownloadManagerAndroid(UstadJSOPDSFeed feed, NetworkManagerAndroid p2pManager) {
        super(feed);
        this.p2pManager=p2pManager;
        context=this.p2pManager.getP2pService().getApplicationContext();
        bConnectionManager=p2pManager.getBluetoothConnectionManager();
    }

    @Override
    public void start() {
        checkDownloadSource();
    }

    /**
     * In here is where the download manager will decide on where to get the file - cloud or peer device
     * if source is:-
     * CLOUD: download directly from cloud server
     *    PEER: Request peer device a connection once connection
     *          has been made download the file from there
     */
    private void checkDownloadSource(){
        HashMap<String,HashMap<String,String>> availableFiles=p2pManager.getAvailableFiles();
        String fileId=getFeed().entries[currentEntryIndex].id;
        //Map entry ID to it's corresponding download ID
        DownloadManagerAndroid.this.entryDownloadLog.put(fileId,getDownloadID());

        //If a file can be downloaded locally, establish bluetooth connection and listen
        // for the connection otherwise download from the internet
            if(availableFiles.keySet().size()>0 && Boolean.parseBoolean(availableFiles.get(fileId).get(FILE_AVAILABILITY_RESPONSE))){

                String peerBluetoothAddress=availableFiles.get(fileId).get(BLUETOOTH_ADDRESS);
                BluetoothAdapter bluetoothAdapter=bConnectionManager.getBluetoothAdapter();
                BluetoothDevice device=bluetoothAdapter.getRemoteDevice(peerBluetoothAddress);
                p2pManager.getBluetoothConnectionManager().start();
                bConnectionManager.setBluetoothTaskType(BLUETOOTH_TASK_TYPE_ACQUIRE);
                p2pManager.getBluetoothConnectionManager().connectToBluetoothDevice(device,false);

                IntentFilter filter=new IntentFilter();
                filter.addAction(BluetoothConnectionManager.ACTION_DEVICE_BLUETOOTH_CONNECTIVITY_CHANGE);
                filter.addAction(ACTION_DOWNLOAD_STARTING);
                LocalBroadcastManager.getInstance(context).registerReceiver(mBroadcastReceiver, filter);
                currentDownloadSource=DOWNLOAD_SOURCE_PEER;


            }else{
                currentDownloadSource=DOWNLOAD_SOURCE_CLOUD;
                downloadInBackground();
            }
    }


    /**
     * Initialize download task
     */

    private void downloadInBackground() {
        downloadTask=new DownLoadTask();
        downloadTask.execute();
    }

    private class DownLoadTask extends AsyncTask<String,Integer,Boolean>{

        private long newFileLength=0;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            currentStatus=DOWNLOAD_STATUS_QUEUED;
            setDownloadStatus(currentStatus);
            setDownloadTotalBytes(0);
            updateDownloadStatus();
            showDownloadNotification();
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
            String downloadUrl = UMFileUtil.resolveLink(feedHref,getFeed().
                    entries[currentEntryIndex].getFirstAcquisitionLink(null)[UstadJSOPDSEntry.LINK_HREF]);

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
                    CatalogController.SHARED_RESOURCE, context);


            try {

                if(currentDownloadSource==DOWNLOAD_SOURCE_CLOUD){
                    downloadUri= downloadUrl;
                }else{
                    downloadUri= UMFileUtil.joinPaths(new String[]{srcfileURL,
                            getFeed().entries[currentEntryIndex].id});
                }

                URL url = new URL(downloadUri.replaceAll("\\s+","%20"));
                Log.d(BluetoothConnectionManager.TAG,"Download URL :"+downloadUri);

                if(infoFile.exists()){
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod(DOWNLOAD_REQUEST_METHOD);
                    con.connect();
                    fileInfoLastModified=con.getHeaderField(HEADER_LAST_MODIFIED);
                    infoFromFile= getFileContentFromFile(infoFile);
                    con.disconnect();
                    con=null;

                }


                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod(DOWNLOAD_REQUEST_METHOD);
                existingFileLength=new File(UMFileUtil.getFilename(downloadUrl)+UNFINISHED_FILE_EXTENSION).length();
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
                int total=0;
                Map<String,List<String>> headers= con.getHeaderFields();
                Log.d(BluetoothConnectionManager.TAG,headers.toString());
                fileInfoLastModified=con.getHeaderField(HEADER_LAST_MODIFIED);

                //check for file ETag or Last-modified date and log them to the temp file info
                if(fileInfoLastModified!=null && !isFileResuming){
                    currentDownloadFileInfo= saveFileInfoToFile(fileInfoLastModified);
                }else if(fileInfoLastModified!=null && isFileResuming){
                    currentDownloadFileInfo=infoFile;
                }

                while ((bytesDownloadedSoFar = in.read(buffer)) > 0 && currentStatus!=DOWNLOAD_STATUS_CANCELLED) {
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
        protected void onProgressUpdate(Integer... values) {
            int progress=(int)((values[0]*100)/newFileLength);
            Log.d(WifiDirectHandler.TAG,":downloading ="+progress);
            updateDownloadNotification(progress);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled() {
            currentStatus=DOWNLOAD_STATUS_CANCELLED;
            setDownloadStatus(currentStatus);
            updateDownloadStatus();
            fireDownloadTaskEnded();
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
                      currentStatus=DOWNLOAD_STATUS_COMPLETED;
                      saveFileWithNewExtension(fileDestination.getAbsolutePath());
                      if(currentDownloadFileInfo!=null){
                          currentDownloadFileInfo.delete();
                      }
                      updateDownloadStatus();
                      Intent downloadCompleted=new Intent(ACTION_DOWNLOAD_COMPLETED);
                      downloadCompleted.putExtra(EXTRA_DOWNLOAD_ID, getDownloadID());
                      downloadCompleted.putExtra(EXTRA_DOWNLOAD_ENTRY_ID,getFeed().entries[currentEntryIndex].id);
                       context.sendBroadcast(downloadCompleted);
                       dismissDownloadNotification();
                      if(currentDownloadSource==DOWNLOAD_SOURCE_PEER){
                          p2pManager.reconnectToThePreviousNetwork();
                      }

                      //notify Feed download task ended
                      fireDownloadTaskEnded();

                  }
              }

            }else{
                /*Partial Download: download task was incomplete, possibly it was interrupted or cancelled*/
                currentStatus=DOWNLOAD_STATUS_FAILED;
                setDownloadStatus(currentStatus);
            }

        }

    }

    /**
     * Update download percentage on the progress bar
     * @param progress - percentage completed so far
     */
    private void updateDownloadNotification(int progress){
        Long time_now = Calendar.getInstance().getTimeInMillis();
         int max_progress_status=100;
        if(((time_now - TIME_PASSED_FOR_PROGRESS_UPDATE) < WAITING_TIME_TO_UPDATE) || (progress < 0 && progress > max_progress_status)) {
            return;
        }
        TIME_PASSED_FOR_PROGRESS_UPDATE = time_now;
        mBuilder.setProgress(100,Math.abs(progress), false);
        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
        currentStatus=DOWNLOAD_STATUS_RUNNING;
        setDownloadStatus(currentStatus);
    }

    /**
     * Setting up the notification
     */
    private void showDownloadNotification(){
        mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle(getFeed().entries[currentEntryIndex].title)
                .setContentText("Downloading...")
                .setSmallIcon(R.drawable.launcher_icon);
        mBuilder.setProgress(100, 0, false);
        mBuilder.setOngoing(true);
        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    /**
     * Dismiss the notification once the download has finished.
     */
    private void dismissDownloadNotification(){
        mBuilder.setContentText("Download complete");
        mBuilder.setProgress(0, 0, false);
        mBuilder.setOngoing(false);
        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
        mNotifyManager.cancel(NOTIFICATION_ID);
    }

    /**
     * Get all mapped feed status given the download ID
     * which will be retrieved using entry ID supplied
     * @return
     */
    public Map<Long,Map<String,int[]>> getFeedDownloadStatus(){
        return this.feedDownloadStatus;
    }

    /**
     * Retried download ID given entry ID so that can be used to
     * retrieve download status of the specific entry
     * @return
     */
    public Map<String,Long> getEntryDownloadLog(){
        return DownloadManagerAndroid.this.entryDownloadLog;
    }


    /**
     * Update download status as the task is running
     */
    private void updateDownloadStatus(){
        String fileId=getFeed().entries[currentEntryIndex].id;
        Map<String,int[]> entriesMap=new HashMap<>();
        entriesMap.put(fileId,getDownloadStatus());
        this.feedDownloadStatus.put(getDownloadID(),entriesMap);
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
