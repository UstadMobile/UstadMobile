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
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Xml;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.widget.Toast;

import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.catalog.contenttype.EPUBTypePlugin;
import com.ustadmobile.core.catalog.contenttype.XapiPackageTypePlugin;
import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.controller.UserSettingsController;
import com.ustadmobile.core.impl.ContainerMountRequest;
import com.ustadmobile.core.impl.TinCanQueueListener;
import com.ustadmobile.core.impl.UMDownloadCompleteReceiver;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.tincan.TinCanResultListener;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.util.UMTinCanUtil;
import com.ustadmobile.core.view.AboutView;
import com.ustadmobile.core.view.AddFeedDialogView;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.core.view.CatalogEntryView;
import com.ustadmobile.core.view.CatalogView;
import com.ustadmobile.core.view.ContainerView;
import com.ustadmobile.core.view.LoginView;
import com.ustadmobile.core.view.RegistrationView;
import com.ustadmobile.core.view.SettingsDataSyncListView;
import com.ustadmobile.core.view.SettingsDataUsageView;
import com.ustadmobile.core.view.UserSettingsView;
import com.ustadmobile.core.view.UserSettingsView2;
import com.ustadmobile.core.view.WelcomeView;
import com.ustadmobile.core.view.XapiPackageView;
import com.ustadmobile.nanolrs.core.endpoints.XapiAgentEndpoint;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsForwardingEndpoint;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsForwardingListener;
import com.ustadmobile.nanolrs.core.manager.UserCustomFieldsManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.port.android.generated.MessageIDMap;
import com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroid;
import com.ustadmobile.port.android.netwokmanager.NetworkServiceAndroid;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.android.view.AboutActivity;
import com.ustadmobile.port.android.view.AddFeedDialogFragment;
import com.ustadmobile.port.android.view.AppViewAndroid;
import com.ustadmobile.port.android.view.AttendanceActivity;
import com.ustadmobile.port.android.view.BasePointActivity;
import com.ustadmobile.port.android.view.CatalogActivity;
import com.ustadmobile.port.android.view.CatalogEntryActivity;
import com.ustadmobile.port.android.view.ClassManagementActivity;
import com.ustadmobile.port.android.view.ClassManagementActivity2;
import com.ustadmobile.port.android.view.ContainerActivity;
import com.ustadmobile.port.android.view.EnrollStudentActivity;
import com.ustadmobile.port.android.view.LoginDialogFragment;
import com.ustadmobile.port.android.view.ReceiveCourseDialogFragment;
import com.ustadmobile.port.android.view.RegistrationDialogFragment;
import com.ustadmobile.port.android.view.SendCourseDialogFragment;
import com.ustadmobile.port.android.view.SettingsDataSyncListActivity;
import com.ustadmobile.port.android.view.SettingsDataUsageActivity;
import com.ustadmobile.port.android.view.UserSettingsActivity;
import com.ustadmobile.port.android.view.UserSettingsActivity2;
import com.ustadmobile.port.android.view.UstadBaseActivity;
import com.ustadmobile.port.android.view.WelcomeDialogFragment;
import com.ustadmobile.port.android.view.XapiPackageActivity;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.port.sharedse.view.AttendanceView;
import com.ustadmobile.port.sharedse.view.ClassManagementView;
import com.ustadmobile.port.sharedse.view.ClassManagementView2;
import com.ustadmobile.port.sharedse.view.EnrollStudentView;
import com.ustadmobile.port.sharedse.view.ReceiveCourseView;
import com.ustadmobile.port.sharedse.view.SendCourseView;

