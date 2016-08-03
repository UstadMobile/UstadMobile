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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;

import com.toughra.ustadmobile.BuildConfig;
import com.ustadmobile.core.MessageIDConstants;
import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.LoginController;
import com.ustadmobile.core.impl.*;
import com.ustadmobile.core.tincan.TinCanResultListener;
import com.ustadmobile.core.tincan.TinCanStatement;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.util.UMTinCanUtil;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.AttendanceView;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.core.view.CatalogView;
import com.ustadmobile.core.view.ClassManagementView;
import com.ustadmobile.core.view.ContainerView;
import com.ustadmobile.core.view.EnrollStudentView;
import com.ustadmobile.core.view.LoginView;
import com.ustadmobile.core.view.UserSettingsView;
import com.ustadmobile.port.android.impl.http.HTTPService;
import com.ustadmobile.port.android.impl.zip.ZipFileHandleAndroid;
import com.ustadmobile.port.android.view.AppViewAndroid;
import com.ustadmobile.port.android.view.AttendanceActivity;
import com.ustadmobile.port.android.view.BasePointActivity;
import com.ustadmobile.port.android.view.CatalogActivity;
import com.ustadmobile.port.android.view.ClassManagementActivity;
import com.ustadmobile.port.android.view.ContainerActivity;
import com.ustadmobile.port.android.view.EnrollStudentActivity;
import com.ustadmobile.port.android.view.LoginActivity;
import com.ustadmobile.port.android.view.UserSettingsActivity;

import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;

import android.os.Build;
import android.util.Log;
import android.util.Xml;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;

import org.json.JSONObject;
import org.xmlpull.v1.*;


/**
 * Created by mike on 07/06/15.
 */
public class UstadMobileSystemImplAndroid extends UstadMobileSystemImplSE {

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

    private UMLogAndroid logger;

    private HashMap<Object, HTTPService> activityToHttpServiceMap;

    public static final String START_USERNAME = "START_USERNAME";

    public static final String START_AUTH = "START_AUTH";

    private WeakHashMap<Activity, AppViewAndroid> appViews;

    private HashMap<UMDownloadCompleteReceiver, BroadcastReceiver> downloadCompleteReceivers;

    private Timer sendStatementsTimer;

    /**
     * Some mime types that the Android OS does not know about but we do...
     * Mapped: Mime type -> extension
     */
    private HashMap<String, String> knownMimeToExtensionMap;

