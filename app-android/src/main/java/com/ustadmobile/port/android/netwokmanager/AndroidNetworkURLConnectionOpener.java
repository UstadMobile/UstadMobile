package com.ustadmobile.port.android.netwokmanager;

import android.annotation.TargetApi;
import android.net.Network;

import com.ustadmobile.port.sharedse.networkmanager.URLConnectionOpener;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by mike on 2/8/18.
 */
@TargetApi(21)
public class AndroidNetworkURLConnectionOpener implements URLConnectionOpener {

    private Network network;

    public AndroidNetworkURLConnectionOpener(Network network) {
        this.network = network;
    }

    @Override
    public URLConnection openConnection(URL url) throws IOException {
        return network.openConnection(url);
    }
}
