package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.AcquisitionListener;
import com.ustadmobile.core.networkmanager.AcquisitionTaskStatus;
import com.ustadmobile.core.networkmanager.NetworkManagerCore;
import com.ustadmobile.core.networkmanager.NetworkNode;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.port.sharedse.networkmanager.WifiP2pListener;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.core.impl.PlatformTestUtil;
import com.ustadmobile.test.sharedse.TestUtilsSE;

import org.junit.Assert;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

/**
 * Created by mike on 8/14/17.
 */

public class TestWifiDirectPeerDiscovery {

    public static final int PEER_DISCOVERY_TIMEOUT = 120000;

    @Test
    public void testPeerDiscovery() {
        final Object lock = new Object();
        WifiP2pListener listener = new WifiP2pListener() {
            @Override
            public void peersChanged(List<NetworkNode> peers) {
                if(NetworkManager.isMacAddrInList(peers, TestConstants.TEST_REMOTE_SLAVE_SERVER_WLAN_MAC)){
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            }

            @Override
            public void wifiP2pConnectionChanged(boolean connected) {

            }

            @Override
            public void wifiP2pConnectionResult(String macAddr, boolean connected) {

            }
        };

        NetworkManager manager = UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        manager.setSharedFeed(new String[]{TestEntryStatusTask.ENTRY_ID}, "Test Entry Status");
        manager.addWifiDirectPeersListener(listener);
        List<NetworkNode> knownPeers = manager.getKnownWifiDirectPeers();
        if(!NetworkManager.isMacAddrInList(knownPeers, TestConstants.TEST_REMOTE_SLAVE_SERVER_WLAN_MAC)) {
            synchronized (lock) {
                try { lock.wait(PEER_DISCOVERY_TIMEOUT); }
                catch(InterruptedException e) {}
            }
        }
        knownPeers = manager.getKnownWifiDirectPeers();
        manager.removeWifiDirectPeersListener(listener);
        manager.setSharedFeed(null);

        Assert.assertTrue("Discovered peer for remote test slave server",
                NetworkManager.isMacAddrInList(knownPeers, TestConstants.TEST_REMOTE_SLAVE_SERVER_WLAN_MAC));
    }

    @Test
    public void testReceiveFile() throws IOException, XmlPullParserException{
        NetworkManager manager = UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        NetworkNode thisNode = manager.getThisWifiDirectDevice();

        Assert.assertNotNull("This wifi direct device is not null", thisNode);
        final Object groupInfoLock = new Object();
        WifiP2pListener listener = new WifiP2pListener() {
            @Override
            public void peersChanged(List<NetworkNode> peers) {

            }

            @Override
            public void wifiP2pConnectionChanged(boolean connected) {
                if(connected) {
                    synchronized (groupInfoLock) {
                        groupInfoLock.notify();
                    }
                }
            }

            @Override
            public void wifiP2pConnectionResult(String macAddr, boolean connected) {

            }
        };
        manager.addWifiDirectPeersListener(listener);


        TestUtilsSE.requestSendFileViaWifiDirect(thisNode.getDeviceWifiDirectMacAddress(),
                new String[]{TestEntryStatusTask.ENTRY_ID});
        if(!manager.isWifiDirectConnectionEstablished(TestConstants.TEST_REMOTE_SLAVE_SERVER_WLAN_MAC)) {
            //we should now get an incoming connection request
            synchronized (groupInfoLock) {
                try { groupInfoLock.wait(120000); }
                catch(InterruptedException e) {}
            }
        }

        UstadJSOPDSFeed feed = manager.getOpdsFeedSharedByWifiP2pGroupOwner();
        manager.removeWifiDirectPeersListener(listener);

        Assert.assertNotNull("Loaded shared feed object", feed);
        Assert.assertNotNull("Feed contains the expected entry",
                feed.getEntryById(TestEntryStatusTask.ENTRY_ID));

        //now acquire that feed - make this a feed that will work for acquisition
        String destinationDir= UstadMobileSystemImpl.getInstance().getStorageDirs(
                CatalogPresenter.SHARED_RESOURCE, PlatformTestUtil.getTargetContext())[0].getDirURI();
        feed.addLink(NetworkManagerCore.LINK_REL_DOWNLOAD_DESTINATION,
                "application/dir", destinationDir);

        final int[] acquisitionStatus = new int[1];
        final Object acquisitionLock = new Object();
        AcquisitionListener acquisitionListener = new AcquisitionListener() {
            @Override
            public void acquisitionProgressUpdate(String entryId, AcquisitionTaskStatus status) {

            }

            @Override
            public void acquisitionStatusChanged(String entryId, AcquisitionTaskStatus status) {
                if(entryId.equals(TestEntryStatusTask.ENTRY_ID)) {
                    acquisitionStatus[0] = status.getStatus();
                    if(status.getStatus() == UstadMobileSystemImpl.DLSTATUS_SUCCESSFUL) {
                        synchronized (acquisitionLock) {
                            acquisitionLock.notify();
                        }
                    }
                }
            }
        };

        manager.addAcquisitionTaskListener(acquisitionListener);
        manager.requestAcquisition(feed, true, true);
        synchronized (acquisitionLock) {
            try { acquisitionLock.wait(60000); }
            catch(InterruptedException e) {}
        }
        Assert.assertEquals("Acquisition of shared feed completed successfully",
                acquisitionStatus[0], UstadMobileSystemImpl.DLSTATUS_SUCCESSFUL);

    }



}
