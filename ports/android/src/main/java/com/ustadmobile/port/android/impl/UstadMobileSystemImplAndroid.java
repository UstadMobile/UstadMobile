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
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;

import com.ustadmobile.core.impl.*;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.port.android.impl.http.HTTPService;
import com.ustadmobile.port.android.impl.zip.ZipFileHandleAndroid;
import com.ustadmobile.port.android.view.AppViewAndroid;

import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import org.xmlpull.v1.*;


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

    public static final String EXTRA_VIEWID = "VIEWID";

    private AppViewAndroid appView;

    private UMLogAndroid logger;

    private HTTPService httpService;

    private HashMap<Activity, HTTPServiceConnection> activityHTTPServiceConnections;

    public UstadMobileSystemImplAndroid() {
        appView = new AppViewAndroid(this);
        logger = new UMLogAndroid();
        activityHTTPServiceConnections = new HashMap<>();
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

    public void handleActivityStart(Activity activity) {
        this.currentActivity = activity;
        setCurrentContext(activity);

        //bind the activity to the HTTP service
        Intent httpServiceIntent = new Intent(activity, HTTPService.class);
        HTTPServiceConnection activityCon = activityHTTPServiceConnections.get(activity);
        if(activityCon == null) {
            activityCon = new HTTPServiceConnection();
            activity.bindService(httpServiceIntent, activityCon, Context.BIND_AUTO_CREATE);
            activityHTTPServiceConnections.put(activity, activityCon);
        }
    }

    public void handleActivityStop(Activity activity) {
        int x =42 ;
        int y = x + 1;
    }

    public void handleActivityDestroy(Activity activity) {
        HTTPServiceConnection activityCon = activityHTTPServiceConnections.get(activity);
        if(activityCon != null) {
            activity.unbindService(activityCon);
            activityHTTPServiceConnections.remove(activity);
        }
    }

    public void setCurrentContext(Context context) {
        if(this.currentContext != context) {
            this.currentContext = context;
            SharedPreferences appPrefs = getAppSharedPreferences();
            currentUsername = appPrefs.getString(KEY_CURRENTUSER, null);
            currentAuth = appPrefs.getString(KEY_CURRENTAUTH, null);
            this.userPreferences = null;//change of context: force this to get reloaded when requested
        }
    }

    /**
     * Return the current Android context
     * @return
     */
    public Context getCurrentContext() {
        return this.currentContext;
    }

    /**
     * Return the current Android activity (may equal currentcontext)
     *
     */
    public Activity getCurrentActivity() {
        return this.currentActivity;
    }

    /**
     * The implementation of the MVC pattern in Android generally means instantiating a view object
     * and then starting an activity with an intent that contains an ID that can be used by the
     * activity to find the view object that started it.
     *
     * This will start an activity, with the parameter EXTRA_VIEWID set to the given viewId
     *
     * @param activityClass The Class object of the Activity to start
     * @param viewId An integer ID that activity expects so it can find it's view object after being created
     */
    public void startActivityForViewId(Class activityClass, int viewId) {
        Intent startIntent = new Intent(getCurrentContext(), activityClass);
        startIntent.putExtra(EXTRA_VIEWID, viewId);
        getCurrentContext().startActivity(startIntent);
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
            "ustadmobileContent/user-" + username);
        return userDir.getAbsolutePath();
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
    public boolean removeFile(String fileURI)  {
        File f = new File(fileURI);
        return f.delete();
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

    @Override
    public InputStream getFileInputStreamFromZip(String zipURI, String filename) {
        return null;
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
        super.setActiveUser(username);
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
    public String getUserPref(String key) {
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

        if(headers != null) {
            Enumeration e = headers.keys();
            while(e.hasMoreElements()) {
                String headerField = e.nextElement().toString();
                String headerValue = headers.get(headerField).toString();
                conn.setRequestProperty(headerField, headerValue);
            }
        }

        conn.setRequestMethod(method);

        if("POST".equals(method) && postParams != null && postParams.size() > 0) {
            //we need to write the post params to the request
            StringBuilder sb = new StringBuilder();
            Enumeration e = postParams.keys();
            boolean firstParam = true;
            while(e.hasMoreElements()) {
                String key = e.nextElement().toString();
                String value = postParams.get(key).toString();
                if(firstParam) {
                    firstParam = false;
                }else {
                    sb.append('&');
                }
                sb.append(URLEncoder.encode(key, "UTF-8")).append('=');
                sb.append(URLEncoder.encode(value, "UTF-8"));
            }

            conn.setDoOutput(true);
            OutputStream out = conn.getOutputStream();
            out.write(sb.toString().getBytes());
            out.flush();
            out.close();
        }

        conn.connect();

        int contentLen = conn.getContentLength();
        int statusCode = conn.getResponseCode();
        InputStream in = conn.getInputStream();
        byte[] buf = new byte[1024];
        int bytesRead = 0;
        int bytesReadTotal = 0;

        //do not read more bytes than is available in the stream
        int bytesToRead = Math.min(buf.length, contentLen != -1 ? contentLen : buf.length);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        if(!method.equalsIgnoreCase("HEAD")) {
            while((contentLen != -1 ? (bytesRead < contentLen) : true)  && (bytesRead = in.read(buf, 0, contentLen == -1 ? buf.length : Math.min(buf.length, contentLen - bytesRead))) != -1) {
                bout.write(buf, 0, bytesRead);
            }
        }

        in.close();

        Hashtable responseHeaders = new Hashtable();
        Iterator<String> headerIterator = conn.getHeaderFields().keySet().iterator();
        while(headerIterator.hasNext()) {
            String header = headerIterator.next();
            if(header == null) {
                continue;//a null header is the response line not header; leave that alone...
            }

            String headerVal = conn.getHeaderField(header);
            responseHeaders.put(header.toLowerCase(), headerVal);
        }

        byte[] resultBytes = bout.toByteArray();
        HTTPResult result = new HTTPResult(resultBytes, statusCode,
                responseHeaders);
        String resultStr = new String(resultBytes, "UTF-8");

        return result;
    }


    public XmlPullParser newPullParser() throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        return parser;
    }

    @Override
    public AppView getAppView() {
        return appView;
    }

    @Override
    public UMLog getLogger() {
        return logger;
    }

    @Override
    public String openContainer(UstadJSOPDSEntry entry, String containerURI, String mimeType) {
        String openPath = httpService.mountZIP(containerURI);
        String extension = UMFileUtil.getExtension(containerURI);
        if(extension != null && extension.endsWith("epub")) {
            String zipName = UMFileUtil.getFilename(containerURI);
            httpService.addFilter(zipName, "xhtml", "autoplay(\\s?)=(\\s?)([\"'])autoplay",
                "data-autoplay$1=$2$3autoplay");
            httpService.addFilter(zipName, "xhtml", "&(\\s)", "&amp;$1");
        }

        return openPath;
    }

    @Override
    public void closeContainer(String openURI) {
        httpService.ummountZIP(openURI);
    }

    /**
     * @inheritDoc
     */
    @Override
    public ZipFileHandle openZip(String name) throws IOException{
        return new ZipFileHandleAndroid(name);
    }

    public class HTTPServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            HTTPService.HTTPBinder httpBinder = (HTTPService.HTTPBinder)service;
            UstadMobileSystemImplAndroid.this.httpService = httpBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
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

        /**
         * When a request for the total size is made before the job starts; use an HTTP HEAD
         * request to get the total size if available.  This variable is used to store the result.
         */
        private int cachedTotalSize;

        public DownloadJob(String srcURL, String destFileURI, UstadMobileSystemImplAndroid hostImpl) {
            this.hostImpl = hostImpl;
            this.srcURL = srcURL;
            this.destFileURI = destFileURI;

            this.progressListeners = new LinkedList<UMProgressListener>();
            this.finished = false;
            cachedTotalSize = -1;

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

        /**
         * Fire the progress event to all registered listeners
         *
         * @param evtType UMProgressEvent.TYPE_COMPLETE or UMProgressEvent.TYPE_PROGRESS
         * @param status HTTP status code if appropriate
         */
        private void fireProgressEvent(int evtType, int status) {
            int[] downloadStatus = getProgressAndTotal();
            UMProgressEvent evt = new UMProgressEvent(this, evtType,
                    downloadStatus[0], downloadStatus[1], status);
            for(int i = 0; i < progressListeners.size(); i++) {
                progressListeners.get(i).progressUpdated(evt);
            }
        }


        /**
         * Fire a progress event as TYPE_PROGRESS with the current progress to all registered listeners
         */
        private void fireProgressEvent() {
            fireProgressEvent(UMProgressEvent.TYPE_PROGRESS, 0);
        }

        private void fireDownloadComplete() {
            fireProgressEvent(UMProgressEvent.TYPE_COMPLETE, 200);
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
        public void addProgressListener(UMProgressListener umProgressListener) {
            this.progressListeners.add(umProgressListener);
        }

        @Override
        public int getBytesDownloadedCount() {
            return getProgressAndTotal()[0];
        }

        @Override
        public int getTotalSize() {
            int totalSize = -1;

            if(cachedTotalSize != -1) {
                return cachedTotalSize;
            }

            // if we are trying to get the total size before an exception will be thrown here
            try {
                cachedTotalSize  =getProgressAndTotal()[1];
                totalSize = cachedTotalSize;
            }catch(Exception e) {}

            if(totalSize == -1) {
                Hashtable headersToSend = new Hashtable();
                try {
                    HTTPResult result = hostImpl.makeRequest(this.srcURL, headersToSend, null,
                        "HEAD");
                    String contentLengthStr = result.getHeaderValue("content-length");
                    if(contentLengthStr != null) {
                        totalSize = Integer.parseInt(contentLengthStr);
                        this.cachedTotalSize = totalSize;
                    }
                }catch(IOException e) {
                    e.printStackTrace();
                    //do nothing; just means we don't know the size of this job for the moment
                }

            }


            return totalSize;
        }

        @Override
        public boolean isFinished() {
            return finished;
        }

        @Override
        public String getSource() {
            return srcURL;
        }

        @Override
        public String getDestination() {
            return destFileURI;
        }
    }

}
