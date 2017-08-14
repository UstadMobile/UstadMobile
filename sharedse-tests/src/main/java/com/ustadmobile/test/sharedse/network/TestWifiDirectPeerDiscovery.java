package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.core.networkmanager.NetworkNode;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.port.sharedse.networkmanager.WifiP2pPeerListener;
import com.ustadmobile.test.core.buildconfig.TestConstants;

import org.junit.Test;

import java.util.List;

/**
 * Created by mike on 8/14/17.
 */

public class TestWifiDirectPeerDiscovery {

    public static final int PEER_DISCOVERY_TIMEOUT = 120000;

    @Test
    public void testPeerDiscovery() {
        final Object lock = new Object();
        WifiP2pPeerListener listener = new WifiP2pPeerListener() {
            @Override
            public void peersChanged(List<NetworkNode> peers) {
                if(isMacAddrInList(peers, TestConstants.TEST_REMOTE_SLAVE_SERVER_WLAN_MAC)){
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            }
        };

        NetworkManager manager = UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        manager.addWifiDirectPeersListener(listener);
        synchronized (lock) {
            try { lock.wait(PEER_DISCOVERY_TIMEOUT); }
            catch(InterruptedException e) {}
        }

    }


    private boolean isMacAddrInList(List<NetworkNode> list, String macAddr) {
        String nodeMacAddr;
        for(NetworkNode node : list) {
            nodeMacAddr = node.getDeviceWifiDirectMacAddress();
            if(nodeMacAddr != null && nodeMacAddr.equals(macAddr))
                return true;
        }

        return false;
    }
}
