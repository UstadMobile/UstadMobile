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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Xml;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.toughra.ustadmobile.BuildConfig;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.AppConfig;
import com.ustadmobile.core.impl.NoAppFoundException;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmCallbackUtil;
import com.ustadmobile.core.impl.UmResultCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.view.AboutView;
import com.ustadmobile.core.view.AddReminderDialogView;
import com.ustadmobile.core.view.AddSaleProductToSaleCategoryView;
import com.ustadmobile.core.view.BasePoint2View;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.core.view.ChangePasswordView;
import com.ustadmobile.core.view.ComingSoonView;
import com.ustadmobile.core.view.ContentEntryDetailView;
import com.ustadmobile.core.view.ContentEntryListView;
import com.ustadmobile.core.view.DummyView;
import com.ustadmobile.core.view.EpubContentView;
import com.ustadmobile.core.view.H5PContentView;
import com.ustadmobile.core.view.Login2View;
import com.ustadmobile.core.view.OnBoardingView;
import com.ustadmobile.core.view.Register2View;
import com.ustadmobile.core.view.ReportOptionsDetailView;
import com.ustadmobile.core.view.ReportSalesLogDetailView;
import com.ustadmobile.core.view.ReportSalesPerformanceDetailView;
import com.ustadmobile.core.view.ReportTopLEsDetailView;
import com.ustadmobile.core.view.SaleDetailSignatureView;
import com.ustadmobile.core.view.SaleDetailView;
import com.ustadmobile.core.view.SaleItemDetailView;
import com.ustadmobile.core.view.SaleListSearchView;
import com.ustadmobile.core.view.SalePaymentDetailView;
import com.ustadmobile.core.view.SaleProductCategoryListView;
import com.ustadmobile.core.view.SaleProductDetailView;
import com.ustadmobile.core.view.ScormPackageView;
import com.ustadmobile.core.view.SelectDateRangeDialogView;
import com.ustadmobile.core.view.SelectLanguageDialogView;
import com.ustadmobile.core.view.SelectMultipleLocationTreeDialogView;
import com.ustadmobile.core.view.SelectMultipleProductTypeTreeDialogView;
import com.ustadmobile.core.view.SelectProducerView;
import com.ustadmobile.core.view.SelectSaleProductView;
import com.ustadmobile.core.view.UserProfileView;
import com.ustadmobile.core.view.VideoPlayerView;
import com.ustadmobile.core.view.WebChunkView;
import com.ustadmobile.core.view.XapiPackageContentView;
import com.ustadmobile.port.android.generated.MessageIDMap;
import com.ustadmobile.port.android.impl.http.UmHttpCachePicassoRequestHandler;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.android.view.AboutActivity;
import com.ustadmobile.port.android.view.AddReminderDialogFragment;
import com.ustadmobile.port.android.view.AddSaleProductToSaleCategoryActivity;
import com.ustadmobile.port.android.view.BasePoint2Activity;
import com.ustadmobile.port.android.view.BasePointActivity;
import com.ustadmobile.port.android.view.ChangePasswordActivity;
import com.ustadmobile.port.android.view.ComingSoonFragment;
import com.ustadmobile.port.android.view.ContentEntryDetailActivity;
import com.ustadmobile.port.android.view.ContentEntryListActivity;
import com.ustadmobile.port.android.view.DownloadDialogFragment;
import com.ustadmobile.port.android.view.DummyActivity;
import com.ustadmobile.port.android.view.EpubContentActivity;
import com.ustadmobile.port.android.view.H5PContentActivity;
import com.ustadmobile.port.android.view.Login2Activity;
import com.ustadmobile.port.android.view.OnBoardingActivity;
import com.ustadmobile.port.android.view.Register2Activity;
import com.ustadmobile.port.android.view.ReportOptionsDetailActivity;
import com.ustadmobile.port.android.view.ReportSalesLogDetailActivity;
import com.ustadmobile.port.android.view.ReportSalesPerformanceDetailActivity;
import com.ustadmobile.port.android.view.ReportTopLEsDetailActivity;
import com.ustadmobile.port.android.view.SaleDetailActivity;
import com.ustadmobile.port.android.view.SaleDetailSignatureActivity;
import com.ustadmobile.port.android.view.SaleItemDetailActivity;
import com.ustadmobile.port.android.view.SaleListSearchActivity;
import com.ustadmobile.port.android.view.SalePaymentDetailActivity;
import com.ustadmobile.port.android.view.SaleProductCategoryListActivity;
import com.ustadmobile.port.android.view.SaleProductDetailActivity;
import com.ustadmobile.port.android.view.ScormPackageActivity;
import com.ustadmobile.port.android.view.SelectDateRangeDialogFragment;
import com.ustadmobile.port.android.view.SelectLanguageDialogFragment;
import com.ustadmobile.port.android.view.SelectMultipleLocationTreeDialogFragment;
import com.ustadmobile.port.android.view.SelectMultipleProductTypeTreeDialogFragment;
import com.ustadmobile.port.android.view.SelectProducerActivity;
import com.ustadmobile.port.android.view.SelectSaleProductActivity;
import com.ustadmobile.port.android.view.UserProfileActivity;
import com.ustadmobile.port.android.view.VideoPlayerActivity;
import com.ustadmobile.port.android.view.WebChunkActivity;
import com.ustadmobile.port.android.view.XapiPackageContentActivity;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle;
import com.ustadmobile.port.sharedse.util.UmFileUtilSe;
import com.ustadmobile.port.sharedse.view.DownloadDialogView;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * Created by mike on 07/06/15.
 */
