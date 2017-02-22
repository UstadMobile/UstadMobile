package com.ustadmobile.port.android.impl.http;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.nanolrs.http.NanoLrsHttpd;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;

import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;
import com.ustadmobile.port.sharedse.impl.http.MountedZipHandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class HTTPService extends Service {

    private final IBinder mBinder = new HTTPBinder();

    private int DEFAULT_PORT = 8001;

    private EmbeddedHTTPD httpd;

    public static int idcount = 0;

    private int id;

    private String assetsPath;

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
        Log.i(UstadMobileSystemImplAndroid.TAG, "Create HTTP P2PServiceAndroid " + this);
        httpd = new EmbeddedHTTPD(DEFAULT_PORT);
        assetsPath = "/assets-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + '/';
        httpd.addRoute(assetsPath +"(.)+",
                AndroidAssetsHandler.class, this);

        NanoLrsHttpd.mountXapiEndpointsOnServer(httpd, this, "/xapi/");

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
        Log.i(UstadMobileSystemImplAndroid.TAG, "Destroy HTTP P2PServiceAndroid");
        httpd.stop();
        httpd = null;
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

    /**
     * The base URL and port of the server: e.g.
     * http://127.0.0.1:PORT/
     *
     * @return Base URL and port of server as above
     */
    public String getBaseURL() {
        return "http://127.0.0.1:" + DEFAULT_PORT  + "/";
    }

    public String getAssetsBaseURL() {
        return UMFileUtil.joinPaths(new String[]{getBaseURL(), assetsPath});
    }


    /**
     * Get the EmbeddedHTTPD instance
     *
     * @return
     */
    public EmbeddedHTTPD getHttpd() {
        return httpd;
    }


    /**
     * Mount a Zip File to the http server.  Optionally specify a preferred mount point (useful if
     * the activity is being created from a saved state)
     *
     * @param zipPath Path to the zip that should be mounted (mandatory)
     * @param mountName Directory name that this should be mounted as e.g. something.epub-timestamp
     *
     * @return The mountname that was used - the ocntent will then be accessible on getZipMountURL()/return value
     */
    public String mountZIP(String zipPath, String mountName) {
        UstadMobileSystemImpl.l(UMLog.INFO, 371, "Mount zip " + zipPath + " on service "
                + this + "httpd server = " + httpd);
        String extension = UMFileUtil.getExtension(zipPath);
        HashMap<String, List<MountedZipHandler.MountedZipFilter>> filterMap = null;

        if(extension != null && extension.endsWith("epub")) {
            filterMap = new HashMap<>();
            List<MountedZipHandler.MountedZipFilter> xhtmlFilterList = new ArrayList<>();
            MountedZipHandler.MountedZipFilter autoplayFilter = new MountedZipHandler.MountedZipFilter(
                    Pattern.compile("autoplay(\\s?)=(\\s?)([\"'])autoplay", Pattern.CASE_INSENSITIVE),
                    "data-autoplay$1=$2$3autoplay");
            xhtmlFilterList.add(autoplayFilter);
            filterMap.put("xhtml", xhtmlFilterList);
        }


        mountName = httpd.mountZip(zipPath, mountName, filterMap);
        return mountName;
    }


    public void ummountZIP(String mountName) {
        httpd.unmountZip(mountName);
    }


}
