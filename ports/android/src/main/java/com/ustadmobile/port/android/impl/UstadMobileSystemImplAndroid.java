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
import android.os.Bundle;
import android.os.Environment;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;

import com.ustadmobile.core.U;
import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.impl.*;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.CatalogView;
import com.ustadmobile.core.view.ContainerView;
import com.ustadmobile.core.view.LoginView;
import com.ustadmobile.port.android.impl.http.HTTPService;
import com.ustadmobile.port.android.impl.zip.ZipFileHandleAndroid;
import com.ustadmobile.port.android.view.AppViewAndroid;
import com.ustadmobile.port.android.view.CatalogActivity;
import com.ustadmobile.port.android.view.ContainerActivity;
import com.ustadmobile.port.android.view.LoginActivity;

import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import org.xmlpull.v1.*;


/**
 * Created by mike on 07/06/15.
 */
public class UstadMobileSystemImplAndroid extends UstadMobileSystemImpl{

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

    private UMLogAndroid logger;

    private HTTPService httpService;

    private HashMap<Object, HTTPServiceConnection> activityHTTPServiceConnections;

    private HashMap<Object, HTTPService> activityToHttpServiceMap;

    public static final String START_USERNAME = "START_USERNAME";

    public static final String START_AUTH = "START_AUTH";

    private WeakHashMap<Activity, AppViewAndroid> appViews;

    private HashMap<UMDownloadCompleteReceiver, BroadcastReceiver> downloadCompleteReceivers;

    /**
     * When opencontainer is called we need to be sure that the http service is ready.  This is the
     * maximum amount of time (in ms) that we will wait for the http service to bind and be ready
     * to mount a container
     */
    public static final int HTTP_READY_TIMEOUT = 20*1000;

    /**
     * The amount of time to sleep in between checking for the http service to be ready
     */
    public static final int HTTP_CHECK_INTERVAL = 500;

    /**
     @deprecated
     */
    public UstadMobileSystemImplAndroid() {
        logger = new UMLogAndroid();
        activityHTTPServiceConnections = new HashMap<>();
        activityToHttpServiceMap = new HashMap<>();
        appViews = new WeakHashMap<>();
        downloadCompleteReceivers = new HashMap<>();
    }

    /**
     * @return
     */
    public static UstadMobileSystemImplAndroid getInstanceAndroid() {
        return (UstadMobileSystemImplAndroid) getInstance();
    }

    @Override
    public void init(Object context) {
        super.init(context);
    }

    @Override
    public boolean loadActiveUserInfo(Object context) {
        SharedPreferences appPrefs = getAppSharedPreferences((Context)context);
        currentUsername = appPrefs.getString(KEY_CURRENTUSER, null);
        currentAuth = appPrefs.getString(KEY_CURRENTAUTH, null);
        this.userPreferences = null;
        return true;
    }

    @Override
    public String getImplementationName() {
        return null;
    }

    /**
     * To be called by activities as the first matter of business in the onCreate method
     *
     * @param activity
     */
    public void handleActivityCreate(Activity activity, Bundle savedInstanceState) {
        init(activity);
    }

    public void handleActivityStart(Activity activity) {

    }

    protected void connectActivityToHttpService(Activity activity) {
        Intent httpServiceIntent = new Intent(activity, HTTPService.class);
        HTTPServiceConnection activityCon = activityHTTPServiceConnections.get(activity);
        if(activityCon == null) {
            activityCon = new HTTPServiceConnection(activity);
            activity.bindService(httpServiceIntent, activityCon, Context.BIND_AUTO_CREATE);
            activityHTTPServiceConnections.put(activity, activityCon);
        }
    }


    public void handleActivityStop(Activity activity) {

    }

    public void handleActivityDestroy(Activity activity) {
        HTTPServiceConnection activityCon = activityHTTPServiceConnections.get(activity);
        if(activityCon != null) {
            activity.unbindService(activityCon);
            activityHTTPServiceConnections.remove(activity);
        }
    }