public class UstadMobileSystemImplAndroid extends UstadMobileSystemImplSE {

    public static final String TAG = "UstadMobileImplAndroid";

    public static final String APP_PREFERENCES_NAME = "UMAPP-PREFERENCES";


    public static final String TAG_DIALOG_FRAGMENT = "UMDialogFrag";

    public static final String ACTION_LOCALE_CHANGE = "com.ustadmobile.locale_change";

    public static final String PREFKEY_LANG = "lang";

    private static final int deviceStorageIndex = 0;

    private static final int sdCardStorageIndex = 1;

    /**
     * Map of view names to the activity class that is implementing them on Android
     *
     * @see UstadMobileSystemImplAndroid#go(String, Hashtable, Object)
     */
    public static final HashMap<String, Class> viewNameToAndroidImplMap = new HashMap<>();

    private boolean initRan = false;

    static {
        viewNameToAndroidImplMap.put(Login2View.VIEW_NAME, Login2Activity.class);
        viewNameToAndroidImplMap.put(EpubContentView.VIEW_NAME, EpubContentActivity.class);
        viewNameToAndroidImplMap.put(BasePointView.VIEW_NAME, BasePointActivity.class);
        viewNameToAndroidImplMap.put(AboutView.VIEW_NAME, AboutActivity.class);
        viewNameToAndroidImplMap.put(XapiPackageContentView.VIEW_NAME, XapiPackageContentActivity.class);
        viewNameToAndroidImplMap.put(ScormPackageView.VIEW_NAME, ScormPackageActivity.class);
        viewNameToAndroidImplMap.put(H5PContentView.VIEW_NAME, H5PContentActivity.class);
        viewNameToAndroidImplMap.put(DownloadDialogView.VIEW_NAME, DownloadDialogFragment.class);
        viewNameToAndroidImplMap.put(ContentEntryListView.VIEW_NAME, ContentEntryListActivity.class);
        viewNameToAndroidImplMap.put(ContentEntryDetailView.VIEW_NAME, ContentEntryDetailActivity.class);
        viewNameToAndroidImplMap.put(DummyView.VIEW_NAME, DummyActivity.class);
        viewNameToAndroidImplMap.put(OnBoardingView.VIEW_NAME, OnBoardingActivity.class);
        viewNameToAndroidImplMap.put(Register2View.VIEW_NAME, Register2Activity.class);
        viewNameToAndroidImplMap.put(WebChunkView.VIEW_NAME, WebChunkActivity.class);
        viewNameToAndroidImplMap.put(VideoPlayerView.VIEW_NAME, VideoPlayerActivity.class);
        //Goldozi
        viewNameToAndroidImplMap.put(BasePoint2View.VIEW_NAME, BasePoint2Activity.class);
        viewNameToAndroidImplMap.put(SaleDetailView.VIEW_NAME, SaleDetailActivity.class);
        viewNameToAndroidImplMap.put(SaleItemDetailView.VIEW_NAME, SaleItemDetailActivity.class);
        viewNameToAndroidImplMap.put(SelectProducerView.VIEW_NAME, SelectProducerActivity.class);
        viewNameToAndroidImplMap.put(SelectSaleProductView.VIEW_NAME, SelectSaleProductActivity.class);
        viewNameToAndroidImplMap.put(ComingSoonView.VIEW_NAME, ComingSoonFragment.class);
        viewNameToAndroidImplMap.put(SalePaymentDetailView.VIEW_NAME, SalePaymentDetailActivity.class);
        viewNameToAndroidImplMap.put(UserProfileView.VIEW_NAME, UserProfileActivity.class);
        viewNameToAndroidImplMap.put(ChangePasswordView.VIEW_NAME, ChangePasswordActivity.class);
        viewNameToAndroidImplMap.put(SelectLanguageDialogView.VIEW_NAME, SelectLanguageDialogFragment.class);
        viewNameToAndroidImplMap.put(SaleProductDetailView.VIEW_NAME, SaleProductDetailActivity.class);
        viewNameToAndroidImplMap.put(SaleProductCategoryListView.VIEW_NAME, SaleProductCategoryListActivity.class);
        viewNameToAndroidImplMap.put(AddSaleProductToSaleCategoryView.VIEW_NAME, AddSaleProductToSaleCategoryActivity.class);
        viewNameToAndroidImplMap.put(SaleListSearchView.VIEW_NAME, SaleListSearchActivity.class);
        viewNameToAndroidImplMap.put(SaleDetailSignatureView.VIEW_NAME, SaleDetailSignatureActivity.class);
        viewNameToAndroidImplMap.put(SelectDateRangeDialogView.VIEW_NAME, SelectDateRangeDialogFragment.class);
        viewNameToAndroidImplMap.put(AddReminderDialogView.VIEW_NAME, AddReminderDialogFragment.class);
        viewNameToAndroidImplMap.put(ReportOptionsDetailView.VIEW_NAME, ReportOptionsDetailActivity.class);
        viewNameToAndroidImplMap.put(SelectMultipleLocationTreeDialogView.VIEW_NAME, SelectMultipleLocationTreeDialogFragment.class);
        viewNameToAndroidImplMap.put(ReportSalesPerformanceDetailView.VIEW_NAME, ReportSalesPerformanceDetailActivity.class);
        viewNameToAndroidImplMap.put(ReportSalesLogDetailView.VIEW_NAME, ReportSalesLogDetailActivity.class);
        viewNameToAndroidImplMap.put(ReportTopLEsDetailView.VIEW_NAME, ReportTopLEsDetailActivity.class);
        viewNameToAndroidImplMap.put(SelectMultipleProductTypeTreeDialogView.VIEW_NAME, SelectMultipleProductTypeTreeDialogFragment.class);


    }

