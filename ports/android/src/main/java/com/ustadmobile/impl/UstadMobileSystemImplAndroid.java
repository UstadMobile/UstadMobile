package com.ustadmobile.impl;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Build;
import android.util.Log;

/**
 * Created by mike on 07/06/15.
 */
public class UstadMobileSystemImplAndroid extends com.ustadmobile.impl.UstadMobileSystemImpl{

    private Activity currentActivity;

    private Context currentContext;

    public static final String TAG = "UstadMobileImplAndroid";

    public static final String PREFS_NAME = "ustadmobilePreferences";


    public UstadMobileSystemImplAndroid() {

    }

    public void init() {
        File sharedContentDir = new File(getSharedContentDir());
        if(!sharedContentDir.exists() && sharedContentDir.isDirectory()) {
            sharedContentDir.mkdirs();
        }
    }

    @Override
    public String getImplementationName() {
        return null;
    }

    public void setCurrentActivity(Activity activity) {

    }

    public void setCurrentContext(Context context) {
        this.currentContext = context;
    }

    protected Context getCurrentContext() {
        return this.currentContext;
    }

    @Override
    public String getSharedContentDir() {
        File extStorage = Environment.getExternalStorageDirectory();
        File ustadContentDir = new File(extStorage, "ustadmobileContent");
        return ustadContentDir.getAbsolutePath();
    }

    @Override
    public String getUserContentDirectory(String username) {
        File userDir = new File(Environment.getExternalStorageDirectory(),
            "ustadmobileContent/users/" + username);
        return null;
    }

    /**
     * Will return language_COUNTRY e.g. en_US
     *
     * @return
     */
    @Override
    public String getSystemLocale() {
        return Locale.getDefault().toString();
    }

    @Override
    public Hashtable getSystemInfo() {
        Hashtable ht = new Hashtable();
        ht.put("os", "Android");
        ht.put("osversion", Build.VERSION.RELEASE);
        ht.put("locale", this.getSystemLocale());

        return ht;
    }

    @Override
    public String readFileAsText(String filename, String encoding) throws IOException {
        IOException ioe = null;
        FileInputStream fin = null;
        ByteArrayOutputStream bout = null;

        try {
            fin = new FileInputStream(filename);
            bout = new ByteArrayOutputStream();
            int bytesRead = -1;
            byte[] buf = new byte[1024];
            while((bytesRead = fin.read(buf, 0, buf.length)) != -1) {
                bout.write(buf, 0, bytesRead);
            }
        }catch(IOException e) {
            ioe = e;
        }finally {
            if(fin != null) {
                fin.close();
            }
        }

        if(ioe == null) {
            String retVal = new String(bout.toByteArray(), encoding);
            return retVal;
        }else {
            throw ioe;
        }
    }

    @Override
    public long modTimeDifference(String fileURI1, String fileURI2) {
        return (new File(fileURI2).lastModified() - new File(fileURI1).lastModified());
    }


    @Override
    public void writeStringToFile(String str, String fileURI, String encoding) throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(str.getBytes(encoding));
        FileOutputStream fout = null;
        IOException ioe = null;
        try {
            int bytesRead = -1;
            byte[] buf = new byte[1024];
            fout = new FileOutputStream(fileURI);
            while((bytesRead = bin.read(buf))!= -1) {
                fout.write(buf, 0, bytesRead);
            }
        }catch(IOException e) {
            ioe = e;
        }finally{
            if(fout != null) {
                fout.close();
            }
        }

