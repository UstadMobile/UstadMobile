package com.ustadmobile.port.android.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.port.sharedse.p2p.P2PNode;
import com.ustadmobile.port.sharedse.p2p.P2PTask;

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

import edu.rit.se.wifibuddy.DnsSdTxtRecord;
import edu.rit.se.wifibuddy.WifiDirectHandler;

import static com.ustadmobile.port.android.p2p.P2PManagerAndroid.CURRENT_NETWORK_NETID;
import static com.ustadmobile.port.android.p2p.P2PManagerAndroid.CURRENT_NETWORK_SSID;
import static com.ustadmobile.port.sharedse.p2p.P2PManagerSharedSE.CURRENT_NETWORK_EMPTY_STATE;


/**
 * Created by kileha3 on 07/03/2017.
 */

public class P2PDownloadTaskAndroid extends P2PTask {

    private P2PManagerAndroid p2pManager;

    private File currentDownloadFileInfo=null;


    private static int currentStatus=-1;
    public static final String ACTION_DOWNLOAD_COMPLETE ="com.ustadmobile.port.android.p2p.DOWNLOAD_COMPLETE";
    public static final String EXTRA_DOWNLOAD_ID ="extra_download_id";
    public static final String EXTRA_DOWNLOAD_STATUS ="extra_download_status";
    public static final String EXTRA_DOWNLOAD_SOURCE ="extra_download_source";
    public static final int DOWNLOAD_SOURCE_CLOUD =1;
    public static final int DOWNLOAD_SOURCE_P2P =2;
    public static final int DOWNLOAD_SOURCE_LOCAL_NETWORK =3;


    public static final String P2P_SUPERNODE_SERVER_ADDRESS = "http://192.168.49.1:8001/";
    public static final String HEADER_ETAG="Etag";
    public static final String HEADER_RANGE="Range";
    public static final String HEADER_LAST_MODIFIED ="Last-Modified";
    public static final String INFO_FILE_EXTENSION=".dlinfo";
    public static final String UNFINISHED_FILE_EXTENSION=".part";
    private static final String DOWNLOAD_REQUEST_METHOD="GET";