    /**
     * When using UstadMobile as a library in other apps, this method can be used to map custom
     * views so that they work with the go method.
     *
     * @param viewName          A unique name e.g. as per the view interface VIEW_NAME
     * @param implementingClass The Activity or Fragment class that implements this view on Android
     */
    @SuppressWarnings("unused")
    public static void mapView(String viewName, Class implementingClass) {
        viewNameToAndroidImplMap.put(viewName, implementingClass);
    }

    private SharedPreferences appPreferences;

    private UMLogAndroid logger;

    private ExecutorService bgExecutorService = Executors.newCachedThreadPool();

    private abstract static class UmCallbackAsyncTask<A, P, R> extends AsyncTask<A, P, R> {

        protected UmCallback<R> umCallback;

        protected Throwable error;

        private UmCallbackAsyncTask(UmCallback<R> callback) {
            this.umCallback = callback;
        }


        @Override
        protected void onPostExecute(R r) {
            if (error == null) {
                umCallback.onSuccess(r);
            } else {
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
            File apkFile = new File(context.getApplicationInfo().sourceDir);
            //TODO: replace this with something from appconfig.properties
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

            String baseName = impl.getAppConfigString(AppConfig.KEY_APP_BASE_NAME, "", context) + "-" +
                    impl.getVersion(context);


            FileInputStream apkFileIn = null;
            Context ctx = (Context) context;
            File outDir = new File(ctx.getFilesDir(), "shared");
            if (!outDir.isDirectory())
                outDir.mkdirs();

            if (booleans[0]) {
                ZipOutputStream zipOut = null;
                File outZipFile = new File(outDir, baseName + ".zip");
                try {
                    zipOut = new ZipOutputStream(new FileOutputStream(outZipFile));
                    zipOut.putNextEntry(new ZipEntry(baseName + ".apk"));
                    apkFileIn = new FileInputStream(apkFile);
                    UMIOUtils.readFully(apkFileIn, zipOut, 1024);
                    zipOut.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    UMIOUtils.closeOutputStream(zipOut);
                    UMIOUtils.closeInputStream(apkFileIn);
                }

                return outZipFile.getAbsolutePath();
            } else {
                FileOutputStream fout = null;
                File outApkFile = new File(outDir, baseName + ".apk");
                try {
                    apkFileIn = new FileInputStream(apkFile);
                    fout = new FileOutputStream(outApkFile);
                    UMIOUtils.readFully(apkFileIn, fout, 1024);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    UMIOUtils.closeInputStream(apkFileIn);
                    UMIOUtils.closeOutputStream(fout);
                }

                return outApkFile.getAbsolutePath();
            }
        }
    }


