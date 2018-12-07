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

import com.squareup.picasso.Picasso;
import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin;
import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.controller.UserSettingsController;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.OpdsEntryStatusCacheDao;
import com.ustadmobile.core.fs.contenttype.EpubTypePluginFs;
import com.ustadmobile.core.fs.contenttype.H5PContentTypeFs;
import com.ustadmobile.core.fs.contenttype.ScormTypePluginFs;
import com.ustadmobile.core.fs.contenttype.XapiPackageTypePluginFs;
import com.ustadmobile.core.fs.db.ContainerFileHelper;
import com.ustadmobile.core.impl.ContainerMountRequest;
import com.ustadmobile.core.impl.UMDownloadCompleteReceiver;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.tincan.TinCanResultListener;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.view.AboutView;
import com.ustadmobile.core.view.AddActivityChangeDialogView;
import com.ustadmobile.core.view.AddFeedDialogView;
import com.ustadmobile.core.view.AddScheduleDialogView;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.core.view.BasePointView2;
import com.ustadmobile.core.view.CatalogEntryView;
import com.ustadmobile.core.view.CatalogView;
import com.ustadmobile.core.view.ClassDetailView;
import com.ustadmobile.core.view.ClassLogDetailView;
import com.ustadmobile.core.view.ClazzActivityEditView;
import com.ustadmobile.core.view.ClazzDetailEnrollStudentView;
import com.ustadmobile.core.view.ClazzEditView;
import com.ustadmobile.core.view.ContainerView;
import com.ustadmobile.core.view.H5PContentView;
import com.ustadmobile.core.view.LoginView;
import com.ustadmobile.core.view.PersonDetailEnrollClazzView;
import com.ustadmobile.core.view.PersonDetailView;
import com.ustadmobile.core.view.PersonEditView;
import com.ustadmobile.core.view.RegistrationView;
import com.ustadmobile.core.view.ReportEditView;
import com.ustadmobile.core.view.ReportNumberOfDaysClassesOpenView;
import com.ustadmobile.core.view.ReportOverallAttendanceView;
import com.ustadmobile.core.view.ReportSelectionView;
import com.ustadmobile.core.view.SELEditView;
import com.ustadmobile.core.view.SELQuestionDetailView;
import com.ustadmobile.core.view.SELQuestionEditView;
import com.ustadmobile.core.view.SELQuestionView;
import com.ustadmobile.core.view.SELRecognitionView;
import com.ustadmobile.core.view.SELSelectConsentView;
import com.ustadmobile.core.view.SELSelectStudentView;
import com.ustadmobile.core.view.ScormPackageView;
import com.ustadmobile.core.view.SelectAttendanceThresholdsDialogView;
import com.ustadmobile.core.view.SelectClazzesDialogView;
import com.ustadmobile.core.view.SelectMultipleTreeDialogView;
import com.ustadmobile.core.view.SelectTwoDatesDialogView;
import com.ustadmobile.core.view.SettingsDataSyncListView;
import com.ustadmobile.core.view.SettingsDataUsageView;
import com.ustadmobile.core.view.UserSettingsView;
import com.ustadmobile.core.view.UserSettingsView2;
import com.ustadmobile.core.view.WelcomeView;
import com.ustadmobile.core.view.XapiPackageView;
import com.ustadmobile.port.android.generated.MessageIDMap;
import com.ustadmobile.port.android.impl.http.UmHttpCachePicassoRequestHandler;
import com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroid;
import com.ustadmobile.port.android.netwokmanager.NetworkServiceAndroid;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.android.view.AboutActivity;
import com.ustadmobile.port.android.view.AddActivityChangeDialogFragment;
import com.ustadmobile.port.android.view.AddFeedDialogFragment;
import com.ustadmobile.port.android.view.AddScheduleDialogFragment;
import com.ustadmobile.port.android.view.AppViewAndroid;
import com.ustadmobile.port.android.view.BasePointActivity;
import com.ustadmobile.port.android.view.BasePointActivity2;
import com.ustadmobile.port.android.view.CatalogActivity;
import com.ustadmobile.port.android.view.CatalogEntryActivity;
import com.ustadmobile.port.android.view.ClazzActivityEditActivity;
import com.ustadmobile.port.android.view.ClazzDetailActivity;
import com.ustadmobile.port.android.view.ClazzDetailEnrollStudentActivity;
import com.ustadmobile.port.android.view.ClazzEditActivity;
import com.ustadmobile.port.android.view.ClazzLogDetailActivity;
import com.ustadmobile.port.android.view.ContainerActivity;
import com.ustadmobile.port.android.view.DownloadDialogFragment;
import com.ustadmobile.port.android.view.H5PContentActivity;
import com.ustadmobile.port.android.view.LoginDialogFragment;
import com.ustadmobile.port.android.view.PersonDetailActivity;
import com.ustadmobile.port.android.view.PersonDetailEnrollClazzActivity;
import com.ustadmobile.port.android.view.PersonEditActivity;
import com.ustadmobile.port.android.view.ReceiveCourseDialogFragment;
import com.ustadmobile.port.android.view.RegistrationDialogFragment;
import com.ustadmobile.port.android.view.ReportEditActivity;
import com.ustadmobile.port.android.view.ReportNumberOfDaysClassesOpenActivity;
import com.ustadmobile.port.android.view.ReportOverallAttendanceActivity;
import com.ustadmobile.port.android.view.ReportSelectionActivity;
import com.ustadmobile.port.android.view.SELEditActivity;
import com.ustadmobile.port.android.view.SELQuestionActivity;
import com.ustadmobile.port.android.view.SELQuestionDetailActivity;
import com.ustadmobile.port.android.view.SELQuestionEditActivity;
import com.ustadmobile.port.android.view.SELRecognitionActivity;
import com.ustadmobile.port.android.view.SELSelectConsentActivity;
import com.ustadmobile.port.android.view.SELSelectStudentActivity;
import com.ustadmobile.port.android.view.ScormPackageActivity;
import com.ustadmobile.port.android.view.SelectAttendanceThresholdsDialogFragment;
import com.ustadmobile.port.android.view.SelectClazzesDialogFragment;
import com.ustadmobile.port.android.view.SelectMultipleTreeDialogFragment;
import com.ustadmobile.port.android.view.SelectTwoDatesDialogFragment;
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
import com.ustadmobile.port.sharedse.view.DownloadDialogView;
import com.ustadmobile.port.sharedse.view.ReceiveCourseView;
import com.ustadmobile.port.sharedse.view.SendCourseView;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private boolean initRan = false;

    static {

        viewNameToAndroidImplMap.put(LoginView.VIEW_NAME, LoginDialogFragment.class);
        viewNameToAndroidImplMap.put(ContainerView.VIEW_NAME, ContainerActivity.class);
        viewNameToAndroidImplMap.put(CatalogView.VIEW_NAME, CatalogActivity.class);
        viewNameToAndroidImplMap.put(UserSettingsView.VIEW_NAME, UserSettingsActivity.class);
        viewNameToAndroidImplMap.put(SettingsDataUsageView.VIEW_NAME, SettingsDataUsageActivity.class);
        viewNameToAndroidImplMap.put(SettingsDataSyncListView.VIEW_NAME, SettingsDataSyncListActivity.class);
        viewNameToAndroidImplMap.put(BasePointView2.VIEW_NAME, BasePointActivity2.class);
        viewNameToAndroidImplMap.put(BasePointView.VIEW_NAME, BasePointActivity.class);
        viewNameToAndroidImplMap.put(AboutView.VIEW_NAME, AboutActivity.class);
        viewNameToAndroidImplMap.put(CatalogEntryView.VIEW_NAME, CatalogEntryActivity.class);
        viewNameToAndroidImplMap.put(UserSettingsView2.VIEW_NAME, UserSettingsActivity2.class);
        viewNameToAndroidImplMap.put(WelcomeView.VIEW_NAME, WelcomeDialogFragment.class);
        viewNameToAndroidImplMap.put(RegistrationView.VIEW_NAME, RegistrationDialogFragment.class);
        viewNameToAndroidImplMap.put(SendCourseView.VIEW_NAME, SendCourseDialogFragment.class);
        viewNameToAndroidImplMap.put(ReceiveCourseView.VIEW_NAME, ReceiveCourseDialogFragment.class);
        viewNameToAndroidImplMap.put(XapiPackageView.VIEW_NAME, XapiPackageActivity.class);
        viewNameToAndroidImplMap.put(AddFeedDialogView.VIEW_NAME, AddFeedDialogFragment.class);
        viewNameToAndroidImplMap.put(ScormPackageView.VIEW_NAME, ScormPackageActivity.class);
        viewNameToAndroidImplMap.put(H5PContentView.VIEW_NAME, H5PContentActivity.class);
        viewNameToAndroidImplMap.put(DownloadDialogView.VIEW_NAME, DownloadDialogFragment.class);
        viewNameToAndroidImplMap.put(ClassDetailView.VIEW_NAME, ClazzDetailActivity.class);
        viewNameToAndroidImplMap.put(ClassLogDetailView.VIEW_NAME, ClazzLogDetailActivity.class);
        viewNameToAndroidImplMap.put(PersonDetailView.VIEW_NAME, PersonDetailActivity.class);
        viewNameToAndroidImplMap.put(PersonEditView.VIEW_NAME, PersonEditActivity.class);
        viewNameToAndroidImplMap.put(PersonDetailEnrollClazzView.VIEW_NAME, PersonDetailEnrollClazzActivity.class);
        viewNameToAndroidImplMap.put(ClazzDetailEnrollStudentView.VIEW_NAME, ClazzDetailEnrollStudentActivity.class);
        viewNameToAndroidImplMap.put(SELSelectStudentView.VIEW_NAME, SELSelectStudentActivity.class);
        viewNameToAndroidImplMap.put(SELSelectConsentView.VIEW_NAME, SELSelectConsentActivity.class);
        viewNameToAndroidImplMap.put(SELEditView.VIEW_NAME, SELEditActivity.class);
        viewNameToAndroidImplMap.put(SELQuestionView.VIEW_NAME, SELQuestionActivity.class);
        viewNameToAndroidImplMap.put(SELQuestionDetailView.VIEW_NAME, SELQuestionDetailActivity.class);
        viewNameToAndroidImplMap.put(SELQuestionEditView.VIEW_NAME, SELQuestionEditActivity.class);
        viewNameToAndroidImplMap.put(SELRecognitionView.VIEW_NAME, SELRecognitionActivity.class);
        viewNameToAndroidImplMap.put(ClazzEditView.VIEW_NAME, ClazzEditActivity.class);
        viewNameToAndroidImplMap.put(AddScheduleDialogView.VIEW_NAME, AddScheduleDialogFragment.class);
        viewNameToAndroidImplMap.put(ClazzActivityEditView.VIEW_NAME, ClazzActivityEditActivity.class);
        viewNameToAndroidImplMap.put(AddActivityChangeDialogView.VIEW_NAME, AddActivityChangeDialogFragment.class);
        viewNameToAndroidImplMap.put(ReportEditView.VIEW_NAME, ReportEditActivity.class);
        viewNameToAndroidImplMap.put(SelectMultipleTreeDialogView.VIEW_NAME, SelectMultipleTreeDialogFragment.class);
        viewNameToAndroidImplMap.put(ReportSelectionView.VIEW_NAME, ReportSelectionActivity.class);
        viewNameToAndroidImplMap.put(SelectClazzesDialogView.VIEW_NAME, SelectClazzesDialogFragment.class);
        viewNameToAndroidImplMap.put(SelectAttendanceThresholdsDialogView.VIEW_NAME, SelectAttendanceThresholdsDialogFragment.class);
        viewNameToAndroidImplMap.put(SelectTwoDatesDialogView.VIEW_NAME, SelectTwoDatesDialogFragment.class);
        viewNameToAndroidImplMap.put(ReportOverallAttendanceView.VIEW_NAME, ReportOverallAttendanceActivity.class);
        viewNameToAndroidImplMap.put(ReportNumberOfDaysClassesOpenView.VIEW_NAME, ReportNumberOfDaysClassesOpenActivity.class);



    }

    /**
     * When using UstadMobile as a library in other apps, this method can be used to map custom
     * views so that they work with the go method.
     *
     * @param viewName A unique name e.g. as per the view interface VIEW_NAME
     * @param implementingClass The Activity or Fragment class that implements this view on Android
     */
    public static void mapView(String viewName, Class implementingClass) {
        viewNameToAndroidImplMap.put(viewName, implementingClass);
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

    private static final ContentTypePlugin[] SUPPORTED_CONTENT_TYPES = new ContentTypePlugin[] {
            new EpubTypePluginFs(), new ScormTypePluginFs(), new XapiPackageTypePluginFs(),
            new H5PContentTypeFs()};

    private ExecutorService bgExecutorService = Executors.newCachedThreadPool();

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

    private abstract static class UmCallbackAsyncTask<A, P, R> extends AsyncTask<A, P, R> {

        protected UmCallback<R> umCallback;

        protected Throwable error;

        private UmCallbackAsyncTask(UmCallback<R> callback) {
            this.umCallback = callback;
        }



        @Override
        protected void onPostExecute(R r) {
            if(error == null) {
                umCallback.onSuccess(r);
            }else {
                umCallback.onFailure(error);
            }
        }
    }

    /**
     * Simple async task to handle getting the setup file
     * Param 0 = boolean - true to zip, false otherwise
     */
    private static class GetSetupFileAsyncTask extends UmCallbackAsyncTask<Boolean, Void, String> {

        private Context context;

        private GetSetupFileAsyncTask(UmCallback doneCallback, Context context) {
            super(doneCallback);
            this.context = context;

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
        System.out.println("init4");

        if(!initRan) {
            File systemBaseDir = new File(getSystemBaseDir(context));
            if(!systemBaseDir.exists()) {
                if(systemBaseDir.mkdirs()){
                    l(UMLog.INFO, 0, "Created base system dir: " +
                            systemBaseDir.getAbsolutePath());
                }else {
                    l(UMLog.CRITICAL, 0, "Failed to created system base dir" +
                        systemBaseDir.getAbsolutePath());
                }
            }

            Context appContext = ((Context)context).getApplicationContext();

            Picasso.Builder picassoBuilder = new Picasso.Builder(appContext);
            picassoBuilder.addRequestHandler(new UmHttpCachePicassoRequestHandler(appContext));
            Picasso.setSingletonInstance(picassoBuilder.build());
            initRan = true;
        }

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
//            TODO: Handle users ROOM ORM style
//            xapiAgent = XapiAgentEndpoint.createOrUpdate(context, null, currentUsername,
//                    UMTinCanUtil.getXapiServer(context));
        }
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
        return new File(Environment.getExternalStorageDirectory(), getContentDirName(context))
                .getAbsolutePath();
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
    public String getSharedContentDir(Object context) {
        File extStorage = Environment.getExternalStorageDirectory();
        File ustadContentDir = new File(extStorage, getContentDirName(context));
        return ustadContentDir.getAbsolutePath();
    }

    @Override
    public String getUserContentDirectory(Object context, String username) {
        File userDir = new File(Environment.getExternalStorageDirectory(),
                getContentDirName(context) + "/user-" + username);
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

    @Override
    public void getAsset(final Object context, String path, final UmCallback<InputStream> callback) {
        if(path.startsWith("/")) {
            path = path.substring(1);
        }
        final String assetPath = path;

        bgExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    callback.onSuccess(((Context)context).getAssets().open(assetPath));
                }catch(IOException e) {
                    callback.onFailure(e);
                }
            }
        });
    }

    @Override
    public InputStream getAssetSync(Object context, String path) throws IOException {
        if(path.startsWith("/")) {
            path = path.substring(1);
        }

        return ((Context)context).getAssets().open(path);
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
    public long getBuildTimestamp(Object ctx) {
        Context context = (Context)ctx;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.lastUpdateTime;
        }catch(PackageManager.NameNotFoundException e) {
            l(UMLog.ERROR, 90, null, e);
        }

        return 0;
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
    public ContentTypePlugin[] getSupportedContentTypePlugins() {
        return SUPPORTED_CONTENT_TYPES;
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

    @Override
    public String convertTimeToReadableTime(long time) {
        Date date = new Date(time);
        Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
        return format.format(date);
    }

    @Override
    public void deleteEntries(Object context, List<String> entryIds, boolean recursive) {
        OpdsEntryStatusCacheDao entryStatusCacheDao = UmAppDatabase.getInstance(context).getOpdsEntryStatusCacheDao();
        List<String> entryIdsToDelete = entryIds;
        if(recursive) {
            entryIdsToDelete = new ArrayList<>();
            for(String entryId : entryIds) {
                entryIdsToDelete.add(entryId);
                entryIdsToDelete.addAll(entryStatusCacheDao.findAllKnownDescendantEntryIds(entryId));
            }
        }

        for(String descendantEntryId: entryIdsToDelete) {
            ContainerFileHelper.getInstance().deleteAllContainerFilesByEntryId(context, descendantEntryId);
        }


    }

    @Override
    public void deleteEntriesAsync(Object context, final List<String> entryIds, boolean recursive, UmCallback<Void> callback) {
        bgExecutorService.execute(() -> {
            deleteEntries(context, entryIds, recursive);
            callback.onSuccess(null);
        });

    }

}
