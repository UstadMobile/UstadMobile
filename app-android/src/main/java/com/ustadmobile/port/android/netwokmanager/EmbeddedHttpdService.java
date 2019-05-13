package com.ustadmobile.port.android.netwokmanager;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.impl.http.AndroidAssetsHandler;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;

import java.io.IOException;

public class EmbeddedHttpdService extends Service {

    private EmbeddedHTTPD httpd;

    private LocalServiceBinder mBinder = new LocalServiceBinder();

    public static final String ANDROID_ASSETS_PATH = "/android-assets/";

    public class LocalServiceBinder extends Binder {

        public EmbeddedHTTPD getHttpd(){
            return httpd;
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        httpd = new EmbeddedHTTPD(0, getApplicationContext());
        httpd.addRoute(ANDROID_ASSETS_PATH +"(.)+",  AndroidAssetsHandler.class,
                getApplicationContext());
        try {
            httpd.start();
        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.CRITICAL, 0, "Could not start httpd server");
            throw new RuntimeException("Could not start httpd server", e);
        }
    }

    @Override
    public void onDestroy() {
        httpd.stop();
        super.onDestroy();
    }
}
