package com.ustadmobile.port.android.p2p;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class P2PServiceAndroid extends Service {

    private P2PManagerAndroid p2PManagerAndroid;

    public P2PServiceAndroid() {
    }

    @Override
    public void onCreate() {
        //create a new P2PManagerAndroid and start it
        p2PManagerAndroid=new P2PManagerAndroid(this);
        p2PManagerAndroid.start();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        //stop the p2p manager if it's running
        p2PManagerAndroid.stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