    private boolean isFileResuming=false;
    private int currentDownloadSource =-1;
    private DownLoadTask downloadTask;


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);

            boolean result = intent.getBooleanExtra(WifiDirectHandler.EXTRA_NOPROMPT_NETWORK_SUCCEEDED,
                    false);
            if(!result) {
                currentStatus=DOWNLOAD_STATUS_FAILED;
                setDownloadStatus(currentStatus);
                P2PDownloadTaskAndroid.this.fireTaskEnded();
            }else {
                P2PDownloadTaskAndroid.this.downloadInBackground();
            }


        }
    };

    /**
     * Task to download file as per request sent.
     * @param node
     * @param downloadUri
     * @param p2pManager
     */
    public P2PDownloadTaskAndroid(P2PNode node, String downloadUri, P2PManagerAndroid p2pManager) {
        super(node, downloadUri);
        this.p2pManager = p2pManager;
    }

    @Override
    public void start() {
        P2PNode node = getNode();
        if(node != null && !node.getNetworkSSID().equals(CURRENT_NETWORK_EMPTY_STATE) && p2pManager!=null) {
            currentDownloadSource=DOWNLOAD_SOURCE_P2P;
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiDirectHandler.Action.NOPROMPT_NETWORK_CONNECTIVITY_ACTION);
            LocalBroadcastManager.getInstance(p2pManager.getP2pService()).registerReceiver(mBroadcastReceiver,
                    filter);
            WifiDirectHandler wifiDirectHandler = p2pManager.getP2pService().getWifiDirectHandlerAPI();
            DnsSdTxtRecord txtRecord = wifiDirectHandler.getDnsSdTxtRecordMap().get(getNode().getNodeMacAddress());
            wifiDirectHandler.connectToNoPromptService(txtRecord);

        }else {
            if(node!=null && node.getNodePortNumber()!=0 && node.getNodeIPAddress()!=null){
                currentDownloadSource=DOWNLOAD_SOURCE_LOCAL_NETWORK;
            }else{
                currentDownloadSource=DOWNLOAD_SOURCE_CLOUD;
            }
            downloadInBackground();
        }
    }

    /**
     * Disconnect after using the service, if there are more
     * tasks from the same Node, the connection won't be terminated
     * @param currentConnectedNetwork
     */
    @Override
    public void disconnect(String [] currentConnectedNetwork) {
        currentConnectedNetwork[CURRENT_NETWORK_SSID]=currentConnectedNetwork[CURRENT_NETWORK_SSID].replaceAll("\\s+","\"");
        if(currentConnectedNetwork[CURRENT_NETWORK_SSID]!=null &&
                getNode().getNetworkSSID().replaceAll("\\s+","\"").equals(currentConnectedNetwork[CURRENT_NETWORK_SSID])){
            p2pManager.setCurrentConnectedNetwork(currentConnectedNetwork);
        }else{
            p2pManager.setCurrentConnectedNetwork(currentConnectedNetwork);
            WifiManager wifiManager = (WifiManager)p2pManager.getP2pService().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiManager.enableNetwork(Integer.parseInt(currentConnectedNetwork[CURRENT_NETWORK_NETID]),true);
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
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            currentStatus=DOWNLOAD_STATUS_QUEUED;
            setDownloadStatus(currentStatus);
            setDownloadTotalBytes(0);
            setDownloadTotalBytes(0);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            InputStream in = null;
            HttpURLConnection con = null;
            OutputStream out = null;
            File file = null;
            String fileInfoEtag,fileInfoLastModified,downloadUri;
            int fileLength=0;
            int bytesDownloadedSoFar;
            try {

                File infoFile=new File(P2PDownloadTaskAndroid.this.getDestinationPath()+INFO_FILE_EXTENSION);
                boolean isFileExists=infoFile.exists();
                currentStatus=DOWNLOAD_STATUS_RUNNING;
                setDownloadStatus(currentStatus);

                if(currentDownloadSource==DOWNLOAD_SOURCE_CLOUD || currentDownloadSource==DOWNLOAD_SOURCE_LOCAL_NETWORK){
                    downloadUri= getDownloadUri();

                }else{
                    downloadUri= UMFileUtil.joinPaths(new String[]{P2P_SUPERNODE_SERVER_ADDRESS,
                            getDownloadUri()});
                }

                URL url = new URL(downloadUri.replaceAll("\\s+","%20"));

                if(isFileExists && getTaskType()==P2PTask.TYPE_COURSE){
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod(DOWNLOAD_REQUEST_METHOD);
                    con.connect();
                    fileInfoEtag=con.getHeaderField(HEADER_ETAG);
                    if(fileInfoEtag!=null){
                        con.disconnect();
                    }

                    con = (HttpURLConnection) url.openConnection();
                    JSONObject infoFromFile= getFileContentFromFile(infoFile);

                    long existingFileLength=new File(P2PDownloadTaskAndroid.this.getDestinationPath()
                            +UNFINISHED_FILE_EXTENSION).length();
                    if(infoFromFile!=null && infoFromFile.get(HEADER_ETAG).equals(fileInfoEtag)){
                        isFileResuming=true;
                        con.setRequestProperty(HEADER_RANGE,"bytes="+String.valueOf(existingFileLength)+"-");
                    }
                }else{
                    con = (HttpURLConnection) url.openConnection();
                }
                con.setRequestMethod(DOWNLOAD_REQUEST_METHOD);
                con.connect();
                fileLength = con.getContentLength();
                setDownloadTotalBytes(fileLength);

                in = con.getInputStream();
                P2PDownloadTaskAndroid.this.setDestinationPath(P2PDownloadTaskAndroid.this.getDestinationPath()
                        +UNFINISHED_FILE_EXTENSION);
                file = new File(P2PDownloadTaskAndroid.this.getDestinationPath());

                out = new FileOutputStream(file,true);
                byte[] buffer = new byte[1024];
                int total=0;
                fileInfoEtag=con.getHeaderField(HEADER_ETAG);
                fileInfoLastModified=con.getHeaderField(HEADER_LAST_MODIFIED);

                //check for file ETag or Last-modified date and log them to the temp file info
                if((fileInfoEtag!=null || fileInfoLastModified!=null) && !isFileResuming){
                    currentDownloadFileInfo= saveFileInfoToFile(fileInfoEtag,fileInfoLastModified);
                }else if(fileInfoEtag!=null && isFileResuming){
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

            if(isFileResuming){
                file=saveFileWithNewExtension(P2PDownloadTaskAndroid.this.getDestinationPath());
                return file!=null && file.exists();
            }else{
                return file != null && file.exists() && file.length()==fileLength;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.d(WifiDirectHandler.TAG,":downloading ="+values[0]);
            setBytesDownloadedSoFar(values[0]);
        }

        @Override
        protected void onCancelled() {
            currentStatus=DOWNLOAD_STATUS_CANCELLED;
            P2PDownloadTaskAndroid.this.fireTaskEnded();
        }

        @Override
        protected void onPostExecute(Boolean isFileAvailable) {

            if(isFileAvailable && currentDownloadFileInfo!=null){
                currentDownloadFileInfo.delete();
                if(new File(P2PDownloadTaskAndroid.this.getDestinationPath())
                        .getAbsolutePath().endsWith(UNFINISHED_FILE_EXTENSION)){

                    File newFile=saveFileWithNewExtension(P2PDownloadTaskAndroid.this.getDestinationPath());

                    if(isFileResuming){
                        newFile=new File(P2PDownloadTaskAndroid.this.getDestinationPath());
                    }
                    if(newFile!=null){
                        currentStatus=DOWNLOAD_STATUS_COMPLETED;
                        setDownloadStatus(currentStatus);
                        if(getTestMode()){
                            fireTaskEnded();
                        }else{
                            sendBroadCast();
                        }
                        setDestinationPath(newFile.getAbsolutePath());
                    }
                }else{
                    Log.d(WifiDirectHandler.TAG,"File Extension doesn't match: status - partial download");
                }
            }else{
                /*Partial Download: download task was incomplete, possibly it was interrupted or cancelled*/

                currentStatus=DOWNLOAD_STATUS_FAILED;
                setDownloadStatus(currentStatus);
            }

            P2PDownloadTaskAndroid.this.fireTaskEnded();
        }
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
     * @param fileUri
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
     * Broadcast download completion completion event
     */
    private void sendBroadCast(){
        Intent intent=new Intent();
        intent.setAction(ACTION_DOWNLOAD_COMPLETE);
        intent.putExtra(EXTRA_DOWNLOAD_ID,String.valueOf(getDownloadRequestID()));
        intent.putExtra(EXTRA_DOWNLOAD_STATUS,String.valueOf(DOWNLOAD_STATUS_COMPLETED));
        intent.putExtra(EXTRA_DOWNLOAD_SOURCE,String.valueOf(currentDownloadSource));
        p2pManager.getP2pService().sendBroadcast(intent);

    }

    /**
     * Create a temporally file to store file info.
     * @param fileInfoEtag - Header File Etag
     * @param fileInfoLastModified Header File last-modified
     * @return
     */
    private File saveFileInfoToFile(String fileInfoEtag, String fileInfoLastModified){
        File tempFileInfo=null;
        try{
            String fileDestination=P2PDownloadTaskAndroid.this.getDestinationPath();
            tempFileInfo=new File(fileDestination.substring(0,fileDestination.length()-UNFINISHED_FILE_EXTENSION.length())+INFO_FILE_EXTENSION);
            FileWriter writer = new FileWriter(tempFileInfo.getAbsolutePath());
            JSONObject fileObject=new JSONObject();
            fileObject.put(HEADER_ETAG,fileInfoEtag);
            fileObject.put(HEADER_LAST_MODIFIED,fileInfoLastModified);
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
