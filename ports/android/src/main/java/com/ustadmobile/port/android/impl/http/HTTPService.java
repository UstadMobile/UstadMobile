package com.ustadmobile.port.android.impl.http;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class HTTPService extends Service {

    private final IBinder mBinder = new HTTPBinder();

    private int DEFAULT_PORT = 8001;

    private EmbeddedHTTPD httpd;

    /**
     * This is a horrible workaround for making the android tests complete.  In reality one
     * service is active throughout the lifespan of the whole app.  In testing different services
     * come and go.
     */
    private static EmbeddedHTTPD lastStartedHTTPD;

    private HashMap<String, String> mountedZipMap;

    private static HashMap<String, String> lastMountedZipMap;


    /**
     * BroadCastReceiver that listens for the download complete message
     */
    private BroadcastReceiver downloadCompleteReceiver;

    /**
     * IntentFilter used to when registering for updates
     */
    private IntentFilter downloadCompleteIntentFilter;

    private WeakHashMap<Long, UstadMobileSystemImplAndroid.DownloadJob> downloadJobMap;

    public static int idcount = 0;

    private int id;

    public HTTPService() {
        id = idcount;
        idcount++;
    }

    @Override
    public String toString() {
        return "HTTPService: id " + id;
    }

    @Override
    public void onCreate() {
        Log.i(UstadMobileSystemImplAndroid.TAG, "Create HTTP Service " + this);
        httpd = new EmbeddedHTTPD(DEFAULT_PORT);
        mountedZipMap = new HashMap<>();

        try {
            httpd.start();

            lastStartedHTTPD = httpd;
            lastMountedZipMap = mountedZipMap;
            Log.i(UstadMobileSystemImplAndroid.TAG, "Started HTTP server");
        } catch (IOException e) {
            Log.e(UstadMobileSystemImplAndroid.TAG, "Error starting http server", e);
            e.printStackTrace();
        }

        downloadJobMap = new WeakHashMap<>();

        //register to receive download manager finished events
        downloadCompleteIntentFilter =
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        downloadCompleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long downloadID =intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
                UstadMobileSystemImplAndroid.DownloadJob job = downloadJobMap.get(downloadID);
                if(job != null) {
                    job.cleanup();
                }
            }
        };
        registerReceiver(downloadCompleteReceiver, downloadCompleteIntentFilter);
    }


    public void watchDownloadJob(long downloadID, UstadMobileSystemImplAndroid.DownloadJob job) {
        downloadJobMap.put(downloadID, job);
    }

    @Override
    public void onDestroy() {
        Log.i(UstadMobileSystemImplAndroid.TAG, "Destroy HTTP Service");
        //saveMountedZips();
        unregisterReceiver(downloadCompleteReceiver);
        httpd.stop();
        httpd = null;


        mountedZipMap = null;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class HTTPBinder extends Binder {
        public HTTPService getService() {
            return HTTPService.this;
        }
    }

    public String getBaseURL() {
        return "http://127.0.0.1:" + DEFAULT_PORT  + "/";
    }

    public EmbeddedHTTPD getActiveServer() {
        if(httpd != null) {
            return httpd;
        }else {
            return lastStartedHTTPD;
        }
    }

    private HashMap<String, String> getActiveMap() {
        if(mountedZipMap != null) {
            return mountedZipMap;
        }else {
            return lastMountedZipMap;
        }
    }

    public String mountZIP(String zipPath) {
        EmbeddedHTTPD server = httpd;
        HashMap<String, String> zipMap = mountedZipMap;
        if(server == null) {
            server = lastStartedHTTPD;
            zipMap = lastMountedZipMap;
        }
        UstadMobileSystemImpl.l(UMLog.INFO, 371, "Mount zip " + zipPath + " on service "
                + this + "httpd server = " + server);
        String zipName = UMFileUtil.getFilename(zipPath);
        server.mountZip(zipName, zipPath);

        String extension = UMFileUtil.getExtension(zipPath);
        if(extension != null && extension.endsWith("epub")) {
            addFilter(zipName, "xhtml", "autoplay(\\s?)=(\\s?)([\"'])autoplay",
                    "data-autoplay$1=$2$3autoplay");
            addFilter(zipName, "xhtml", "&(\\s)", "&amp;$1");
        }

        String openedPath = null;
        try {
            openedPath =getBaseURL() + "mount/" + URLEncoder.encode(zipName, "UTF-8");
            zipMap.put(openedPath, zipName);
        }catch(IOException e) {
            //this will only ever happen if UTF-8 is unsupported by the system - which is never going to happen
            UstadMobileSystemImpl.l(UMLog.CRITICAL, 20, null, e);
        }


        return openedPath;
    }

    public void ummountZIP(String openedPath) {
        String mountedPath = mountedZipMap.get(openedPath);
        getActiveServer().unmountZip(mountedPath);
    }

    public void addFilter(String mountPath, String extension, String regex, String replacement) {
        getActiveServer().addFilter(mountPath, extension, regex, replacement);
    }



}
