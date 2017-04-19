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
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;

import java.io.*;
import java.net.URLConnection;
import java.util.*;
import java.net.URL;
import com.ustadmobile.core.impl.*;
import com.ustadmobile.core.tincan.TinCanResultListener;
import com.ustadmobile.core.view.AboutView;
import com.ustadmobile.core.view.AppView;

import com.ustadmobile.core.view.SettingsDataUsageView;
import com.ustadmobile.port.android.network.DownloadTaskAndroid;
import com.ustadmobile.port.android.network.NetworkManagerAndroid;
import com.ustadmobile.port.android.network.NetworkServiceAndroid;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.sharedse.network.DownloadRequest;
import com.ustadmobile.port.sharedse.network.NetworkManagerSharedSE;
import com.ustadmobile.port.sharedse.view.AttendanceView;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.core.view.CatalogView;
import com.ustadmobile.port.sharedse.view.ClassManagementView;
import com.ustadmobile.port.sharedse.view.ClassManagementView2;
import com.ustadmobile.core.view.ContainerView;
import com.ustadmobile.port.sharedse.view.EnrollStudentView;
import com.ustadmobile.core.view.LoginView;
import com.ustadmobile.core.view.UserSettingsView;
import com.ustadmobile.port.android.impl.http.HTTPService;

import com.ustadmobile.port.android.view.*;
import com.ustadmobile.nanolrs.core.endpoints.*;

import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;


import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.util.Xml;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.widget.Toast;

import org.json.JSONObject;
import org.xmlpull.v1.*;

