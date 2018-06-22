package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.fs.db.ContainerFileHelper;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.DownloadJobWithDownloadSet;
import com.ustadmobile.lib.db.entities.DownloadSet;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryWithChildEntries;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.port.sharedse.networkmanager.WifiP2pListener;
import com.ustadmobile.test.core.annotation.PeerServerRequiredTest;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.core.impl.PlatformTestUtil;
import com.ustadmobile.test.sharedse.TestUtilsSE;

import org.junit.Assert;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mike on 8/14/17.
 */
@PeerServerRequiredTest
public class TestWifiDirectSendReceive {

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

//    TODO: the direct send/receive test needs reworked to use the new download preparation / crawl system
//    @Test
//    public void testReceiveFile() throws IOException, XmlPullParserException{
//        ContainerFileHelper.getInstance().deleteAllContainerFilesByEntryId(PlatformTestUtil.getTargetContext(),
//                TestEntryStatusTask.ENTRY_ID);
//
//        NetworkManager manager = UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
//        NetworkNode thisNode = manager.getThisWifiDirectDevice();
//
//        Assert.assertNotNull("This wifi direct device is not null", thisNode);
//        final Object groupInfoLock = new Object();
//        WifiP2pListener listener = new WifiP2pListener() {
//            @Override
//            public void peersChanged(List<NetworkNode> peers) {
//
//            }
//
//            @Override
//            public void wifiP2pConnectionChanged(boolean connected) {
//                if(connected) {
//                    synchronized (groupInfoLock) {
//                        groupInfoLock.notify();
//                    }
//                }
//            }
//
//            @Override
//            public void wifiP2pConnectionResult(String macAddr, boolean connected) {
//
//            }
//        };
//        manager.addWifiDirectPeersListener(listener);
//
//
//        TestUtilsSE.requestSendFileViaWifiDirect(thisNode.getWifiDirectMacAddress(),
//                new String[]{TestEntryStatusTask.ENTRY_ID});
//        if(!manager.isWifiDirectConnectionEstablished(TestConstants.TEST_REMOTE_SLAVE_SERVER_WLAN_MAC)) {
//            //we should now get an incoming connection request
//            synchronized (groupInfoLock) {
//                try { groupInfoLock.wait(120000); }
//                catch(InterruptedException e) {}
//            }
//        }
//
//        OpdsEntryWithChildEntries feed = manager.getOpdsFeedSharedByWifiP2pGroupOwner();
//        manager.removeWifiDirectPeersListener(listener);
//
//        Assert.assertNotNull("Loaded shared feed object", feed);
//        Assert.assertNotNull("Feed contains the expected entry",
//                feed.getChildEntryByEntryId(TestEntryStatusTask.ENTRY_ID));
//
//        //now acquire that feed - make this a feed that will work for acquisition
//        String destinationDir= UstadMobileSystemImpl.getInstance().getStorageDirs(
//                CatalogPresenter.SHARED_RESOURCE, PlatformTestUtil.getTargetContext())[0].getDirURI();
//
//        final Object acquireLock = new Object();
//
//        DbManager dbManager = DbManager.getInstance(PlatformTestUtil.getTargetContext());
//
//        String feedBaseHref = feed.getLinks(OpdsEntry.LINK_REL_P2P_SELF, null, null,
//                false, false, false ,1).get(0)
//                .getHref();
//
//        List<OpdsLink> linkList = new ArrayList<>();
//        for(OpdsEntryWithRelations entry : feed.getChildEntries()) {
//            OpdsLink acquireLink = entry.getAcquisitionLink(null, false);
//            String linkHref = UMFileUtil.resolveLink(feedBaseHref, acquireLink.getHref());
//            linkList.add(new OpdsLink(entry.getUuid(), acquireLink.getMimeType(),linkHref,
//                    OpdsEntry.LINK_REL_ACQUIRE));
//        }
//
//        dbManager.getOpdsLinkDao().insert(linkList);
//
//        dbManager.getOpdsEntryDao().insertList(
//                OpdsEntryWithRelations.toOpdsEntryList(feed.getChildEntries()));
//
//        NetworkManager networkManager = (NetworkManager)UstadMobileSystemImpl.getInstance().getNetworkManager();
//        DownloadSet job = networkManager.buildDownloadJob(feed.getChildEntries(), destinationDir,
//                false);
//        networkManager.queueDownloadJob(job.getId());
//
//        UmLiveData<DownloadJobWithDownloadSet> jobLiveData = dbManager.getDownloadSetDao().getByIdLive(
//                job.getId());
//
//        UmObserver<DownloadSetWithRelations> observer = (downloadJob) -> {
//            synchronized (acquireLock) {
//                if(downloadJob != null && downloadJob.getStatus() > 20) {
//                    acquireLock.notifyAll();
//                }
//            }
//        };
//
//        jobLiveData.observeForever(observer);
//
//        synchronized (acquireLock) {
//            try { acquireLock.wait(60000*4); }
//            catch(InterruptedException e) {}
//        }
//
//        Assert.assertEquals("Download job reported as complete",
//                NetworkTask.STATUS_COMPLETE, jobLiveData.getValue().getStatus());
//    }



}
