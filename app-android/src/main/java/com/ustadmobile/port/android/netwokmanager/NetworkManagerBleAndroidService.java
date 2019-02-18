package com.ustadmobile.port.android.netwokmanager;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.ustadmobile.port.android.netwokmanager.DownloadNotificationService.ACTION_START_FOREGROUND_SERVICE;
import static com.ustadmobile.port.android.netwokmanager.DownloadNotificationService.GROUP_SUMMARY_ID;
import static com.ustadmobile.port.android.netwokmanager.DownloadNotificationService.JOB_ID_TAG;

/**
 * Wrapper class for NetworkManagerBle. A service is required as this encapsulates
 * peer discovery processes and the http server that should continue running
 * regardless of which activity is active.
 *
 * @author Kileha3
 */
public class NetworkManagerBleAndroidService extends Service {

    private final IBinder mBinder = new NetworkManagerBleAndroidService.LocalServiceBinder();

    private AtomicReference<NetworkManagerAndroidBle> managerAndroidBleRef = new AtomicReference<>();

    private AtomicBoolean mHttpServiceBound = new AtomicBoolean(false);

    private AtomicReference<EmbeddedHTTPD> httpdRef = new AtomicReference<>();

    private AtomicBoolean mHttpDownloadServiceActive = new AtomicBoolean(false);

    private UmLiveData<Boolean> activeDownloadJobData = null;

    private UmObserver<Boolean> activeDownloadJobObserver;

    private ServiceConnection mHttpdServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mHttpServiceBound.set(true);
            httpdRef.set(((EmbeddedHttpdService.LocalServiceBinder)service).getHttpd());
            handleHttpdServiceBound();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mHttpServiceBound.set(false);
        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent serviceIntent = new Intent(getApplicationContext(), EmbeddedHttpdService.class);
        bindService(serviceIntent, mHttpdServiceConnection, Context.BIND_AUTO_CREATE);

        activeDownloadJobData = UmAppDatabase.getInstance(this)
                .getDownloadJobDao().getAnyActiveDownloadJob();
        activeDownloadJobObserver = this::handleActiveJob;
        activeDownloadJobData.observeForever(activeDownloadJobObserver);
    }

    private void handleHttpdServiceBound() {
        NetworkManagerAndroidBle managerAndroidBle = new NetworkManagerAndroidBle(this,
                httpdRef.get());
        managerAndroidBleRef.set(managerAndroidBle);
        managerAndroidBle.onCreate();
    }

    private void handleActiveJob(boolean anyActivityJob){
        if(!mHttpDownloadServiceActive.get() && anyActivityJob){
            Intent serviceIntent = new Intent(getApplicationContext(), DownloadNotificationService.class);
            serviceIntent.setAction(ACTION_START_FOREGROUND_SERVICE);
            serviceIntent.putExtra(JOB_ID_TAG,GROUP_SUMMARY_ID);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }

        mHttpDownloadServiceActive.set(anyActivityJob);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mHttpServiceBound.get())
            unbindService(mHttpdServiceConnection);

        if(mHttpDownloadServiceActive.get())
            activeDownloadJobData.removeObserver(activeDownloadJobObserver);

        NetworkManagerAndroidBle managerAndroidBle = managerAndroidBleRef.get();
        if(managerAndroidBle != null)
            managerAndroidBle.onDestroy();
    }

    /**
     * @return Running instance of the NetworkManagerBle
     */
    public NetworkManagerAndroidBle getNetworkManagerBle(){
        return managerAndroidBleRef.get();
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we won't be dealing with IPC.
     */
    public class LocalServiceBinder extends Binder {
        public NetworkManagerBleAndroidService getService() {
            return NetworkManagerBleAndroidService.this;
        }

    }

}