    /**
     @deprecated
     */
    public UstadMobileSystemImplAndroid() {
        logger = new UMLogAndroid();
        activityToHttpServiceMap = new HashMap<>();
        appViews = new WeakHashMap<>();
        downloadCompleteReceivers = new HashMap<>();
        knownMimeToExtensionMap = new HashMap<>();
        knownMimeToExtensionMap.put("application/epub+zip", "epub");

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

        if(context instanceof Activity) {
            ((Activity)context).runOnUiThread(new Runnable() {
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        WebView.setWebContentsDebuggingEnabled(true);
                    }
                }
            });
        }

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

    @Override
    public boolean isJavascriptSupported() {
        return true;
    }

    @Override
    public boolean queueTinCanStatement(final JSONObject stmt, final Object context) {
        if(sendStatementsTimer == null) {
            sendStatementsTimer = new Timer();
        }

        sendStatementsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                OutputStream out = null;
                int responseCode = 0;
                String stmtId = null;

                try {
                    if(stmt.optString("id") == null || stmt.optString("id").length() == 0) {
                        stmt.put("id", UMTinCanUtil.generateUUID());
                    }
                    stmtId = stmt.getString("id");

                    String stmtEndpoint = LoginController.LLRS_XAPI_ENDPOINT +"statements?statementId="
                            + URLEncoder.encode(stmtId, "UTF-8");

                    URL url = new URL(stmtEndpoint);
                    conn = (HttpURLConnection)url.openConnection();
                    conn.setRequestProperty("X-Experience-API-Version", "1.0.1");
                    conn.setRequestProperty("Authorization", LoginController.encodeBasicAuth(
                            getActiveUser(context), getActiveUserAuth(context)));

                    conn.setRequestMethod("PUT");
                    conn.setDoOutput(true);
                    out = conn.getOutputStream();
                    out.write(stmt.toString().getBytes("UTF-8"));
                    out.flush();
                    out.close();


                    conn.connect();
                    responseCode = conn.getResponseCode();
                    UstadMobileSystemImpl.l(UMLog.INFO, 377, " send stmt: " + stmtId + ": server responds: "
                            + responseCode);
                }catch(Exception e) {
                    l(UMLog.ERROR, 191, stmtId, e);
                }finally {
                    UMIOUtils.closeOutputStream(out);
                    if(conn != null) {
                        conn.disconnect();
                    }
                }
            }
        }, 100);



        return true;
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


    public void handleActivityStop(Activity activity) {

    }

    public void handleActivityDestroy(Activity activity) {

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
        }else if(cls.equals(UserSettingsView.class)) {
            androidClass = UserSettingsActivity.class;
        }else if(cls.equals(AttendanceView.class)) {
            androidClass = AttendanceActivity.class;
        }else if(cls.equals(BasePointView.class)) {
            androidClass = BasePointActivity.class;
        }else if(cls.equals(ClassManagementView.class)) {
            androidClass = ClassManagementActivity.class;
        }else if(cls.equals(EnrollStudentView.class)){
            androidClass = EnrollStudentActivity.class;
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
        String systemBaseDir = getSystemBaseDir();

        if((mode & CatalogController.SHARED_RESOURCE) == CatalogController.SHARED_RESOURCE) {
            dirList.add(new UMStorageDir(systemBaseDir, getString(MessageIDConstants.device), false, true, false));

            //Find external directories
            String[] externalDirs = findRemovableStorage();
            for(String extDir : externalDirs) {
                dirList.add(new UMStorageDir(UMFileUtil.joinPaths(new String[]{extDir,
                    UstadMobileSystemImpl.CONTENT_DIR_NAME}), getString(MessageIDConstants.memory_card),
                        true, true, false, false));
            }
        }

        if((mode & CatalogController.USER_RESOURCE) == CatalogController.USER_RESOURCE) {
            String userBase = UMFileUtil.joinPaths(new String[]{systemBaseDir, "user-"
                    + getActiveUser(context)});
            dirList.add(new UMStorageDir(userBase, getString(MessageIDConstants.device), false, true, true));
        }




        UMStorageDir[] retVal = new UMStorageDir[dirList.size()];
        dirList.toArray(retVal);
        return retVal;
    }

    /**
     * Method to accomplish the surprisingly tricky task of finding the external SD card (if this
     * device has one)
     *
     * Approach borrowed from:
     *  http://pietromaggi.com/2014/10/19/finding-the-sdcard-path-on-android-devices/
     *
     * Note: Approaches that use a mount based way of looking at things are returning paths that
     * actually are not actually usable.  Therefor: use the approach based on environment variables.
     *
     * @return A HashSet of paths to any external memory cards mounted
     */
    public String[] findRemovableStorage() {
        String secondaryStorage = System.getenv("SECONDARY_STORAGE");
        if(secondaryStorage == null || secondaryStorage.length() == 0) {
            secondaryStorage = System.getenv("EXTERNAL_SDCARD_STORAGE");
        }

        if(secondaryStorage != null) {
            return secondaryStorage.split(":");
        }else {
            return new String[0];
        }
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
    public long fileLastModified(String fileURI) {
        return new File(fileURI).lastModified();
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

    
    /** 
     * Use Android assets instead 
     */
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
        //TODO: surround with try catch - sometimes this can go wrong with android SQL errors: in which case return null
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
    public void registerDownloadCompleteReceiver(final UMDownloadCompleteReceiver receiver, final Object context) {
        IntentFilter downloadCompleteIntentFilter =
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        BroadcastReceiver completeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long downloadID =intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
                receiver.downloadStatusUpdated(new UMDownloadCompleteEvent(downloadID,
                        getFileDownloadStatus(downloadID, context)));
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
    public long fileAvailableSize(String fileURI) throws IOException {
        return new File(fileURI).getFreeSpace();
    }

    @Override
    public boolean makeDirectory(String dirPath) throws IOException {
        File newDir = new File(dirPath);
        return newDir.mkdir();
    }

    @Override
    public boolean makeDirectoryRecursive(String dirURI) throws IOException {
        return new File(dirURI).mkdirs();
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

    public XmlPullParser newPullParser() throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        return parser;
    }

    @Override
    public XmlSerializer newXMLSerializer() {
        return Xml.newSerializer();
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


    /**
     * @inheritDoc
     */
    @Override
    public ZipFileHandle openZip(String name) throws IOException{
        return new ZipFileHandleAndroid(name);
    }

    /**
     * Running on Android we will take the "full fat" version of any files... eg. files without
     * a x-umprofile tag
     *
     * @return
     */
    @Override
    public String getUMProfileName() {
        return null;
    }

    @Override
    public String getMimeTypeFromExtension(String extension) {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    @Override
    public String getExtensionFromMimeType(String mimeType) {
        String extension =MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        if(extension == null) {
            extension = knownMimeToExtensionMap.get(mimeType);
        }

        return extension;
    }

    @Override
    public void getResumableRegistrations(final String activityId, final Object context, final TinCanResultListener listener)  {
        new Thread(new Runnable() {
            public void run() {
                String stmtURL =  LoginController.LLRS_XAPI_ENDPOINT + "statements";
                Hashtable headers = UMTinCanUtil.makeXAPIHeaders(context);

                try {
                    JSONObject actor = UMTinCanUtil.makeActorFromActiveUser(context);
                    //Query local xAPI server
                    String launchQueryBaseURL = stmtURL + "?"
                            +"agent=" + URLEncoder.encode(actor.toString(), "UTF-8")
                            +"&activity=" + URLEncoder.encode(activityId, "UTF-8");

                    String launchQueryURL = launchQueryBaseURL +
                            "&verb=" +URLEncoder.encode("http://adlnet.gov/expapi/verbs/launched", "UTF-8") +
                            "&limit=" + RESUME_MAX_CHOICES;

                    String[] invalidatingVerbs = new String[]{"http://adlnet.gov/expapi/verbs/completed",
                            "http://adlnet.gov/expapi/verbs/terminated"};


                    String completedQueryURL = launchQueryBaseURL +
                            "&verb=" +URLEncoder.encode("http://adlnet.gov/expapi/verbs/completed", "UTF-8");
                    String terminatedQueryURL = launchQueryBaseURL +
                            "&verb=" +URLEncoder.encode("http://adlnet.gov/expapi/verbs/terminated", "UTF-8");

                    HTTPResult launchResult = makeRequest(launchQueryURL, headers, null);
                    String launchResponse = new String(launchResult.getResponse(), "UTF-8");
                    TinCanStatement[] launchedStmts = UMTinCanUtil.getStatementsFromResult(launchResponse);

                    //if null was returned then it means there was an error running
                    if(launchedStmts == null) {
                        listener.resultReady(null);
                        return;
                    }


                    String currentReg;
                    int x = 0;

                    ArrayList<TinCanStatement> filteredStmts = new ArrayList<>();
                    int j;
                    String queryURL;
                    HTTPResult queryResult;
                    TinCanStatement[] queryStmts;

                    long queryTimeCount = 0;
                    long queryStartTime;
                    for(int i = 0; i < launchedStmts.length && filteredStmts.size() < RESUME_MAX_CHOICES; i++) {
                        currentReg = launchedStmts[i].getRegistrationUUID();
                        if(currentReg == null) {
                            continue;
                        }

                        boolean stmtComplete = false;

                        for(j = 0; j < invalidatingVerbs.length && !stmtComplete; j++) {
                            queryURL = launchQueryBaseURL + "&verb=" + URLEncoder.encode(
                                invalidatingVerbs[j], "UTF-8") + "&registration=" + currentReg;

                            queryStartTime = System.currentTimeMillis();
                            queryResult = makeRequest(queryURL, headers, null);
                            queryTimeCount += (System.currentTimeMillis() - queryStartTime);
                            queryStmts = UMTinCanUtil.getStatementsFromResult(new String(
                                    queryResult.getResponse(), "UTF-8"));
                            if(queryStmts.length > 0) {
                                stmtComplete = true;
                            }
                        }

                        if(!stmtComplete) {
                            //make sure there are actually statements here... not just something launched and never filled
                            queryURL = launchQueryBaseURL + "&registration=" + currentReg + "&limit=3";
                            queryStartTime = System.currentTimeMillis();
                            queryResult = makeRequest(queryURL, headers, null);
                            queryTimeCount += (System.currentTimeMillis() - queryStartTime);
                            queryStmts  = UMTinCanUtil.getStatementsFromResult(new String(
                                    queryResult.getResponse(), "UTF-8"));
                            if(queryStmts.length > 2) {
                                filteredStmts.add(launchedStmts[i]);
                            }
                        }
                    }
                    l(UMLog.INFO, 315, ""+queryTimeCount);

                    /*
                    JSONObject[] completedStmts = UMTinCanUtil.getStatementsFromResult(
                            new String(makeRequest(completedQueryURL, headers, null).getResponse(), "UTF-8"));
                    JSONObject[] terminatedStmts = UMTinCanUtil.getStatementsFromResult(
                            new String(makeRequest(terminatedQueryURL, headers, null).getResponse(), "UTF-8"));

                    String[] completedRegistrations = UMTinCanUtil.getDistinctRegistrations(completedStmts);
                    String[] terminatedRegistrations = UMTinCanUtil.getDistinctRegistrations(terminatedStmts);


                    JSONObject[] openStmts = UMTinCanUtil.filterByRegistration(completedRegistrations,
                            false, launchedStmts);
                    openStmts = UMTinCanUtil.filterByRegistration(terminatedRegistrations, false,
                            openStmts);
                    */
                    TinCanStatement[] resultStmts = new TinCanStatement[filteredStmts.size()];
                    filteredStmts.toArray(resultStmts);
                    listener.resultReady(resultStmts);
                }catch(Exception e) {
                    listener.resultReady(null);

                }
            }
        }).start();
    }

    @Override
    public long getBuildTime() {
        return BuildConfig.TIMESTAMP;
    }

    @Override
    public String getVersion(Object ctx) {
        Context context = (Context)ctx;
        String versionInfo = null;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionInfo = 'v' + pInfo.versionName + " (#" + pInfo.versionCode + ')';
        }catch(PackageManager.NameNotFoundException e) {
            l(UMLog.ERROR, 90, null, e);
        }
        return versionInfo;
    }
}
