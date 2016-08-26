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

import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HTTPService extends Service {

    private final IBinder mBinder = new HTTPBinder();

    private int DEFAULT_PORT = 8001;

    private EmbeddedHTTPD httpd;

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

    /**
     * The path where zips (e.g. epubs) are mounted to e.g.
     * http://127.0.0.1:PORT/mount/
     *
     * @return Zip mount path as above including trailing slash
     */
    public String getZipMountURL() {
        return getBaseURL() + "mount/";
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

        mountName = httpd.mountZip(zipPath, mountName);

        String extension = UMFileUtil.getExtension(zipPath);
        if(extension != null && extension.endsWith("epub")) {
            addFilter(mountName, "xhtml", "autoplay(\\s?)=(\\s?)([\"'])autoplay",
                    "data-autoplay$1=$2$3autoplay");
            addFilter(mountName, "xhtml", "&(\\s)", "&amp;$1");
        }

        try {
            return URLEncoder.encode(mountName, "UTF-8");
        }catch(IOException e) {
            //this will only ever happen if UTF-8 is unsupported by the system - which is never going to happen
            UstadMobileSystemImpl.l(UMLog.CRITICAL, 20, null, e);
        }


        return null;
    }


    public void ummountZIP(String mountName) {
        httpd.unmountZip(mountName);
    }


    public void addFilter(String mountPath, String extension, String regex, String replacement) {
        httpd.addFilter(mountPath, extension, regex, replacement);
    }



}
