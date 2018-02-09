package com.ustadmobile.port.android.netwokmanager;

import android.annotation.TargetApi;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import static com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroid.normalizeAndroidWifiSsid;

/**
 * On Android SDK 21+ when a wifi connection is established no traffic will be routed through it
 * if there is no Internet connection. It is required to obtain the Network object, and then use
 * it's method to open url connections or make a socket factory.
 *
 * Created by mike on 2/8/18.
 */
@TargetApi(21)
public class NetworkRequestHelper extends ConnectivityManager.NetworkCallback {

    private ConnectivityManager connectivityManager;

    private NetworkManagerAndroid manager;

    private String targetSsid;

    public NetworkRequestHelper(NetworkManagerAndroid manager, ConnectivityManager connectivityManager,
                                String targetSsid) {
        this.connectivityManager = connectivityManager;
        this.manager = manager;
        this.targetSsid = targetSsid;
    }

    @Override
    public void onAvailable(Network network) {
        super.onAvailable(network);
        NetworkInfo networkInfo= connectivityManager.getNetworkInfo(network);
        String ssid = normalizeAndroidWifiSsid(networkInfo.getExtraInfo());
        if(ssid != null && targetSsid.equals(ssid)) {
            UstadMobileSystemImpl.l(UMLog.INFO, 0, "NetworkRequestHelper: onAvailable" + network);
            manager.setWifiSocketFactory(network.getSocketFactory());

            //TODO: this could be replaced with a method reference
            manager.setWifiUrlConnectionOpener(new AndroidNetworkURLConnectionOpener(network));
            connectivityManager.unregisterNetworkCallback(this);
        }
    }

}
