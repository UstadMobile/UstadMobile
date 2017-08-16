package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.networkmanager.NetworkNode;

import java.util.List;

/**
 * Created by mike on 8/14/17.
 */

public interface WifiP2pPeerListener {

    void peersChanged(List<NetworkNode> peers);

}
