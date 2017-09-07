package com.ustadmobile.port.android.netwokmanager;

/**
 * Created by mike on 9/6/17.
 */

public interface INsdHelperAndroid {

    void startNSDiscovery();

    void stopNSDiscovery();

    void registerNSDService();

    void unregisterNSDService();

    boolean isDiscoveringNetworkService();

    void onDestroy();
}
