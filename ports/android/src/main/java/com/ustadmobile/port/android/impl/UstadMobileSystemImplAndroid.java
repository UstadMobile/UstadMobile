/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */

package com.ustadmobile.port.android.impl;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.ustadmobile.core.impl.*;

import android.os.Build;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Created by mike on 07/06/15.
 */
public class UstadMobileSystemImplAndroid extends UstadMobileSystemImpl{

    private Activity currentActivity;

    private Context currentContext;

    public static final String TAG = "UstadMobileImplAndroid";

    public static final String PREFS_NAME = "ustadmobilePreferences";

    public static final String APP_PREFERENCES_NAME = "UMAPP-PREFERENCES";

    public static final String USER_PREFERENCES_NAME  = "user-";

    public static final String KEY_CURRENTUSER = "app-currentuser";

    public static final String KEY_CURRENTAUTH = "app-currentauth";

    private String currentUsername;

    private String currentAuth;

    private SharedPreferences appPreferences;

    private SharedPreferences userPreferences;

    private SharedPreferences.Editor userPreferencesEditor;


    public UstadMobileSystemImplAndroid() {

    }

    public static UstadMobileSystemImplAndroid getInstanceAndroid() {
        return (UstadMobileSystemImplAndroid) mainInstance;
    }

    public void init() {
        File sharedContentDir = new File(getSharedContentDir());
        if(!sharedContentDir.isDirectory()) {
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
        if(this.currentContext != context) {
            this.currentContext = context;
            SharedPreferences appPrefs = getAppSharedPreferences();
            currentUsername = appPrefs.getString(KEY_CURRENTUSER, null);
            this.userPreferences = null;//change of context: force this to get reloaded when requested
        }
    }

    public Context getCurrentContext() {
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
    public boolean renameFile(String path1, String path2) {
        File file1 = new File(path1);
        File file2 = new File(path2);
        return file1.renameTo(file2);
    }

    @Override
    public long fileSize(String path) {
        File file = new File(path);
        return file.length();
    }

    @Override
    public boolean makeDirectory(String dirPath) throws IOException {
        File newDir = new File(dirPath);
        return newDir.mkdirs();
    }

    @Override
    public boolean removeRecursively(String path) {
        return removeRecursively(new File(path));
    }

    public boolean removeRecursively(File f) {
        if(f.isDirectory()) {
            File[] dirContents = f.listFiles();
            for(int i = 0; i < dirContents.length; i++) {
                if(dirContents[i].isDirectory()) {
                    removeRecursively(dirContents[i]);
                }
                dirContents[i].delete();
            }
        }
        return f.delete();
    }

    @Override
    public UMTransferJob unzipFile(String s, String s1) {
        return null;
    }

    private SharedPreferences getAppSharedPreferences() {
        if(appPreferences == null) {
            appPreferences = currentContext.getSharedPreferences(APP_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        }
        return appPreferences;
    }

    private SharedPreferences getUserPreferences() {
        if(currentUsername != null) {
            if(userPreferences == null) {
                userPreferences = currentContext.getSharedPreferences(USER_PREFERENCES_NAME +
                        currentUsername, Context.MODE_PRIVATE);
                Log.d(TAG, "Opening preferences for user: " + currentUsername);
            }
            return userPreferences;
        }else {
            return null;
        }
    }

    @Override
    public void setActiveUser(String username) {
        saveUserPrefs();
        SharedPreferences appPreferences = getAppSharedPreferences();
        SharedPreferences.Editor editor = appPreferences.edit();
        if(username != null) {
            editor.putString(KEY_CURRENTUSER, username);
        }else {
            editor.remove(KEY_CURRENTUSER);
        }
        editor.commit();

        this.currentUsername = username;
        this.userPreferences = null;
    }

    @Override
    public String getActiveUser() {
        return currentUsername;
    }

    @Override
    public void setActiveUserAuth(String auth) {
        setAppPref(KEY_CURRENTAUTH, auth);
        this.currentAuth = auth;
    }

    @Override
    public String getActiveUserAuth() {
        return this.currentAuth;
    }

    @Override
    public void setUserPref(String key, String value) {
        if(userPreferencesEditor == null) {
            userPreferencesEditor = getUserPreferences().edit();
        }
        if(value != null) {
            userPreferencesEditor.putString(key, value);
        }else {
            userPreferencesEditor.remove(key);
        }

        userPreferencesEditor.commit();
    }

    @Override
    public String getUserPref(String key, String s1) {
        return getUserPreferences().getString(key, null);
    }

    @Override
    public String[] getPrefKeyList() {
        return new String[0];
    }

    @Override
    public void saveUserPrefs() {
        if(userPreferencesEditor != null) {
            userPreferencesEditor.commit();
            userPreferencesEditor = null;
        }
    }

    @Override
    public String getAppPref(String key) {
        return getAppSharedPreferences().getString(key, null);
    }

    public void setAppPref(String key, String value) {
        SharedPreferences prefs = getAppSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        if(value != null) {
            editor.putString(key, value);
        }else {
            editor.remove(key);
        }
        editor.commit();
    }

    @Override
    public HTTPResult makeRequest(String httpURL, Hashtable headers, Hashtable postParams, String method) throws IOException {
        URL url = new URL(httpURL);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();

        Enumeration e = headers.keys();
        while(e.hasMoreElements()) {
            String headerField = e.nextElement().toString();
            String headerValue = headers.get(headerField).toString();
            conn.setRequestProperty(headerField, headerValue);
        }

        conn.setRequestMethod(method);

        conn.connect();

        int contentLen = conn.getContentLength();
        InputStream in = conn.getInputStream();
        byte[] buf = new byte[1024];
        int bytesRead = 0;
        int bytesReadTotal = 0;

        //do not read more bytes than is available in the stream
        int bytesToRead = Math.min(buf.length, contentLen != -1 ? contentLen : buf.length);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        while((contentLen != -1 ? (bytesRead < contentLen) : true)  && (bytesRead = in.read(buf, 0, contentLen == -1 ? buf.length : Math.min(buf.length, contentLen - bytesRead))) != -1) {
            bout.write(buf, 0, bytesRead);
        }
        in.close();

        byte[] resultBytes = bout.toByteArray();
        HTTPResult result = new HTTPResult(resultBytes, conn.getResponseCode(),
                new Hashtable());
        String resultStr = new String(resultBytes, "UTF-8");

        return result;
    }


    public XmlPullParser newPullParser() throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        return parser;
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

        public static final int IDX_DOWNLOADED_SO_FAR = 0;

        public static final int IDX_BYTES_TOTAL = 1;

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

        /**
         * In Android2.3 this is not available after completion: cache with object
         */
        private int finishedTotalSize;

        /**
         * In Android2.3 this is not available after completion: cache with object
         */
        private int finishedBytesDownloaded;

        public DownloadJob(String srcURL, String destFileURI, UstadMobileSystemImplAndroid hostImpl) {
            this.hostImpl = hostImpl;
            this.srcURL = srcURL;
            this.destFileURI = destFileURI;

            this.progressListeners = new LinkedList<UMProgressListener>();
            this.finished = false;
        }

        @Override
        public void start() {
            /* TODO: In Android 2.3 if the destination file already exists: it must be removed or
            *  we must use a temporary dir
            *  */
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
                        Log.d(TAG, "Download Complete");
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
            Log.d(TAG, "Download Complete");
            if(timerProgressUpdate != null) {
                timerProgressUpdate.cancel();
            }

            final int[] byteTotals = this.getProgressAndTotal();
            this.finished = true;
            this.finishedBytesDownloaded = byteTotals[IDX_DOWNLOADED_SO_FAR];
            this.finishedTotalSize = byteTotals[IDX_BYTES_TOTAL];
            Log.d(TAG, "Download Size: " + finishedBytesDownloaded + " / " + finishedTotalSize);


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
            int[] retVal = new int[2];
            if(this.isFinished()) {
                retVal[IDX_DOWNLOADED_SO_FAR] = this.finishedBytesDownloaded;
                retVal[IDX_BYTES_TOTAL] = this.finishedTotalSize;
            }else {
                DownloadManager mgr = (DownloadManager)hostImpl.getCurrentContext().getSystemService(
                        Context.DOWNLOAD_SERVICE);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(this.downloadID);
                Cursor cursor = mgr.query(query);
                cursor.moveToFirst();

                retVal[IDX_DOWNLOADED_SO_FAR] = cursor.getInt(cursor.getColumnIndex(
                        DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                retVal[IDX_BYTES_TOTAL] = cursor.getInt(cursor.getColumnIndex(
                        DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                int statusCode = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            }

            return retVal;
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
