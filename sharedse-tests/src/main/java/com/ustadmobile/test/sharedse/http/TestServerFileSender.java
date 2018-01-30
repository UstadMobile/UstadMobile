package com.ustadmobile.test.sharedse.http;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.lib.db.entities.NetworkNode;
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

    private NetworkManager networkManager;

    public TestServerFileSender(String destMacAddr, String[] entryIdsToSend, NetworkManager networkManager) {
        this.destMacAddr = destMacAddr;
        this.entryIdsToSend = entryIdsToSend;
        this.networkManager = networkManager;
    }

    public void start() {
        if(thread == null) {
            thread= new Thread(this);
            thread.start();
        }
    }

    @Override
    public void run() {
        networkManager.addWifiDirectPeersListener(this);
        try {
            //wait for discovery
            List<NetworkNode> knownPeers = networkManager.getKnownWifiDirectPeers();
            if(!NetworkManager.isMacAddrInList(knownPeers, destMacAddr)) {
                synchronized (this) {
                    try { wait(SEND_DISCOVERY_TIMEOUT); }
                    catch(InterruptedException e) {}
                }
            }

            knownPeers = networkManager.getKnownWifiDirectPeers();
            if(!NetworkManager.isMacAddrInList(knownPeers, destMacAddr)) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 662, "Did not discover destination: " + destMacAddr);
                return;

            }
            networkManager.shareEntries(entryIdsToSend, "Test incoming files", destMacAddr);
        }catch(Exception e) {

        }

        networkManager.removeWifiDirectPeersListener(this);
    }

    @Override
    public void peersChanged(List<NetworkNode> peers) {
        if(NetworkManager.isMacAddrInList(peers, destMacAddr)) {
            synchronized (this) {
                notify();
            }
        }
    }

    @Override
    public void wifiP2pConnectionChanged(boolean connected) {

    }

    @Override
    public void wifiP2pConnectionResult(String macAddr, boolean connected) {

    }
}
