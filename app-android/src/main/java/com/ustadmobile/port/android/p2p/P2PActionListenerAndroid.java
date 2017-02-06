package com.ustadmobile.port.android.p2p;

import android.net.wifi.p2p.WifiP2pManager;

import com.ustadmobile.port.sharedse.p2p.P2PActionListener;

/**
 * Created by kileha3 on 05/02/2017.
 */

public class P2PActionListenerAndroid implements WifiP2pManager.ActionListener{

    private P2PActionListener dest;

    public P2PActionListenerAndroid(P2PActionListener dest) {
        this.dest = dest;
    }

    @Override
    public void onSuccess() {
        dest.onSuccess();
    }

    @Override
    public void onFailure(int i) {
        dest.onFailure(i);
    }
}