    /**
     */
    public UstadMobileSystemImplAndroid() {
        logger = new UMLogAndroid();
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

        if (!initRan) {
            File systemBaseDir = new File(getSystemBaseDir(context));
            if (!systemBaseDir.exists()) {
                if (systemBaseDir.mkdirs()) {
                    l(UMLog.INFO, 0, "Created base system dir: " +
                            systemBaseDir.getAbsolutePath());
                } else {
                    l(UMLog.CRITICAL, 0, "Failed to created system base dir" +
                            systemBaseDir.getAbsolutePath());
                }
            }

            Context appContext = ((Context) context).getApplicationContext();

            Picasso.Builder picassoBuilder = new Picasso.Builder(appContext);
            picassoBuilder.addRequestHandler(new UmHttpCachePicassoRequestHandler(appContext));
            Picasso.setSingletonInstance(picassoBuilder.build());
            initRan = true;
        }

        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    WebView.setWebContentsDebuggingEnabled(true);
                }
            });
        }

    }

    @Override
    public void setLocale(String locale, Object context) {
        super.setLocale(locale, context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences((Context) context);
        prefs.edit().putString(PREFKEY_LANG, locale).apply();
    }

    @Override
    public String getLocale(Object context) {
        String locale = super.getLocale(context);
        if (locale == null) {
            locale = PreferenceManager.getDefaultSharedPreferences((Context) context).getString(
                    PREFKEY_LANG, "");
            super.setLocale(locale, context);
        }

        return locale;
    }

    @Override
    public String getString(int messageCode, Object context) {
        Integer androidId = MessageIDMap.ID_MAP.get(messageCode);
        if (androidId != null) {
            return ((Context) context).getResources().getString(androidId);
        } else {
            return null;
        }
    }


    /**
     * To be called by activities as the first matter of business in the onCreate method
     * <p>
     * Will bind to the HTTP service
     * <p>
     * TODO: This should really be handleContextCreate : This should be used by background services as well
     *
     * @param mContext
     */
    public void handleActivityCreate(Activity mContext, Bundle savedInstanceState) {
        init(mContext);
    }

    public void handleActivityDestroy(Activity mContext) {

    }


    public void go(String viewName, Hashtable args, Object context, int flags) {
        Class androidImplClass = viewNameToAndroidImplMap.get(viewName);
        Context ctx = (Context) context;
        Bundle argsBundle = UMAndroidUtil.hashtableToBundle(args);

        if (androidImplClass == null) {
            Log.wtf(UMLogAndroid.LOGTAG, "No activity for " + viewName + " found");
            Toast.makeText(ctx, "ERROR: No Activity found for view: " + viewName,
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (DialogFragment.class.isAssignableFrom(androidImplClass)) {
            String toastMsg = null;
            try {
                DialogFragment dialog = (DialogFragment) androidImplClass.newInstance();
                if (args != null)
                    dialog.setArguments(argsBundle);
                AppCompatActivity activity = (AppCompatActivity) context;
                dialog.show(activity.getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
            } catch (InstantiationException e) {
                Log.wtf(UMLogAndroid.LOGTAG, "Could not instantiate dialog", e);
                toastMsg = "Dialog error: " + e.toString();
            } catch (IllegalAccessException e2) {
                Log.wtf(UMLogAndroid.LOGTAG, "Could not instantiate dialog", e2);
                toastMsg = "Dialog error: " + e2.toString();
            }

            if (toastMsg != null) {
                Toast.makeText(ctx, toastMsg, Toast.LENGTH_LONG).show();
            }
        } else {
            Intent startIntent = new Intent(ctx, androidImplClass);
            if (ctx instanceof Activity) {
                String referrer = "";
                if (((Activity) ctx).getIntent().getExtras() != null) {
                    referrer = ((Activity) ctx).getIntent().getExtras().getString(ARG_REFERRER, "");
                }

                if ((flags & GO_FLAG_CLEAR_TOP) > 0) {
                    referrer = UMFileUtil.clearTopFromReferrerPath(viewName, args,
                            referrer);
                } else {
                    referrer += "/" + viewName + "?" + UMFileUtil.hashtableToQueryString(args);
                }

                startIntent.putExtra(ARG_REFERRER, referrer);
            }
            startIntent.setFlags(flags);
            if (args != null)
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
        Context ctx = (Context) context;
        File cacheDir = ctx.getCacheDir();
        return cacheDir.getAbsolutePath();

    }

    /**
     * Method to accomplish the surprisingly tricky task of finding the external SD card (if this
     * device has one)
     * <p>
     * Approach borrowed from:
     * http://pietromaggi.com/2014/10/19/finding-the-sdcard-path-on-android-devices/
     * <p>
     * Note: Approaches that use a mount based way of looking at things are returning paths that
     * actually are not actually usable.  Therefor: use the approach based on environment variables.
     *
     * @return A HashSet of paths to any external memory cards mounted
     */
    public String[] findRemovableStorage() {
        String secondaryStorage = System.getenv("SECONDARY_STORAGE");
        if (secondaryStorage == null || secondaryStorage.length() == 0) {
            secondaryStorage = System.getenv("EXTERNAL_SDCARD_STORAGE");
        }

        if (secondaryStorage != null) {
            return secondaryStorage.split(":");
        } else {
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


    public URLConnection openConnection(URL url) throws IOException {
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
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        final String assetPath = path;

        bgExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    callback.onSuccess(((Context) context).getAssets().open(assetPath));
                } catch (IOException e) {
                    callback.onFailure(e);
                }
            }
        });
    }

    @Override
    public InputStream getAssetSync(Object context, String path) throws IOException {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        return ((Context) context).getAssets().open(path);
    }


    private SharedPreferences getAppSharedPreferences(Context context) {
        if (appPreferences == null) {
            appPreferences = context.getSharedPreferences(APP_PREFERENCES_NAME,
                    Context.MODE_PRIVATE);
        }
        return appPreferences;
    }

    @Override
    public String getAppPref(String key, Object context) {
        return getAppSharedPreferences((Context) context).getString(key, null);
    }

    public void setAppPref(String key, String value, Object context) {
        SharedPreferences prefs = getAppSharedPreferences((Context) context);
        SharedPreferences.Editor editor = prefs.edit();
        if (value != null) {
            editor.putString(key, value);
        } else {
            editor.remove(key);
        }
        editor.commit();
    }


    @Override
    public XmlSerializer newXMLSerializer() {
        return Xml.newSerializer();
    }

    @Override
    public UMLog getLogger() {
        return logger;
    }


    @Override
    public String getMimeTypeFromExtension(String extension) {
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        if (mimeType != null) {
            return mimeType;
        } else {
            return super.getMimeTypeFromExtension(extension);
        }
    }

    @Override
    public String getExtensionFromMimeType(String mimeType) {
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        if (extension != null) {
            return extension;
        } else {
            return super.getExtensionFromMimeType(mimeType);
        }
    }


    @Override
    public String getVersion(Object ctx) {
        Context context = (Context) ctx;
        String versionInfo = null;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionInfo = 'v' + pInfo.versionName + " (#" + pInfo.versionCode + ')';
        } catch (PackageManager.NameNotFoundException e) {
            l(UMLog.ERROR, 90, null, e);
        }
        return versionInfo;
    }

    @Override
    public long getBuildTimestamp(Object ctx) {
        Context context = (Context) ctx;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.lastUpdateTime;
        } catch (PackageManager.NameNotFoundException e) {
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
    public NetworkManagerBle getNetworkManagerBle() {
        return null;
    }

    @Override
    public void getAppSetupFile(Object context, boolean zip, UmCallback callback) {
        GetSetupFileAsyncTask setupFileAsyncTask = new GetSetupFileAsyncTask(callback,
                (Context) context);
        setupFileAsyncTask.execute(zip);
    }

    @Override
    public String getManifestPreference(String key, Object context) {
        try {
            Context ctx = (Context) context;
            ApplicationInfo ai2 = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(),
                    PackageManager.GET_META_DATA);
            Bundle metaData = ai2.metaData;
            if (metaData != null) {
                return metaData.getString(key);
            }
        } catch (PackageManager.NameNotFoundException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, UMLog.ERROR, key, e);
        }

        return null;
    }

    @Override
    public void openFileInDefaultViewer(Object context, String path, String mimeType, UmCallback<Void> callback) {
        Context ctx = (Context) context;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = FileProvider.getUriForFile(ctx, BuildConfig.APPLICATION_ID, new File(path));
        if (mimeType == null || mimeType.isEmpty()) {
            mimeType = "*/*";
        }
        intent.setDataAndType(uri, mimeType);
        PackageManager pm = ctx.getPackageManager();
        if (intent.resolveActivity(pm) != null) {
            ctx.startActivity(intent);
            UmCallbackUtil.onSuccessIfNotNull(callback, null);
        } else {
            UmCallbackUtil.onFailIfNotNull(callback,
                    new NoAppFoundException("No activity found for mimetype", mimeType));
        }
    }

    @Override
    public void getStorageDirs(Object context, UmResultCallback<List<UMStorageDir>> callback) {
        new Thread(() -> {
            List<UMStorageDir> dirList = new ArrayList<>();
            File[] storageOptions = ContextCompat.getExternalFilesDirs((Context) context, null);
            String contentDirName = getContentDirName(context);

            File umDir = new File(storageOptions[deviceStorageIndex], contentDirName);
            if (!umDir.exists()) umDir.mkdirs();
            dirList.add(new UMStorageDir(umDir.getAbsolutePath(),
                    getString(MessageID.phone_memory, context), true,
                    true, false, UmFileUtilSe.canWriteFileInDir(umDir)));

            if (storageOptions.length > 1) {
                File sdCardStorage = storageOptions[sdCardStorageIndex];
                umDir = new File(sdCardStorage, contentDirName);
                if (!umDir.exists()) umDir.mkdirs();
                dirList.add(new UMStorageDir(umDir.getAbsolutePath(),
                        getString(MessageID.memory_card, context), true,
                        true, false, UmFileUtilSe.canWriteFileInDir(umDir)));
            }

            callback.onDone(dirList);
        }).start();
    }
}
