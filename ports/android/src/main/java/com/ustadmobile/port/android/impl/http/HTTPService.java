package com.ustadmobile.port.android.impl.http;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.ustadmobile.core.util.UMFileUtil;

import java.io.IOException;

public class HTTPService extends Service {

    private final IBinder mBinder = new HTTPBinder();

    private int DEFAULT_PORT = 8001;

    private EmbeddedHTTPD httpd;

    public HTTPService() {
    }

    @Override
    public void onCreate() {
        httpd = new EmbeddedHTTPD(DEFAULT_PORT);
        try {
            httpd.start();
        } catch (IOException e) {
            e.printStackTrace();
            //throw new RuntimeException(e);
        }
    }

    @Override
    public void onDestroy() {
        httpd.stop();
        httpd = null;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new HTTPBinder();
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
        String zipName = UMFileUtil.getFilename(zipPath);
        httpd.mountZip(zipName, zipPath);
        return getBaseURL() + "mount/" + zipName;
    }

    public void addFilter(String mountPath, String extension, String regex, String replacement) {
        httpd.addFilter(mountPath, extension, regex, replacement);
    }



}