import edu.rit.se.wifibuddy.WifiDirectHandler;


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

    /**
     * Map of view names to the activity class that is implementing them on Android
     *
     * @see UstadMobileSystemImplAndroid#go(String, Hashtable, Object)
     */
    public static final HashMap<String, Class> viewNameToActivityMap = new HashMap<>();

    static {
        viewNameToActivityMap.put(LoginView.VIEW_NAME, LoginActivity.class);
        viewNameToActivityMap.put(ContainerView.VIEW_NAME, ContainerActivity.class);
        viewNameToActivityMap.put(CatalogView.VIEW_NAME, CatalogActivity.class);
        viewNameToActivityMap.put(UserSettingsView.VIEW_NAME, UserSettingsActivity.class);
        viewNameToActivityMap.put(BasePointView.VIEW_NAME, BasePointActivity.class);
        viewNameToActivityMap.put(ClassManagementView.VIEW_NAME, ClassManagementActivity.class);
        viewNameToActivityMap.put(EnrollStudentView.VIEW_NAME, EnrollStudentActivity.class);
        viewNameToActivityMap.put(ClassManagementView2.VIEW_NAME, ClassManagementActivity2.class);
        viewNameToActivityMap.put(AboutView.VIEW_NAME, AboutActivity.class);
        viewNameToActivityMap.put(AttendanceView.VIEW_NAME, AttendanceActivity.class);
        viewNameToActivityMap.put(SettingsDataUsageView.VIEW_NAME,SettingsDataUsageActivity.class);
    }


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
     * Map of TinCanQueueListeners to the XapiQueueStatusListeners used by NanoLRS
     */
    private HashMap<TinCanQueueListener, XapiStatementsForwardingListener> queueStatusListeners;

    /**
     * Some mime types that the Android OS does not know about but we do...
     * Mapped: Mime type -> extension
     */
    private HashMap<String, String> knownMimeToExtensionMap;

    /**
     * Base ServiceConnection class used to bind any given context to shared services: notably
     * the HTTP service and the upcoming p2p service.
     */
    public static class BaseServiceConnection implements ServiceConnection {
        private IBinder iBinder;

        private Context context;

        private Map<Context, ServiceConnection> contextToBinderMap;

        public BaseServiceConnection(Context context, Map<Context, ServiceConnection> contextToBinderMap) {
            this.context = context;
            this.contextToBinderMap = contextToBinderMap;
            contextToBinderMap.put(context, this);
        }


        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
                this.iBinder = iBinder;
            if(context instanceof ServiceConnection) {
                ((ServiceConnection)context).onServiceConnected(name, iBinder);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            iBinder= null;
            if(context instanceof ServiceConnection)
                ((ServiceConnection)context).onServiceDisconnected(name);

            contextToBinderMap.remove(context);
        }

        public IBinder getBinder() {
            return iBinder;
        }
    }

    private HashMap<Context, ServiceConnection> httpServiceConnections = new HashMap<>();

    private HashMap<Context, ServiceConnection> p2pServiceConnections = new HashMap<>();

    private NetworkManagerAndroid p2pManager;

    /**
     @deprecated
     */
    public UstadMobileSystemImplAndroid() {
        logger = new UMLogAndroid();
        activityToHttpServiceMap = new HashMap<>();
        appViews = new WeakHashMap<>();
        downloadCompleteReceivers = new HashMap<>();
        queueStatusListeners = new HashMap<>();
        knownMimeToExtensionMap = new HashMap<>();
        knownMimeToExtensionMap.put("application/epub+zip", "epub");
        p2pManager = new NetworkManagerAndroid();
        p2pManager.setServiceConnectionMap(p2pServiceConnections);
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
    public boolean queueTinCanStatement(final JSONObject stmtObj, final Object context) {

        //String xapiServer = "http://umcloud1.ustadmobile.com/umlrs/";
        String xapiServer = getAppPref(
                UstadMobileSystemImpl.PREFKEY_XAPISERVER,
                UstadMobileDefaults.DEFAULT_XAPI_SERVER, context);

        XapiStatementsForwardingEndpoint.putAndQueueStatement(context, stmtObj,
                xapiServer, getActiveUser(context), getActiveUserAuth(context));

        l(UMLog.INFO, 304, null);

        return true;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void addTinCanQueueStatusListener(final TinCanQueueListener listener) {

    }

    /**
     * @inheritDoc
     */
    @Override
    public void removeTinCanQueueListener(TinCanQueueListener listener) {
        queueStatusListeners.remove(listener);
    }

    /**
     * To be called by activities as the first matter of business in the onCreate method
     *
     * Will bind to the HTTP service
     *
     * TODO: This should really be handleContextCreate : This should be used by background services as well
     *
     * @param mContext
     */
    public void handleActivityCreate(Activity mContext, Bundle savedInstanceState) {
        init(mContext);
        Intent intent = new Intent(mContext, HTTPService.class);
        BaseServiceConnection connection = new BaseServiceConnection(mContext, httpServiceConnections);
        mContext.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        Intent p2pIntent = new Intent(mContext, NetworkServiceAndroid.class);
        connection = new BaseServiceConnection(mContext, p2pServiceConnections);
        mContext.bindService(p2pIntent, connection, Context.BIND_AUTO_CREATE);
    }

    public void handleActivityStart(Activity mContext) {

    }


    public void handleActivityStop(Activity mContext) {

    }

    public void handleActivityDestroy(Activity mContext) {
        mContext.unbindService(httpServiceConnections.get(mContext));
        mContext.unbindService(p2pServiceConnections.get(mContext));
    }


    public void go(String viewName, Hashtable args, Object context) {
        Class activityClass = viewNameToActivityMap.get(viewName);
        Context ctx = (Context)context;

        if(activityClass == null) {
            Log.wtf(UMLogAndroid.LOGTAG, "No activity for " + viewName + " found");
            Toast.makeText(ctx, "ERROR: No Activity found for view: " + viewName,
                    Toast.LENGTH_LONG).show();
            return;
        }

        Intent startIntent = new Intent(ctx, activityClass);
        if(args != null)
            startIntent.putExtras(UMAndroidUtil.hashtableToBundle(args));

        ctx.startActivity(startIntent);
    }

    @Override
    protected String getSystemBaseDir() {
        return new File(Environment.getExternalStorageDirectory(), "ustadmobileContent").getAbsolutePath();
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



    @Override
    public Hashtable getSystemInfo() {
        Hashtable ht = new Hashtable();
        ht.put("os", "Android");
        ht.put("osversion", Build.VERSION.RELEASE);

        return ht;
    }


    public URLConnection openConnection(URL url) throws IOException{
        return url.openConnection();
        /*
        String proxyString = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.HTTP_PROXY);
        if (proxyString != null)
        {
            String proxyAddress = proxyString.split(":")[0];
            int proxyPort = Integer.parseInt(proxyString.split(":")[1]);
            Proxy p = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyAddress,proxyPort));
            HttpHost proxy = new HttpHost(proxyAddress,proxyPort);
            con = (HttpURLConnection) url.openConnection(p);
        }
        else
        {
            con = (HttpURLConnection) url.openConnection();
        }
        */
    }
    
    /** 
     * Use Android assets instead 
     */
    @Override
    public InputStream openResourceInputStream(String resURI, Object context) throws IOException {
        return ((Context)context).getAssets().open(resURI);
    }

    @Override
    public String queueFileDownload(String url, String destFileURI, String entryId, Hashtable headers, Object context) {

        DownloadRequest p2pRequest = new DownloadRequest();
        p2pRequest.setFileId(entryId);
        p2pRequest.setFileSource(url);
        p2pRequest.setFileDestination(destFileURI);
        return null;
        //return String.valueOf(p2pManager.requestDownload(context, p2pRequest));
    }

    @Override
    public int[] getFileDownloadStatus(String downloadID, Object context) {
        return p2pManager.getRequestStatus(context,Integer.parseInt(downloadID));
    }

    @Override
    public void registerDownloadCompleteReceiver(final UMDownloadCompleteReceiver receiver, final Object context) {
        IntentFilter downloadCompleteIntentFilter =
                new IntentFilter(DownloadTaskAndroid.ACTION_DOWNLOAD_COMPLETE);
        BroadcastReceiver completeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
               if(intent!=null){
                   String downloadID = intent.getStringExtra(DownloadTaskAndroid.EXTRA_DOWNLOAD_ID);
                   String downloadStatus = intent.getStringExtra(DownloadTaskAndroid.EXTRA_DOWNLOAD_STATUS);
                   String downloadSource = intent.getStringExtra(DownloadTaskAndroid.EXTRA_DOWNLOAD_SOURCE);
                   Log.d(WifiDirectHandler.TAG,"Download ID:"+downloadID+" Download Status:"
                           +downloadStatus+" Download Source: "+(Boolean.parseBoolean(downloadSource)?" Cloud":"Local"));
                   receiver.downloadStatusUpdated(
                           new UMDownloadCompleteEvent(downloadID,Integer.parseInt(downloadStatus),Integer.parseInt(downloadSource)));

               }
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
        //removed
        listener.resultReady(null);
    }

    @Override
    public long getBuildTime() {
        return 0;
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

    @Override
    public boolean isWiFiP2PSupported() {
        //TODO: Use android specific code here to determine if this device supports wifi p2p
        return true;
    }

    @Override
    public NetworkManagerSharedSE getP2PManager() {
        return p2pManager;
    }
}
