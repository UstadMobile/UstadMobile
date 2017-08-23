package com.ustadmobile.test.sharedse.http;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.NetworkNode;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.port.sharedse.networkmanager.WifiP2pListener;
import com.ustadmobile.test.sharedse.network.TestWifiDirectPeerDiscovery;

import java.util.List;

/**
 * Created by mike on 8/19/17.
 */

public class TestServerFileSender implements Runnable, WifiP2pListener{

    private String destMacAddr;

    private String[] entryIdsToSend;

    private Thread thread;

    public static final int SEND_DISCOVERY_TIMEOUT = 60000;

    public TestServerFileSender(String destMacAddr, String[] entryIdsToSend) {
        this.destMacAddr = destMacAddr;
        this.entryIdsToSend = entryIdsToSend;
    }

    public void start() {
        if(thread == null) {
            thread= new Thread(this);
            thread.start();
        }
    }

    @Override
    public void run() {
        NetworkManager manager = UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        manager.addWifiDirectPeersListener(this);
        try {
            //wait for discovery
            List<NetworkNode> knownPeers = manager.getKnownWifiDirectPeers();
            if(!TestWifiDirectPeerDiscovery.isMacAddrInList(knownPeers, destMacAddr)) {
                synchronized (this) {
                    try { wait(SEND_DISCOVERY_TIMEOUT); }
                    catch(InterruptedException e) {}
                }
            }

            knownPeers = manager.getKnownWifiDirectPeers();
            if(!TestWifiDirectPeerDiscovery.isMacAddrInList(knownPeers, destMacAddr)) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 662, "Did not discover destination: " + destMacAddr);
                return;

            }
            manager.shareEntries(entryIdsToSend, "Test incoming files", destMacAddr);

//            manager.connectToWifiDirectNode(destMacAddr);
        }catch(Exception e) {

        }

        manager.removeWifiDirectPeersListener(this);
    }

    @Override
    public void peersChanged(List<NetworkNode> peers) {
        if(TestWifiDirectPeerDiscovery.isMacAddrInList(peers, destMacAddr)) {
            synchronized (this) {
                notify();
            }
        }
    }

    @Override
    public void wifiP2pConnectionChanged(boolean connected) {

    }
}
