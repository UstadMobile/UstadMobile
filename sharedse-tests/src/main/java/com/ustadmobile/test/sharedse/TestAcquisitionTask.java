package com.ustadmobile.test.sharedse;

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.impl.AcquisitionManager;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.AcquisitionTaskStatus;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.core.networkmanager.AcquisitionListener;
import com.ustadmobile.port.sharedse.networkmanager.AcquisitionTask;
import com.ustadmobile.core.networkmanager.AcquisitionTaskHistoryEntry;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.core.networkmanager.NetworkManagerListener;
import com.ustadmobile.core.networkmanager.NetworkNode;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.core.impl.ClassResourcesResponder;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.List;

import fi.iki.elonen.router.RouterNanoHTTPD;

import static org.hamcrest.CoreMatchers.is;

/**
 * Test the acquisition task. The OPDS feed on which the acquisition is based and the EPUBs are in
 * the resources of the sharedse-tests module.
 *
 * Created by kileha3 on 17/05/2017.
 */
public class TestAcquisitionTask{
    private static final int DEFAULT_WAIT_TIME =20000;
    private static final String FEED_LINK_MIME ="application/dir";
    private static final String ENTRY_ID_PRESENT ="202b10fe-b028-4b84-9b84-852aa766607d";
    private static final String ENTRY_ID_NOT_PRESENT = "b649852e-2bf9-45ab-839e-ec5bb00ca19d";
    public static final String[] ENTRY_IDS = new String[]{ENTRY_ID_PRESENT,ENTRY_ID_NOT_PRESENT};

    private static RouterNanoHTTPD resourcesHttpd;

    private boolean localNetworkEnabled=false;

    private boolean wifiDirectEnabled=false;

    /**
     * The resources server can be used as the "cloud"
     */
    private static String httpRoot;

    @BeforeClass
    public static void startHttpResourcesServer() throws IOException {
        if(resourcesHttpd == null) {
            resourcesHttpd = new RouterNanoHTTPD(0);
            resourcesHttpd.addRoute("/res/(.*)", ClassResourcesResponder.class, "/res/");
            resourcesHttpd.start();
            httpRoot = "http://localhost:" + resourcesHttpd.getListeningPort() + "/res/";
        }
    }

    @AfterClass
    public static void stopHttpResourcesServer() throws IOException {
        if(resourcesHttpd != null) {
            resourcesHttpd.stop();
            resourcesHttpd = null;
        }
    }

