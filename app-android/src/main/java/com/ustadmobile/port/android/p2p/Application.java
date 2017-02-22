package com.ustadmobile.port.android.p2p;

import android.content.Context;


public class Application extends android.app.Application {

    private static Application mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
    }

    public static synchronized Application getInstance() {
        return mInstance;
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

    }

    public void setNodeChangeListener(P2PListChangeReceiver.NodeListChangeReceiverListener listener) {
        P2PListChangeReceiver.nodeListChangeReceiverListener = listener;
    }
}