import org.json.JSONObject;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.WeakHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


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

    public static final String TAG_DIALOG_FRAGMENT = "UMDialogFrag";

    public static final String ACTION_LOCALE_CHANGE = "com.ustadmobile.locale_change";

    /**
     * Map of view names to the activity class that is implementing them on Android
     *
     * @see UstadMobileSystemImplAndroid#go(String, Hashtable, Object)
     */
    public static final HashMap<String, Class> viewNameToAndroidImplMap = new HashMap<>();

    static {

        viewNameToAndroidImplMap.put(LoginView.VIEW_NAME, LoginDialogFragment.class);
        viewNameToAndroidImplMap.put(ContainerView.VIEW_NAME, ContainerActivity.class);
        viewNameToAndroidImplMap.put(CatalogView.VIEW_NAME, CatalogActivity.class);
        viewNameToAndroidImplMap.put(UserSettingsView.VIEW_NAME, UserSettingsActivity.class);
        viewNameToAndroidImplMap.put(SettingsDataUsageView.VIEW_NAME, SettingsDataUsageActivity.class);
        viewNameToAndroidImplMap.put(SettingsDataSyncListView.VIEW_NAME, SettingsDataSyncListActivity.class);
        //Account settings:
        //viewNameToAndroidImplMap.put(AccountSettingsView.VIEW_NAME, AccountSettingsActivity.class);
        viewNameToAndroidImplMap.put(BasePointView.VIEW_NAME, BasePointActivity.class);
        viewNameToAndroidImplMap.put(ClassManagementView.VIEW_NAME, ClassManagementActivity.class);
        viewNameToAndroidImplMap.put(EnrollStudentView.VIEW_NAME, EnrollStudentActivity.class);
        viewNameToAndroidImplMap.put(ClassManagementView2.VIEW_NAME, ClassManagementActivity2.class);
        viewNameToAndroidImplMap.put(AboutView.VIEW_NAME, AboutActivity.class);
        viewNameToAndroidImplMap.put(AttendanceView.VIEW_NAME, AttendanceActivity.class);
        viewNameToAndroidImplMap.put(CatalogEntryView.VIEW_NAME, CatalogEntryActivity.class);
        viewNameToAndroidImplMap.put(UserSettingsView2.VIEW_NAME, UserSettingsActivity2.class);
        viewNameToAndroidImplMap.put(WelcomeView.VIEW_NAME, WelcomeDialogFragment.class);
        viewNameToAndroidImplMap.put(RegistrationView.VIEW_NAME, RegistrationDialogFragment.class);
        viewNameToAndroidImplMap.put(SendCourseView.VIEW_NAME, SendCourseDialogFragment.class);
        viewNameToAndroidImplMap.put(ReceiveCourseView.VIEW_NAME, ReceiveCourseDialogFragment.class);
        viewNameToAndroidImplMap.put(XapiPackageView.VIEW_NAME, XapiPackageActivity.class);
        viewNameToAndroidImplMap.put(AddFeedDialogView.VIEW_NAME, AddFeedDialogFragment.class);
    }


    private String currentUsername;

    private String currentAuth;

    private SharedPreferences appPreferences;

    private SharedPreferences userPreferences;

    private SharedPreferences.Editor userPreferencesEditor;

    private UMLogAndroid logger;

    public static final String START_USERNAME = "START_USERNAME";

    public static final String START_AUTH = "START_AUTH";

    private WeakHashMap<Context, AppViewAndroid> appViews;

    private HashMap<UMDownloadCompleteReceiver, BroadcastReceiver> downloadCompleteReceivers;

    private Timer sendStatementsTimer;

    /**
     * Map of TinCanQueueListeners to the XapiQueueStatusListeners used by NanoLRS
     */
    private HashMap<TinCanQueueListener, XapiStatementsForwardingListener> queueStatusListeners;

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

            /*
             * NetworkServiceAndroid will register itself using the Application to receive lifecycle
             * callbacks. That however happens when the service is created, by which point an activity
             * might have already started. This check happens when the NetworkService is bound to
             * each activity.
             */
            if(context instanceof UstadBaseActivity
                    && name.getClassName().equals(NetworkServiceAndroid.class.getName())) {
                UstadBaseActivity activity = (UstadBaseActivity)context;
                if(activity.isStarted()) {
                    NetworkServiceAndroid networkService = ((NetworkServiceAndroid.LocalServiceBinder)iBinder)
                            .getService();
                    networkService.getNetworkManager().onActivityStarted(activity);
                }
            }

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

    /**
     * Simple async task to handle getting the setup file
     * Param 0 = boolean - true to zip, false otherwise
     */
    private static class GetSetupFileAsyncTask extends AsyncTask<Boolean, Void, String> {

        private Context context;

        private UmCallback doneCallback;

        private GetSetupFileAsyncTask(UmCallback doneCallback, Context context) {
            this.context = context;
            this.doneCallback = doneCallback;
        }

        @Override
        protected String doInBackground(Boolean... booleans) {
            File apkFile = new File(((Context)context).getApplicationInfo().sourceDir);
            String baseName = CoreBuildConfig.BASE_NAME + "-" + CoreBuildConfig.VERSION;
            FileInputStream apkFileIn = null;
            Context ctx = (Context)context;
            File outDir = new File(ctx.getFilesDir(), "shared");
            if(!outDir.isDirectory())
                outDir.mkdirs();

            if(booleans[0]) {
                ZipOutputStream zipOut = null;
                File outZipFile = new File(outDir, baseName + ".zip");
                try {
                    zipOut = new ZipOutputStream(new FileOutputStream(outZipFile));
                    zipOut.putNextEntry(new ZipEntry(baseName + ".apk"));
                    apkFileIn = new FileInputStream(apkFile);
                    UMIOUtils.readFully(apkFileIn, zipOut, 1024);
                    zipOut.closeEntry();
                }catch(IOException e) {
                    e.printStackTrace();
                }finally {
                    UMIOUtils.closeOutputStream(zipOut);
                    UMIOUtils.closeInputStream(apkFileIn);
                }

                return outZipFile.getAbsolutePath();
            }else {
                FileOutputStream fout = null;
                File outApkFile = new File(outDir, baseName + ".apk");
                try {
                    apkFileIn = new FileInputStream(apkFile);
                    fout = new FileOutputStream(outApkFile);
                    UMIOUtils.readFully(apkFileIn, fout, 1024);
                }catch(IOException e) {
                    e.printStackTrace();
                }finally {
                    UMIOUtils.closeInputStream(apkFileIn);
                    UMIOUtils.closeOutputStream(fout);
                }

                return outApkFile.getAbsolutePath();
            }
        }

        @Override
        protected void onPostExecute(String filePath) {
            doneCallback.onSuccess(filePath);
        }
    }


    protected HashMap<Context, ServiceConnection> networkServiceConnections = new HashMap<>();

    protected NetworkManagerAndroid networkManagerAndroid;

    /**
     @deprecated
     */
    public UstadMobileSystemImplAndroid() {
        logger = new UMLogAndroid();
        appViews = new WeakHashMap<>();
        downloadCompleteReceivers = new HashMap<>();
        queueStatusListeners = new HashMap<>();
        networkManagerAndroid = new NetworkManagerAndroid();
        networkManagerAndroid.setServiceConnectionMap(networkServiceConnections);
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
    public void setLocale(String locale, Object context) {
        super.setLocale(locale, context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences((Context)context);
        prefs.edit().putString(UserSettingsController.PREFKEY_LANG, locale).apply();
    }

    @Override
    public String getLocale(Object context) {
        String locale = super.getLocale(context);
        if(locale == null) {
            locale = PreferenceManager.getDefaultSharedPreferences((Context)context).getString(
                    UserSettingsController.PREFKEY_LANG, "");
            super.setLocale(locale, context);
        }

        return locale;
    }

    @Override
    public String getString(int messageCode, Object context) {
        Integer androidId = MessageIDMap.ID_MAP.get(messageCode);
        if(androidId != null) {
            return ((Context)context).getResources().getString(androidId);
        }else {
            return null;
        }
    }

    @Override
    public boolean loadActiveUserInfo(Object context) {
        SharedPreferences appPrefs = getAppSharedPreferences((Context)context);
        currentUsername = appPrefs.getString(KEY_CURRENTUSER, null);
        currentAuth = appPrefs.getString(KEY_CURRENTAUTH, null);
        if(currentUsername != null) {
            xapiAgent = XapiAgentEndpoint.createOrUpdate(context, null, currentUsername,
                    UMTinCanUtil.getXapiServer(context));
        }
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
                CoreBuildConfig.DEFAULT_XAPI_SERVER, context);

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
        Intent networkIntent = new Intent(mContext, NetworkServiceAndroid.class);
        BaseServiceConnection connection = new BaseServiceConnection(mContext, networkServiceConnections);
        mContext.bindService(networkIntent, connection, Context.BIND_AUTO_CREATE|Context.BIND_ADJUST_WITH_ACTIVITY);
    }

    /**
     *
     * @param mContext
     */
    public void handleActivityStart(Activity mContext) {

    }


    public void handleActivityStop(Activity mContext) {

    }

    public void handleActivityDestroy(Activity mContext) {
        mContext.unbindService(networkServiceConnections.get(mContext));
        networkServiceConnections.remove(mContext);
        if(appViews.containsKey(mContext)) {
            appViews.remove(mContext);
        }
    }


    public void go(String viewName, Hashtable args, Object context) {
        Class androidImplClass = viewNameToAndroidImplMap.get(viewName);
        Context ctx = (Context)context;
        Bundle argsBundle = UMAndroidUtil.hashtableToBundle(args);

        if(androidImplClass == null) {
            Log.wtf(UMLogAndroid.LOGTAG, "No activity for " + viewName + " found");
            Toast.makeText(ctx, "ERROR: No Activity found for view: " + viewName,
                    Toast.LENGTH_LONG).show();
            return;
        }

        if(DialogFragment.class.isAssignableFrom(androidImplClass)) {
            String toastMsg = null;
            try {
                DialogFragment dialog = (DialogFragment)androidImplClass.newInstance();
                if(args != null)
                    dialog.setArguments(argsBundle);
                AppCompatActivity activity = (AppCompatActivity)context;
                dialog.show(activity.getSupportFragmentManager(),TAG_DIALOG_FRAGMENT);
            }catch(InstantiationException e) {
                Log.wtf(UMLogAndroid.LOGTAG, "Could not instantiate dialog", e);
                toastMsg = "Dialog error: " + e.toString();
            }catch(IllegalAccessException e2) {
                Log.wtf(UMLogAndroid.LOGTAG, "Could not instantiate dialog", e2);
                toastMsg = "Dialog error: " + e2.toString();
            }

            if(toastMsg != null) {
                Toast.makeText(ctx, toastMsg, Toast.LENGTH_LONG).show();
            }
        }else {
            Intent startIntent = new Intent(ctx, androidImplClass);
            if(args != null)
                startIntent.putExtras(argsBundle);

            ctx.startActivity(startIntent);
        }
    }

    @Override
    protected String getSystemBaseDir(Object context) {
        return new File(Environment.getExternalStorageDirectory(), "ustadmobileContent").getAbsolutePath();
    }

    @Override
    public String getCacheDir(int mode, Object context) {
        Context ctx = (Context)context;
        File cacheDir = ctx.getCacheDir();
        if(mode == CatalogPresenter.SHARED_RESOURCE) {
            return cacheDir.getAbsolutePath();
        }else {
            return new File(cacheDir, "user-" + getActiveUser(context)).getAbsolutePath();
        }
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
            HttpHost proxy = new HttpHosqt(proxyAddress,proxyPort);
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
        if(resURI.charAt(0) == '/')
            resURI = resURI.substring(1);

        return ((Context)context).getAssets().open(resURI);
    }



    @Override
    public int[] getFileDownloadStatus(String downloadID, Object context) {
        return new int[3];
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
        return currentUsername != null ? getUserPreferences((Context)context).getString(key, null) : null;
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
        Context ctx = (Context)context;
        AppViewAndroid view = appViews.get(ctx);
        if(view == null) {
            view = new AppViewAndroid(this, ctx);
            appViews.put(ctx, view);
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
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        if(mimeType != null) {
            return mimeType;
        }else {
            return super.getMimeTypeFromExtension(extension);
        }
    }

    @Override
    public String getExtensionFromMimeType(String mimeType) {
        String extension =MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        if(extension != null) {
            return extension;
        }else {
            return super.getExtensionFromMimeType(mimeType);
        }
    }

    @Override
    public void getResumableRegistrations(final String activityId, final Object context, final TinCanResultListener listener)  {
        //removed
        listener.resultReady(null);
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
    public NetworkManager getNetworkManager() {
        return networkManagerAndroid;
    }

    @Override
    public void getAppSetupFile(Object context, boolean zip, UmCallback callback) {
        GetSetupFileAsyncTask setupFileAsyncTask = new GetSetupFileAsyncTask(callback,
                (Context)context);
        setupFileAsyncTask.execute(zip);
    }

    @Override
    public Class[] getSupportedContentTypePlugins() {
        return new Class[]{
                EPUBTypePlugin.class,
                XapiPackageTypePlugin.class
        };
    }

    @Override
    public String getManifestPreference(String key, Object context) {
        try {
            Context ctx = (Context)context;
            ApplicationInfo ai2 = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(),
                    PackageManager.GET_META_DATA);
            Bundle metaData = ai2.metaData;
            if(metaData != null) {
                return metaData.getString(key);
            }
        }catch(PackageManager.NameNotFoundException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, UMLog.ERROR, key, e);
        }

        return null;
    }

    @Override
    public String getUserDetail(String username, int field, Object dbContext){

        try {
            UserCustomFieldsManager customFieldsManager =
                    PersistenceManager.getInstance().getManager(UserCustomFieldsManager.class);
            UserManager userManager =
                    PersistenceManager.getInstance().getManager(UserManager.class);
            User user = userManager.findByUsername(dbContext, username);
            String value = customFieldsManager.getUserField(user, field, dbContext);
            if (value == null) {
                return "";
            }
            return value;
        }catch(SQLException s){
            s.printStackTrace();
            System.out.println("Unable to get user detail: " + field +
                " for user: " + username);
            return "";
        }


    }

    @Override
    public void mountContainer(final ContainerMountRequest request, final int id,
                               final UmCallback callback) {

        final String scriptPath = UMFileUtil.joinPaths(new String[] {
                networkManagerAndroid.getHttpAndroidAssetsUrl(), "epub-paginate.js"});
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String mountedPath = networkManagerAndroid.mountZipOnHttp(request.getContainerUri(),
                        null, request.isEpubMode(), scriptPath);
                return UMFileUtil.joinPaths(new String[]{networkManagerAndroid.getLocalHttpUrl(),
                    mountedPath});
            }

            @Override
            protected void onPostExecute(String mountedPath) {
                callback.onSuccess(mountedPath);
            }
        }.execute();
    }
}