    @Override
    public void go(Class cls, Hashtable args, Object context) {
        Class androidClass = null;
        if(cls.equals(LoginView.class)) {
            androidClass = LoginActivity.class;
        }else if(cls.equals(ContainerView.class)) {
            androidClass = ContainerActivity.class;
        }else if(cls.equals(CatalogView.class)) {
            androidClass = CatalogActivity.class;
        }

        Intent startIntent = new Intent((Context)context, androidClass);

        if(args != null) {
            Enumeration argE = args.keys();

            String currentKey;
            Object currentVal;
            while(argE.hasMoreElements()) {
                currentKey = (String)argE.nextElement();
                currentVal = args.get(currentKey);

                if(currentVal instanceof String) {
                    startIntent.putExtra(currentKey, (String)currentVal);
                }else if(currentVal instanceof Integer) {
                    startIntent.putExtra(currentKey, (Integer)currentVal);
                }
            }
        }


        ((Context)context).startActivity(startIntent);
    }

    private String getSystemBaseDir() {
        return new File(Environment.getExternalStorageDirectory(), "ustadmobileContent").getAbsolutePath();
    }

    @Override
    public String getCacheDir(int mode, Object context) {
        String systemBaseDir = getSystemBaseDir();
        if(mode == CatalogController.SHARED_RESOURCE) {
            return UMFileUtil.joinPaths(new String[]{systemBaseDir, UstadMobileConstants.CACHEDIR});
        }else {
            return UMFileUtil.joinPaths(new String[]{systemBaseDir, "user-" + getActiveUser(context),
                    UstadMobileConstants.CACHEDIR});
        }
    }

