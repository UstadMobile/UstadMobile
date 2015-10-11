package com.ustadmobile.port.android.impl.http;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;

public class HTTPService extends Service {

    private final IBinder mBinder = new HTTPBinder();

    private int DEFAULT_PORT = 8001;

    private EmbeddedHTTPD httpd;

    private HashMap<String, String> mountedZipMap;


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
            Log.i(UstadMobileSystemImplAndroid.TAG, "Started HTTP server");
        } catch (IOException e) {
            Log.e(UstadMobileSystemImplAndroid.TAG, "Error starting http server", e);
            e.printStackTrace();
        }


    }


    @Override
    public void onDestroy() {
        Log.i(UstadMobileSystemImplAndroid.TAG, "Destroy HTTP Service");
        //saveMountedZips();
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

    public String mountZIP(String zipPath) {
        HashMap<String, String> zipMap = mountedZipMap;

        UstadMobileSystemImpl.l(UMLog.INFO, 371, "Mount zip " + zipPath + " on service "
                + this + "httpd server = " + httpd);
        String zipName = UMFileUtil.getFilename(zipPath);
        httpd.mountZip(zipName, zipPath);

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
        httpd.unmountZip(mountedPath);
    }

    public void addFilter(String mountPath, String extension, String regex, String replacement) {
        httpd.addFilter(mountPath, extension, regex, replacement);
    }



}
