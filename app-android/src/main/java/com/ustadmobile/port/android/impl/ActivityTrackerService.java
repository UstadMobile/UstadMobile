package com.ustadmobile.port.android.impl;

import android.app.Service;
import android.arch.lifecycle.LifecycleObserver;
import android.content.Intent;
import android.os.IBinder;

public class ActivityTrackerService extends Service implements LifecycleObserver {

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