    @Override
    public UMStorageDir[] getStorageDirs(int mode, Object context) {
        List<UMStorageDir> dirList = new ArrayList<>();
        if((mode & CatalogController.SHARED_RESOURCE) == CatalogController.SHARED_RESOURCE) {
            dirList.add(new UMStorageDir(getSystemBaseDir(), getString(U.id.device), false, true, false));
        }

        if((mode & CatalogController.USER_RESOURCE) == CatalogController.USER_RESOURCE) {
            String userBase = UMFileUtil.joinPaths(new String[]{getSystemBaseDir(), "user-"
                    + getActiveUser(context)});
            dirList.add(new UMStorageDir(userBase, getString(U.id.device), false, true, true));
        }

        UMStorageDir[] retVal = new UMStorageDir[dirList.size()];
        dirList.toArray(retVal);
        return retVal;
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
    public String getSystemLocale(Object context) {
        return Locale.getDefault().toString();
    }

    @Override
    public Hashtable getSystemInfo() {
        Hashtable ht = new Hashtable();
        ht.put("os", "Android");
        ht.put("osversion", Build.VERSION.RELEASE);

        return ht;
    }

    @Override
    public long modTimeDifference(String fileURI1, String fileURI2) {
        return (new File(fileURI2).lastModified() - new File(fileURI1).lastModified());
    }

    @Override
    public OutputStream openFileOutputStream(String fileURI, int flags) throws IOException {
        fileURI = UMFileUtil.stripPrefixIfPresent("file://", fileURI);
        return new FileOutputStream(fileURI, (flags & FILE_APPEND) == FILE_APPEND);
    }

    @Override
    public InputStream openFileInputStream(String fileURI) throws IOException {
        fileURI = UMFileUtil.stripPrefixIfPresent("file://", fileURI);
        return new FileInputStream(fileURI);
    }

    @Override
    public InputStream openResourceInputStream(String resURI, Object context) throws IOException {
        return ((Context)context).getAssets().open(resURI);
    }

    @Override
    public boolean fileExists(String fileURI) throws IOException {
        fileURI = UMFileUtil.stripPrefixIfPresent("file://", fileURI);
        return new File(fileURI).exists();
    }

    @Override
    public boolean dirExists(String dirURI) throws IOException {
        dirURI = UMFileUtil.stripPrefixIfPresent("file://", dirURI);
        File dir = new File(dirURI);
        return dir.exists() && dir.isDirectory();
    }

    @Override
    public boolean removeFile(String fileURI)  {
        fileURI = UMFileUtil.stripPrefixIfPresent("file://", fileURI);
        File f = new File(fileURI);
        return f.delete();
    }

    @Override
    public String[] listDirectory(String dirURI) throws IOException {
        dirURI = UMFileUtil.stripPrefixIfPresent("file://", dirURI);
        File dir = new File(dirURI);
        return dir.list();
    }

    @Override
    public UMTransferJob downloadURLToFile(String url, String fileURI, Hashtable headers, Object context) {
        DownloadJob job = new DownloadJob(url, fileURI, this, (Context)context);

        return job;
    }

    @Override
    public long queueFileDownload(String url, String destFileURI, Hashtable headers, Object context) {
        Context aContext = (Context)context;
        DownloadManager mgr = (DownloadManager)aContext.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        File destFile = new File(destFileURI);
        String destStr = destFile.getAbsolutePath();
        request.setDestinationUri(Uri.fromFile(destFile));

        return mgr.enqueue(request);
    }

    @Override
    public int[] getFileDownloadStatus(long downloadID, Object context) {
        Context ctx = (Context)context;
        DownloadManager mgr = (DownloadManager)ctx.getSystemService(
                Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadID);

        Cursor cursor = mgr.query(query);
        cursor.moveToFirst();

        int[] retVal = new int[3];
        retVal[IDX_DOWNLOADED_SO_FAR] = cursor.getInt(cursor.getColumnIndex(
                DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
        retVal[IDX_BYTES_TOTAL] = cursor.getInt(cursor.getColumnIndex(
                DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
        retVal[IDX_STATUS] = cursor.getInt(cursor.getColumnIndex(
                DownloadManager.COLUMN_STATUS));
        return retVal;
    }

    @Override
    public void registerDownloadCompleteReceiver(final UMDownloadCompleteReceiver receiver, Object context) {
        IntentFilter downloadCompleteIntentFilter =
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        BroadcastReceiver completeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long downloadID =intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
                receiver.downloadStatusUpdated(new UMDownloadCompleteEvent(downloadID));
            }
        };

        downloadCompleteReceivers.put(receiver, completeReceiver);
        ((Context)context).registerReceiver(completeReceiver, downloadCompleteIntentFilter);
    }

    @Override
    public void unregisterDownloadCompleteReceiver(UMDownloadCompleteReceiver receiver, Object context) {
        ((Context)context).unregisterReceiver(downloadCompleteReceivers.get(receiver));
        downloadCompleteReceivers.remove(receiver);
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


    private SharedPreferences getAppSharedPreferences(Context context) {
        if(appPreferences == null) {
            appPreferences = context.getSharedPreferences(APP_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        }
        return appPreferences;
    }

    private SharedPreferences getUserPreferences(Context context) {
        if(currentUsername != null) {
            if(userPreferences == null) {
                userPreferences = context.getSharedPreferences(USER_PREFERENCES_NAME +
                        currentUsername, Context.MODE_PRIVATE);
                Log.d(TAG, "Opening preferences for user: " + currentUsername);
            }
            return userPreferences;
        }else {
            return null;
        }
    }

    @Override
    public void setActiveUser(String username, Object context) {
        this.currentUsername = username;

        super.setActiveUser(username, context);
        saveUserPrefs(context);
        SharedPreferences appPreferences = getAppSharedPreferences((Context)context);
        SharedPreferences.Editor editor = appPreferences.edit();
        if(username != null) {
            editor.putString(KEY_CURRENTUSER, username);
        }else {
            editor.remove(KEY_CURRENTUSER);
        }
        editor.commit();


        this.userPreferences = null;
    }

    @Override
    public String getActiveUser(Object context) {
        return currentUsername;
    }

    @Override
    public void setActiveUserAuth(String auth, Object context) {
        setAppPref(KEY_CURRENTAUTH, auth, context);
        this.currentAuth = auth;
    }

    @Override
    public String getActiveUserAuth(Object context) {
        return this.currentAuth;
    }

    @Override
    public void setUserPref(String key, String value, Object context) {
        if(userPreferencesEditor == null) {
            userPreferencesEditor = getUserPreferences((Context)context).edit();
        }
        if(value != null) {
            userPreferencesEditor.putString(key, value);
        }else {
            userPreferencesEditor.remove(key);
        }

        userPreferencesEditor.commit();
    }

    @Override
    public String getUserPref(String key, Object context) {
        return getUserPreferences((Context)context).getString(key, null);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAppPrefKeyList(Object context) {
        return getKeysFromSharedPreferences(getAppSharedPreferences((Context) context));
    }


    /**
     * @inheritDoc
     */
    @Override
    public String[] getUserPrefKeyList(Object context) {
        return getKeysFromSharedPreferences(getUserPreferences((Context) context));
    }

    /**
     * Private utility function to get a String array of keys from a SharedPreferences object
     * @param prefs
     * @return
     */
    private String[] getKeysFromSharedPreferences(SharedPreferences prefs) {
        Set keySet = prefs.getAll().keySet();
        String[] retVal = new String[keySet.size()];
        keySet.toArray(retVal);
        return retVal;
    }

    @Override
    public void saveUserPrefs(Object context) {
        if(userPreferencesEditor != null) {
            userPreferencesEditor.commit();
            userPreferencesEditor = null;
        }
    }

    @Override
    public String getAppPref(String key, Object context) {
        return getAppSharedPreferences((Context)context).getString(key, null);
    }

    public void setAppPref(String key, String value, Object context) {
        SharedPreferences prefs = getAppSharedPreferences((Context)context);
        SharedPreferences.Editor editor = prefs.edit();
        if(value != null) {
            editor.putString(key, value);
        }else {
            editor.remove(key);
        }
        editor.commit();
    }

    /**
     * @inheritDoc
     */
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
        //conn.setRequestProperty("Connection", "close");

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
    public AppView getAppView(Object context) {
        Activity activity = (Activity)context;
        AppViewAndroid view = appViews.get(activity);
        if(view == null) {
            view = new AppViewAndroid(this, activity);
            appViews.put(activity, view);
        }

        return view;
    }

    @Override
    public UMLog getLogger() {
        return logger;
    }

    public boolean waitForHTTPReady(int interval, int timeout) {
        if(isHTTPReady()) {
            return true;
        }

        int waitTime = 0;
        while(!isHTTPReady() && waitTime < timeout) {
            try { Thread.sleep(interval); }
            catch(InterruptedException e) {}
            waitTime += interval;
        }

        return isHTTPReady();
    }

    @Override
    public String openContainer(String containerURI, String mimeType) {
        int waitTime = 0;
        waitForHTTPReady(HTTP_CHECK_INTERVAL, HTTP_READY_TIMEOUT);

        l(UMLog.INFO, 367, containerURI + " type (" + mimeType + ")" + " ready : " + isHTTPReady());

        String openPath = httpService.mountZIP(containerURI);


        return openPath;
    }

    public boolean isHTTPReady() {
        return httpService != null && httpService.getActiveServer() != null;
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

        private Object key;

        public HTTPServiceConnection(Object key) {
            this.key = key;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            HTTPService.HTTPBinder httpBinder = (HTTPService.HTTPBinder)service;
            HTTPService bindedService = httpBinder.getService();
            UstadMobileSystemImplAndroid.this.httpService = httpBinder.getService();
            UstadMobileSystemImplAndroid.this.activityToHttpServiceMap.put(key, bindedService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }


    /**
     * Represents a single Download: backed by the Android DownloadManager service
     *
     */
    public static class DownloadJob implements UMTransferJob {


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

        public static final int IDX_STATUS = 2;

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

        public DownloadJob(String srcURL, String destFileURI, UstadMobileSystemImplAndroid hostImpl, Context context) {
            this.hostImpl = hostImpl;
            this.srcURL = srcURL;
            this.destFileURI = destFileURI;

            this.progressListeners = new LinkedList<UMProgressListener>();
            this.finished = false;
            cachedTotalSize = -1;
            ctx = context;
        }

        @Override
        public void start() {
            /* TODO: In Android 2.3 if the destination file already exists: it must be removed or
            *  we must use a temporary dir
            *  */
            DownloadManager mgr = (DownloadManager)ctx.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(this.srcURL));

            File destFile = new File(destFileURI);
            String destStr = destFile.getAbsolutePath();
            request.setDestinationUri(Uri.fromFile(destFile));
            final DownloadJob thisJob = this;

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

            if(evtType == UMProgressEvent.TYPE_PROGRESS && downloadStatus[IDX_STATUS] == DownloadManager.STATUS_SUCCESSFUL) {
                //actually - it's really all done...
                cleanup();
            }else {
                UMProgressEvent evt = new UMProgressEvent(this, evtType,
                        downloadStatus[0], downloadStatus[1], status);
                for(int i = 0; i < progressListeners.size(); i++) {
                    progressListeners.get(i).progressUpdated(evt);
                }
            }
        }


        /**
         * Fire a progress event as TYPE_PROGRESS with the current progress to all registered listeners
         */
        private void fireProgressEvent() {
            fireProgressEvent(UMProgressEvent.TYPE_PROGRESS, 0);
        }

        private void fireDownloadComplete() {
            Log.i(TAG, "Firing download complete");
            fireProgressEvent(UMProgressEvent.TYPE_COMPLETE, 200);
        }

        public synchronized void cleanup() {
            //when everything is done...  done only once
            if(timerProgressUpdate != null) {
                timerProgressUpdate.cancel();
                timerProgressUpdate = null;

                final int[] byteTotals = this.getProgressAndTotal();
                this.finished = true;
                this.finishedBytesDownloaded = byteTotals[IDX_DOWNLOADED_SO_FAR];
                this.finishedTotalSize = byteTotals[IDX_BYTES_TOTAL];
                Log.d(TAG, "Download Size: " + finishedBytesDownloaded + " / " + finishedTotalSize);

                try { notifyAll(); }
                catch(Exception e) {}

                fireDownloadComplete();
            }
        }


        private int[] getProgressAndTotal() {
            int[] retVal = new int[3];
            if(this.isFinished()) {
                retVal[IDX_DOWNLOADED_SO_FAR] = this.finishedBytesDownloaded;
                retVal[IDX_BYTES_TOTAL] = this.finishedTotalSize;
                retVal[IDX_STATUS] = DownloadManager.STATUS_SUCCESSFUL;
            }else {
                DownloadManager mgr = (DownloadManager)ctx.getSystemService(
                        Context.DOWNLOAD_SERVICE);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(this.downloadID);
                Cursor cursor = mgr.query(query);
                cursor.moveToFirst();


                retVal[IDX_DOWNLOADED_SO_FAR] = cursor.getInt(cursor.getColumnIndex(
                        DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                retVal[IDX_BYTES_TOTAL] = cursor.getInt(cursor.getColumnIndex(
                        DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                retVal[IDX_STATUS] = cursor.getInt(cursor.getColumnIndex(
                        DownloadManager.COLUMN_STATUS));
            }

            return retVal;
        }

        public long getDownloadID() {
            return this.downloadID;
        }


        private void startProgressTracking(final DownloadJob job) {
            hostImpl.waitForHTTPReady(10, 5000);
            hostImpl.httpService.watchDownloadJob(downloadID, this);
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
        public long getBytesDownloadedCount() {
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
            return this.finished;
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