    public static void testAcquisition(NetworkNode remoteNode, boolean localNetworkEnabled, boolean wifiDirectEnabled, int expectedLocalDownloadMode) throws IOException, InterruptedException,XmlPullParserException{
        final NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        final Object acquireLock = new Object();

        //make sure we don't have any of the entries in question already
        CatalogController.removeEntry(ENTRY_ID_PRESENT, CatalogController.SHARED_RESOURCE,
                PlatformTestUtil.getTargetContext());
        CatalogController.removeEntry(ENTRY_ID_NOT_PRESENT, CatalogController.SHARED_RESOURCE,
                PlatformTestUtil.getTargetContext());

        NetworkManagerListener responseListener = new NetworkManagerListener() {
            @Override
            public void fileStatusCheckInformationAvailable(List<String> fileIds) {
            }

            @Override
            public void networkTaskCompleted(NetworkTask task) {
                if(task instanceof AcquisitionTask) {
                    if(((AcquisitionTask) task).taskIncludesEntry(ENTRY_ID_NOT_PRESENT)) {
                        //The entries are downloaded in the order in which they are requested -
                        // which is ENTRY_ID, ENTRY_ID_NOT_PRESENT
                        //Therefor when we receive the complete event for the latter we can notify the
                        //thread to continue.
                        synchronized (acquireLock) {
                            acquireLock.notifyAll();
                        }
                    }
                }
            }

            @Override
            public void networkNodeDiscovered(NetworkNode node) {
            }

            @Override
            public void networkNodeUpdated(NetworkNode node) {

            }

            @Override
            public void fileAcquisitionInformationAvailable(String entryId, long downloadId, int sources) {
            }

            @Override
            public void wifiConnectionChanged(String ssid, boolean connected, boolean connectedOrConnecting) {

            }


        };
        manager.addNetworkManagerListener(responseListener);
        Assert.assertTrue("Supernode mode enabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(true));
        TestNetworkManager.testWifiDirectDiscovery(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE,
                TestNetworkManager.NODE_DISCOVERY_TIMEOUT);
        TestNetworkManager.testNetworkServiceDiscovery(SharedSeTestSuite.REMOTE_SLAVE_SERVER,
                TestNetworkManager.NODE_DISCOVERY_TIMEOUT);
        TestEntryStatusTask.testEntryStatusBluetooth(TestEntryStatusTask.EXPECTED_AVAILABILITY,
                TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);


        //NetworkNode remoteNode = manager.getNodeByBluetoothAddr(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);
        int numAcquisitions = remoteNode.getAcquisitionHistory() != null ? remoteNode.getAcquisitionHistory().size() : 0;

        //Create a feed manually
        String catalogUrl = UMFileUtil.joinPaths(new String[]{
                httpRoot, "com/ustadmobile/test/sharedse/test-acquisition-task-feed.opds"});
        UstadJSOPDSFeed feed = CatalogController.getCatalogByURL(catalogUrl,
                CatalogController.SHARED_RESOURCE, null, null, 0, PlatformTestUtil.getTargetContext());

        String destinationDir= UstadMobileSystemImpl.getInstance().getStorageDirs(
                CatalogController.SHARED_RESOURCE, PlatformTestUtil.getTargetContext())[0].getDirURI();
        feed.addLink(AcquisitionManager.LINK_REL_DOWNLOAD_DESTINATION,
                FEED_LINK_MIME, destinationDir);
        feed.addLink(UstadJSOPDSItem.LINK_REL_SELF_ABSOLUTE, UstadJSOPDSItem.TYPE_ACQUISITIONFEED,
                catalogUrl);

        AcquisitionListener acquisitionListener =new AcquisitionListener() {
            @Override
            public void acquisitionProgressUpdate(String entryId, AcquisitionTaskStatus status) {

            }

            @Override
            public void acquisitionStatusChanged(String entryId, AcquisitionTaskStatus status) {
                UstadMobileSystemImpl.l(UMLog.INFO, 335, "acquisition status changed: " + entryId +
                        " : " +status.getStatus());
            }
        };

        manager.addAcquisitionTaskListener(acquisitionListener);

        manager.requestAcquisition(feed,manager.getContext(),localNetworkEnabled,wifiDirectEnabled);
        AcquisitionTask task = manager.getAcquisitionTaskByEntryId(ENTRY_ID_PRESENT);
        Assert.assertNotNull("Task created for acquisition", task);
        synchronized (acquireLock){
            acquireLock.wait(DEFAULT_WAIT_TIME* 6);
        }

        List<AcquisitionTaskHistoryEntry> entryHistoryList = task.getAcquisitionHistoryByEntryId(ENTRY_ID_PRESENT);
        for(AcquisitionTaskHistoryEntry entryHistory : entryHistoryList) {
            Assert.assertEquals("Task reported as being downloaded from same network",
                    expectedLocalDownloadMode, entryHistory.getMode());
        }

        //check history was recorded on the node
        //Assertion has failed 27/06/17 - not able to reproduce again.
        Assert.assertNotNull("Remote node has acquisition history", remoteNode.getAcquisitionHistory());
        Assert.assertEquals("Remote node has one additional acquisition history entry",
                numAcquisitions + 1, remoteNode.getAcquisitionHistory().size());
        numAcquisitions = remoteNode.getAcquisitionHistory().size();


        CatalogEntryInfo localEntryInfo = CatalogController.getEntryInfo(ENTRY_ID_PRESENT,
                CatalogController.SHARED_RESOURCE, PlatformTestUtil.getTargetContext());
        Assert.assertEquals("File was downloaded successfully from node on same network",
                CatalogController.STATUS_ACQUIRED, localEntryInfo.acquisitionStatus);
        Assert.assertTrue("File downloaded via local network is present",
                new File(localEntryInfo.fileURI).exists());

        CatalogEntryInfo cloudEntryInfo = CatalogController.getEntryInfo(ENTRY_ID_NOT_PRESENT,
                CatalogController.SHARED_RESOURCE, PlatformTestUtil.getTargetContext());
        Assert.assertEquals("File was downloaded successfully from cloud",
                CatalogController.STATUS_ACQUIRED, cloudEntryInfo.acquisitionStatus);
        Assert.assertTrue("File downloaded via cloud is present",
                new File(cloudEntryInfo.fileURI).exists());

        manager.removeNetworkManagerListener(responseListener);
        manager.removeAcquisitionTaskListener(acquisitionListener);
    }


    @Test
    public void testAcquisition() throws IOException, InterruptedException, XmlPullParserException {
        final NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        Assume.assumeTrue("Network test wifi and bluetooth enabled",
                manager.isBluetoothEnabled() && manager.isWiFiEnabled());
        Assert.assertTrue("Supernode mode enabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(true));
        TestNetworkManager.testWifiDirectDiscovery(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE,
                TestNetworkManager.NODE_DISCOVERY_TIMEOUT);
        TestNetworkManager.testNetworkServiceDiscovery(SharedSeTestSuite.REMOTE_SLAVE_SERVER,
                TestNetworkManager.NODE_DISCOVERY_TIMEOUT);
        TestEntryStatusTask.testEntryStatusBluetooth(TestEntryStatusTask.EXPECTED_AVAILABILITY,
                TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);
        NetworkNode remoteNode = manager.getNodeByBluetoothAddr(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);
        testAcquisition(remoteNode, true, false, NetworkManager.DOWNLOAD_FROM_PEER_ON_SAME_NETWORK);
        testAcquisition(remoteNode, false, true, NetworkManager.DOWNLOAD_FROM_PEER_ON_DIFFERENT_NETWORK);
        Assert.assertTrue(TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(false));
    }

}