        if(ioe != null) {
            throw ioe;
        }
    }

    @Override
    public boolean fileExists(String fileURI) throws IOException {
        return new File(fileURI).exists();
    }

    @Override
    public boolean dirExists(String dirURI) throws IOException {
        File dir = new File(dirURI);
        return dir.exists() && dir.isDirectory();
    }

    @Override
    public void removeFile(String fileURI) throws IOException {
        File f = new File(fileURI);
        f.delete();
    }

    @Override
    public String[] listDirectory(String dirURI) throws IOException {
        File dir = new File(dirURI);
        return dir.list();
    }

    @Override
    public UMTransferJob downloadURLToFile(String url, String fileURI, Hashtable headers) {
        DownloadJob job = new DownloadJob(url, fileURI, this);

        return job;
    }

    @Override
    public void renameFile(String s, String s1) {

    }

    @Override
    public int fileSize(String s) {
        return 0;
    }

    @Override
    public void makeDirectory(String s) throws IOException {

    }

    @Override
    public void removeRecursively(String s) {

    }

    @Override
    public UMTransferJob unzipFile(String s, String s1) {
        return null;
    }

    @Override
    public void setActiveUser(String s) {

    }

    @Override
    public String getActiveUser() {
        return null;
    }

    @Override
    public void setActiveUserAuth(String s) {

    }

    @Override
    public String getActiveUserAuth() {
        return null;
    }

    @Override
    public void setUserPref(String s, String s1) {

    }

    @Override
    public String getUserPref(String s, String s1) {
        return null;
    }

    @Override
    public String[] getPrefKeyList() {
        return new String[0];
    }

    @Override
    public void saveUserPrefs() {

    }

    @Override
    public String getAppPref(String s) {
        return null;
    }

    @Override
    public HTTPResult makeRequest(String s, Hashtable hashtable, Hashtable hashtable1, String s1) {
        return null;
    }

    /**
     * Represents a single Download: backed by the Android DownloadManager service
     *
     */
    public class DownloadJob implements UMTransferJob {


        private String srcURL;

        private UstadMobileSystemImplAndroid hostImpl;

        /**
         * The download id assigned by the DownloadManager service
         */
        private long downloadID = -1;

        /**
         * Timer object that runs to check on the download progress: Download Manager itself
         * does not provide progress events
         */
        private Timer timerProgressUpdate = null;

        /**
         * Where we are going to save the file
         */
        private String destFileURI;

        /**
         * The interval (in ms) used to check on download progress
         */
        public static final int DOWNLOAD_PROGRESS_UPDATE_TIMEOUT = 1000;

        /**
         * BroadCastReceiver that listens for the download complete message
         */
        private BroadcastReceiver downloadCompleteReceiver;

        /**
         * IntentFilter used to when registering for updates
         */
        private IntentFilter downloadCompleteIntentFilter;

        /**
         * UMProgressListener objects
         */
        private List<UMProgressListener> progressListeners;

        /**
         * Android Context to be used to lookup services etc.
         */
        private Context ctx;

        private boolean finished;

        public DownloadJob(String srcURL, String destFileURI, UstadMobileSystemImplAndroid hostImpl) {
            this.hostImpl = hostImpl;
            this.srcURL = srcURL;
            this.destFileURI = destFileURI;

            this.progressListeners = new LinkedList<UMProgressListener>();
            this.finished = false;
        }

        @Override
        public void start() {
            this.ctx = hostImpl.getCurrentContext();
            DownloadManager mgr = (DownloadManager)ctx.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(this.srcURL));

            File destFile = new File(destFileURI);
            String destStr = destFile.getAbsolutePath();
            request.setDestinationUri(Uri.fromFile(destFile));
            final DownloadJob thisJob = this;


            downloadCompleteIntentFilter =
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            downloadCompleteReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L) == thisJob.downloadID) {
                        thisJob.cleanup();
                    }
                }
            };
            ctx.registerReceiver(downloadCompleteReceiver, downloadCompleteIntentFilter);

            downloadID = mgr.enqueue(request);
            startProgressTracking(this);
        }

        private void fireDownloadComplete() {
            int[] downloadStatus = getProgressAndTotal();
            UMProgressEvent evt = new UMProgressEvent(UMProgressEvent.TYPE_COMPLETE, downloadStatus[0],
                    downloadStatus[1], 200);
            for(int i = 0; i < progressListeners.size(); i++) {
                progressListeners.get(i).progressUpdated(evt);
            }
        }

        private void cleanup() {
            //when everything is done...
            if(timerProgressUpdate != null) {
                timerProgressUpdate.cancel();
            }

            this.finished = true;

            /*
             * On Android 2.3 devices it seems after the download is complete the receiver is no
             * longer registered and this will throw an IllegalArgumentException
             */
            try {
                this.ctx.unregisterReceiver(downloadCompleteReceiver);
            }catch(IllegalArgumentException e) {
                Log.d(TAG, "Ignore illegal argument exception for receiver already unregistered");
            }



            try { notifyAll(); }
            catch(Exception e) {}

            fireDownloadComplete();
        }

        private void fireProgressEvent() {

        }

        private int[] getProgressAndTotal() {
            DownloadManager mgr = (DownloadManager)hostImpl.getCurrentContext().getSystemService(
                    Context.DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(this.downloadID);
            Cursor cursor = mgr.query(query);
            cursor.moveToFirst();

            int bytesDownloaded = cursor.getInt(cursor.getColumnIndex(
                    DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            int bytesTotal = cursor.getInt(cursor.getColumnIndex(
                DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            int statusCode = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));

            return new int[]{bytesDownloaded, bytesTotal};
        }

        public long getDownloadID() {
            return this.downloadID;
        }


        private void startProgressTracking(final DownloadJob job) {
            this.timerProgressUpdate = new Timer();
            timerProgressUpdate.schedule(new TimerTask() {
                @Override
                public void run() {
                    fireProgressEvent();
                }
            }, DOWNLOAD_PROGRESS_UPDATE_TIMEOUT, DOWNLOAD_PROGRESS_UPDATE_TIMEOUT);
        }

        @Override
        public void addProgresListener(UMProgressListener umProgressListener) {
            this.progressListeners.add(umProgressListener);
        }

        @Override
        public int getBytesDownloadedCount() {
            return getProgressAndTotal()[0];
        }

        @Override
        public int getTotalSize() {
            return getProgressAndTotal()[1];
        }

        @Override
        public boolean isFinished() {
            return finished;
        }
    }

}
