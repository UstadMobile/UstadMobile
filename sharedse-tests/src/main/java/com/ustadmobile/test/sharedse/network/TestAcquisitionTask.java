package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.AcquisitionTaskStatus;
import com.ustadmobile.core.networkmanager.EntryCheckResponse;
import com.ustadmobile.core.networkmanager.NetworkManagerCore;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.core.networkmanager.AcquisitionListener;
import com.ustadmobile.port.sharedse.networkmanager.AcquisitionTask;
import com.ustadmobile.core.networkmanager.AcquisitionTaskHistoryEntry;
import com.ustadmobile.port.sharedse.networkmanager.LocalMirrorFinder;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.core.networkmanager.NetworkManagerListener;
import com.ustadmobile.core.networkmanager.NetworkNode;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.core.impl.ClassResourcesResponder;
import com.ustadmobile.test.core.impl.PlatformTestUtil;
import com.ustadmobile.test.sharedse.SharedSeTestSuite;
import com.ustadmobile.test.sharedse.TestUtilsSE;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fi.iki.elonen.router.RouterNanoHTTPD;

/**
 * Test the acquisition task. The OPDS feed on which the acquisition is based and the EPUBs are in
 * the resources of the sharedse-tests module.
 *
 * Created by kileha3 on 17/05/2017.
 */
public class TestAcquisitionTask {

    private static final int DEFAULT_ACQUIRE_TIMEOUT = 120000;//default acquire timeout: 2mins

    private static final String FEED_LINK_MIME ="application/dir";
    private static final String ENTRY_ID_PRESENT ="202b10fe-b028-4b84-9b84-852aa766607d";
    private static final String ENTRY_ID_NOT_PRESENT = "b649852e-2bf9-45ab-839e-ec5bb00ca19d";
    public static final String[] ENTRY_IDS = new String[]{ENTRY_ID_PRESENT,ENTRY_ID_NOT_PRESENT};

    private static RouterNanoHTTPD resourcesHttpd;

    /**
     * The resources server can be used as the "cloud"
     */
    private static String httpRoot;

    @BeforeClass
    public static void startHttpResourcesServer() throws IOException, InterruptedException {
        if(resourcesHttpd == null) {
            resourcesHttpd = new RouterNanoHTTPD(0);
            resourcesHttpd.addRoute("/res/(.*)", ClassResourcesResponder.class, "/res/");
            resourcesHttpd.start();
            httpRoot = "http://localhost:" + resourcesHttpd.getListeningPort() + "/res/";
        }

        SharedSeNetworkTestSuite.assumeNetworkHardwareEnabled();
    }

    @AfterClass
    public static void stopHttpResourcesServer() throws IOException {
        if(resourcesHttpd != null) {
            resourcesHttpd.stop();
            resourcesHttpd = null;
        }
    }

    public static void testAcquisition(NetworkNode remoteNode, LocalMirrorFinder mirrorFinder, boolean localNetworkEnabled, boolean wifiDirectEnabled, int expectedLocalDownloadMode, int acquireTimeout) throws IOException, InterruptedException,XmlPullParserException{
        final NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        manager.clearNetworkNodeAcquisitionHistory();
        final Object acquireLock = new Object();

        //make sure we don't have any of the entries in question already
        CatalogPresenter.removeEntry(ENTRY_ID_PRESENT, CatalogPresenter.SHARED_RESOURCE,
                PlatformTestUtil.getTargetContext());
        CatalogPresenter.removeEntry(ENTRY_ID_NOT_PRESENT, CatalogPresenter.SHARED_RESOURCE,
                PlatformTestUtil.getTargetContext());

        final long[] testTaskId = new long[1];
        NetworkManagerListener responseListener = new NetworkManagerListener() {
            @Override
            public void fileStatusCheckInformationAvailable(String[] fileIds) {
            }

            @Override
            public void networkTaskStatusChanged(NetworkTask task) {
                if(task.getTaskId() == testTaskId[0] && (task.isFinished() || task.isRetryNeeded())) {
                    synchronized (acquireLock) {
                        acquireLock.notifyAll();
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
        int numAcquisitions = remoteNode.getAcquisitionHistory() != null ?
                remoteNode.getAcquisitionHistory().size() : 0;

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

        UstadJSOPDSFeed feed = makeAcquisitionTestFeed();
        testTaskId[0] = manager.requestAcquisition(feed, mirrorFinder,localNetworkEnabled,wifiDirectEnabled);
        AcquisitionTask task = (AcquisitionTask)manager.getTaskById(testTaskId[0],
                NetworkManager.QUEUE_ENTRY_ACQUISITION);
        Assert.assertNotNull("Task created for acquisition", task);
        synchronized (acquireLock){
            acquireLock.wait(acquireTimeout);
        }

        List<AcquisitionTaskHistoryEntry> entryHistoryList = task.getAcquisitionHistoryByEntryId(ENTRY_ID_PRESENT);
        int lastIndex = entryHistoryList.size()-1;
        int networkDownloadedFrom =entryHistoryList.get(lastIndex).getMode();
        UstadMobileSystemImpl.l(UMLog.DEBUG, 646, "Test task id = " + task.getTaskId());
        Assert.assertEquals("Last history entry was downloaded from expected network", expectedLocalDownloadMode,
                networkDownloadedFrom);
        int lastHistoryStatus = entryHistoryList.get(entryHistoryList.size()-1).getStatus();
        Assert.assertEquals("Last history entry was successful", UstadMobileSystemImpl.DLSTATUS_SUCCESSFUL,
                lastHistoryStatus);

        //check history was recorded on the node
        //Assertion has failed 27/06/17 - not able to reproduce again.
        if(expectedLocalDownloadMode != NetworkManager.DOWNLOAD_FROM_CLOUD) {
            Assert.assertNotNull("Remote node has acquisition history", remoteNode.getAcquisitionHistory());
            Assert.assertTrue("Remote node has at least one additional acquisition history entries",
                    remoteNode.getAcquisitionHistory().size() > numAcquisitions);
        }

        CatalogEntryInfo localEntryInfo = CatalogPresenter.getEntryInfo(ENTRY_ID_PRESENT,
                CatalogPresenter.SHARED_RESOURCE, PlatformTestUtil.getTargetContext());
        Assert.assertEquals("File was downloaded successfully from node on same network",
                CatalogPresenter.STATUS_ACQUIRED, localEntryInfo.acquisitionStatus);
        Assert.assertTrue("File downloaded via local network is present",
                new File(localEntryInfo.fileURI).exists());

        CatalogEntryInfo cloudEntryInfo = CatalogPresenter.getEntryInfo(ENTRY_ID_NOT_PRESENT,
                CatalogPresenter.SHARED_RESOURCE, PlatformTestUtil.getTargetContext());
        Assert.assertEquals("File was downloaded successfully from cloud",
                CatalogPresenter.STATUS_ACQUIRED, cloudEntryInfo.acquisitionStatus);
        Assert.assertTrue("File downloaded via cloud is present",
                new File(cloudEntryInfo.fileURI).exists());

        manager.removeNetworkManagerListener(responseListener);
        manager.removeAcquisitionTaskListener(acquisitionListener);
    }

    public static void testAcquisition(NetworkNode remoteNode, LocalMirrorFinder mirrorFinder, boolean localNetworkEnabled, boolean wifiDirectEnabled, int expectedLocalDownloadMode) throws IOException, InterruptedException,XmlPullParserException{
        testAcquisition(remoteNode, mirrorFinder, localNetworkEnabled, wifiDirectEnabled, expectedLocalDownloadMode, DEFAULT_ACQUIRE_TIMEOUT);
    }

    private static UstadJSOPDSFeed makeAcquisitionTestFeed() throws XmlPullParserException, IOException{
        //Create a feed manually
        String catalogUrl = UMFileUtil.joinPaths(new String[]{
                httpRoot, "com/ustadmobile/test/sharedse/test-acquisition-task-feed.opds"});
        UstadJSOPDSFeed feed = new UstadJSOPDSFeed(catalogUrl);
        HttpURLConnection urlConnection = (HttpURLConnection)new URL(catalogUrl).openConnection();

        InputStream in = urlConnection.getInputStream();
        XmlPullParser xpp = UstadMobileSystemImpl.getInstance().newPullParser(in);
        feed.loadFromXpp(xpp, null);
        in.close();

        String destinationDir= UstadMobileSystemImpl.getInstance().getStorageDirs(
                CatalogPresenter.SHARED_RESOURCE, PlatformTestUtil.getTargetContext())[0].getDirURI();
        feed.addLink(NetworkManagerCore.LINK_REL_DOWNLOAD_DESTINATION,
                FEED_LINK_MIME, destinationDir);
        feed.addLink(UstadJSOPDSItem.LINK_REL_SELF_ABSOLUTE, UstadJSOPDSItem.TYPE_ACQUISITIONFEED,
                catalogUrl);

        return feed;
    }


    /**
     * Main acquisition test: test downloading from the same network (e.g. use with network
     * service discovery) and test downloading using WiFi direct when not on the same network
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws XmlPullParserException
     */
//    @Test
    public void testAcquisitionLocalWifi() throws IOException, InterruptedException, XmlPullParserException {
        final NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        SharedSeNetworkTestSuite.assumeNetworkHardwareEnabled();
        Assert.assertTrue("Supernode mode enabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(true));

        TestNetworkManager.testNetworkServiceDiscovery(SharedSeTestSuite.REMOTE_SLAVE_SERVER,
                TestNetworkManager.NODE_DISCOVERY_TIMEOUT);
        //TODO: Test entry status over http here


        NetworkNode remoteNode = manager.getNodeByBluetoothAddr(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);
        testAcquisition(remoteNode, manager, true, false, NetworkManager.DOWNLOAD_FROM_PEER_ON_SAME_NETWORK);
        Assert.assertTrue(TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(false));
    }

    @Test
    public void testAcquisitionWifiDirect() throws IOException, InterruptedException, XmlPullParserException {
        final NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        SharedSeNetworkTestSuite.assumeNetworkHardwareEnabled();
        Assert.assertTrue("Supernode mode enabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(true));
        TestNetworkManager.testWifiDirectDiscovery(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE,
                TestNetworkManager.NODE_DISCOVERY_TIMEOUT);
        TestEntryStatusTask.testEntryStatusBluetooth(TestEntryStatusTask.EXPECTED_AVAILABILITY,
                TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);

        NetworkNode remoteNode = manager.getNodeByBluetoothAddr(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);
        testAcquisition(remoteNode, manager, false, true, NetworkManager.DOWNLOAD_FROM_PEER_ON_DIFFERENT_NETWORK);
        Assert.assertTrue(TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(false));
    }


    /**
     * Test what happens with acquisition over WiFi direct when the bluetooth connection to the
     * remote node fails. After a number of failures the scoring mechanism should result in the
     * download taking place from the cloud.
     *
     * @throws Exception
     */
    @Test(timeout = 10 * 60 * 1000)//for debugging purposes - should normally complete in 30s
    public void testAcquisitionBluetoothFail() throws Exception {
        final NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        SharedSeNetworkTestSuite.assumeNetworkHardwareEnabled();
        Assert.assertTrue("Supernode mode enabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(true));
        TestNetworkManager.testWifiDirectDiscovery(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE,
                TestNetworkManager.NODE_DISCOVERY_TIMEOUT);
        TestEntryStatusTask.testEntryStatusBluetooth(TestEntryStatusTask.EXPECTED_AVAILABILITY,
                TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);

        final NetworkNode remoteNode = manager.getNodeByBluetoothAddr(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);

        final NetworkNode wrongAddressNode = new NetworkNode(remoteNode.getDeviceWifiDirectMacAddress(),
                null);
        wrongAddressNode.setDeviceBluetoothMacAddress("00:11:22:33:44:55");
        wrongAddressNode.setWifiDirectLastUpdated(Calendar.getInstance().getTimeInMillis());
        EntryCheckResponse wrongAddressNodeResponse = new EntryCheckResponse(wrongAddressNode);
        wrongAddressNodeResponse.setFileAvailable(true);
        wrongAddressNodeResponse.setLastChecked(Calendar.getInstance().getTimeInMillis());
        final List<EntryCheckResponse> entryCheckResponseList = new ArrayList<>();
        entryCheckResponseList.add(wrongAddressNodeResponse);


        LocalMirrorFinder mirrorFinder= new LocalMirrorFinder() {
            @Override
            public List<EntryCheckResponse> getEntryResponsesWithLocalFile(String entryId) {
                if(entryId.equals(ENTRY_ID_PRESENT))
                    return entryCheckResponseList;
                else
                    return null;
            }
        };

        testAcquisition(remoteNode, mirrorFinder, false, true, NetworkManager.DOWNLOAD_FROM_CLOUD);
        Assert.assertTrue("Supernode mode disabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(false));
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testAcquisitionStop() throws Exception {
        final NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        SharedSeNetworkTestSuite.assumeNetworkHardwareEnabled();
        CatalogPresenter.removeEntry(ENTRY_ID_PRESENT, CatalogPresenter.SHARED_RESOURCE,
                PlatformTestUtil.getTargetContext());
        CatalogPresenter.removeEntry(ENTRY_ID_NOT_PRESENT, CatalogPresenter.SHARED_RESOURCE,
                PlatformTestUtil.getTargetContext());

        Assert.assertTrue("Supernode mode enabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(true));

        TestNetworkManager.testWifiDirectDiscovery(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE,
                TestNetworkManager.NODE_DISCOVERY_TIMEOUT);
        TestNetworkManager.testNetworkServiceDiscovery(SharedSeTestSuite.REMOTE_SLAVE_SERVER,
                TestNetworkManager.NODE_DISCOVERY_TIMEOUT);


        NetworkNode remoteNode = manager.getNodeByBluetoothAddr(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);
        UstadJSOPDSFeed feed = makeAcquisitionTestFeed();
        manager.requestAcquisition(feed, manager, false, false);
        AcquisitionTask task = manager.getAcquisitionTaskByEntryId(ENTRY_ID_PRESENT);
        try { Thread.sleep(1000); }
        catch(InterruptedException e){}
        task.stop(NetworkTask.STATUS_STOPPED);
        try { Thread.sleep(3000); }
        catch(InterruptedException e){}
        //TODO: Fix me - what's happening: Acquisition Task is being restarted when stopped, timer task is null on acquisitiontask.java line 325

        Assert.assertEquals("Task status is stopped", NetworkTask.STATUS_STOPPED, task.getStatus());
        Assert.assertTrue("Task is stopped", task.isStopped());
        CatalogEntryInfo presentEntryInfo = CatalogPresenter.getEntryInfo(ENTRY_ID_PRESENT,
                CatalogPresenter.SHARED_RESOURCE, PlatformTestUtil.getTargetContext());
        Assert.assertEquals("Entry 1 not acquired", CatalogPresenter.STATUS_NOT_ACQUIRED,
                presentEntryInfo.acquisitionStatus);
        CatalogEntryInfo notPresentEntryInfo = CatalogPresenter.getEntryInfo(ENTRY_ID_NOT_PRESENT,
                CatalogPresenter.SHARED_RESOURCE, PlatformTestUtil.getTargetContext());
        Assert.assertEquals("Entry 2 not acquired", CatalogPresenter.STATUS_NOT_ACQUIRED,
                notPresentEntryInfo.acquisitionStatus);
        Assert.assertTrue("Supernode mode disabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(false));

    }


    /**
     * Test what happens with acquisition over WiFi direct when the WiFi connection fails. After a
     * number of failures the scoring mechanism should result in the download taking place from
     * the cloud. This is achieved by telling the remote test driver node to mangle the wifi direct
     * group information. The remote node will return an invalid wifi network, simulating the effect
     * of the WiFi group connection not working.
     *
     * @throws Exception
     */
    //@Test(timeout = 7 * 60 * 1000)
    public void testAcquisitionWifiDirectFail() throws Exception{
        final NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        SharedSeNetworkTestSuite.assumeNetworkHardwareEnabled();
        NetworkNode remoteNode = manager.getNodeByBluetoothAddr(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);
        Assert.assertTrue("Supernode mode enabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(true));
        Exception e = null;
        try {
            Assert.assertTrue("Mangle wifi direct group enabled", TestUtilsSE.setRemoteTestMangleWifi(true));
            testAcquisition(remoteNode, manager, false, true, NetworkManager.DOWNLOAD_FROM_CLOUD,
                    (manager.getWifiConnectionTimeout() * 5) + 60000);
        }catch(Exception e2) {
            e = e2;
        }finally {
            Assert.assertTrue("Mangle wifi direct group disabled", TestUtilsSE.setRemoteTestMangleWifi(false));
        }

        if(e != null)
            throw e;

        Assert.assertTrue("Supernode mode enabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(false));
    }


    @Test
    public void testEntryCheckResponseScoring() throws IOException, XmlPullParserException{
        /*
         * When the entry is available on the local network, and wifi direct - we should choose wifi direct
         */
        long timeNow = Calendar.getInstance().getTimeInMillis();

        NetworkNode wifiDirectNode = new NetworkNode("00:00:00:00:00:01", null);
        wifiDirectNode.setWifiDirectLastUpdated(timeNow);
        EntryCheckResponse wifiDirectResponse = new EntryCheckResponse(wifiDirectNode);
        wifiDirectResponse.setFileAvailable(true);

        NetworkNode sameNetworkNode = new NetworkNode(null, "127.0.0.2");
        sameNetworkNode.setNetworkServiceLastUpdated(timeNow);
        sameNetworkNode.setNsdServiceName("lan-node");

        EntryCheckResponse sameNetworkResponse = new EntryCheckResponse(sameNetworkNode);
        sameNetworkResponse.setFileAvailable(true);

        ArrayList responseList = new ArrayList();
        responseList.add(wifiDirectResponse);
        responseList.add(sameNetworkResponse);

        UstadJSOPDSFeed acquisitionFeed = makeAcquisitionTestFeed();
        AcquisitionTask task = new AcquisitionTask(makeAcquisitionTestFeed(),
                UstadMobileSystemImplSE.getInstanceSE().getNetworkManager());
        Assert.assertEquals("When WiFi direct and local network responses are available, local network response will be chosen",
                sameNetworkResponse, task.selectEntryCheckResponse(acquisitionFeed.entries[0], responseList));


        /*
         * When given a choice between a node with failures and a node without - select the node without recent failures
         */
        NetworkNode nodeWithoutFailures = new NetworkNode(null, "127.0.0.1");
        nodeWithoutFailures.setNetworkServiceLastUpdated(timeNow);
        nodeWithoutFailures.setNsdServiceName("without-failures");
        EntryCheckResponse nodeWithoutFailuresResponse = new EntryCheckResponse(nodeWithoutFailures);
        nodeWithoutFailuresResponse.setFileAvailable(true);

        NetworkNode nodeWithFailures= new NetworkNode(null, "127.0.0.2");
        nodeWithFailures.setNetworkServiceLastUpdated(timeNow);
        nodeWithoutFailures.setNsdServiceName("with-failures");
        EntryCheckResponse nodeWithFailuresResponse = new EntryCheckResponse(nodeWithFailures);
        nodeWithFailuresResponse.setFileAvailable(true);
        long failureTime = timeNow - 20000;//20 seconds ago
        AcquisitionTaskHistoryEntry failureEntry = new AcquisitionTaskHistoryEntry(
            acquisitionFeed.entries[0].id, "http://127.0.0.2:8001/catalog/entry/foo",
                NetworkManager.DOWNLOAD_FROM_PEER_ON_SAME_NETWORK, failureTime,
                failureTime, UstadMobileSystemImpl.DLSTATUS_FAILED);
        nodeWithFailures.addAcquisitionHistoryEntry(failureEntry);
        responseList.clear();
        responseList.add(nodeWithFailuresResponse);
        responseList.add(nodeWithoutFailuresResponse);
        Assert.assertEquals("When downloading prefer a network node without recent failures",
                nodeWithoutFailuresResponse, task.selectEntryCheckResponse(acquisitionFeed.entries[0],
                responseList));

        /*
         * When nodes have failed - choose the node where it has been the longest time since that failure
         */
        responseList.clear();
        NetworkNode[] nodesWithFailures = new NetworkNode[4];
        EntryCheckResponse[] nodeWithFailuresResponses = new EntryCheckResponse[4];
        for(int i = 0; i < nodesWithFailures.length; i++) {
            nodesWithFailures[i] = new NetworkNode(null, "127.0.0." + i);
            nodesWithFailures[i].setNetworkServiceLastUpdated(timeNow);
            nodesWithFailures[i].setNsdServiceName("node-with-failures-"+i);
            failureTime = timeNow - (((long)AcquisitionTask.FAILURE_MEMORY_TIME / 4)*i);
            AcquisitionTaskHistoryEntry nodeWithFailuresEntry = new AcquisitionTaskHistoryEntry(
                acquisitionFeed.entries[0].id, "http://127.0.0." + i +":8000/catalog/foo",
                NetworkManager.DOWNLOAD_FROM_PEER_ON_SAME_NETWORK, failureTime, failureTime,
                UstadMobileSystemImpl.DLSTATUS_FAILED);
            nodesWithFailures[i].addAcquisitionHistoryEntry(nodeWithFailuresEntry);
            nodeWithFailuresResponses[i] = new EntryCheckResponse(nodesWithFailures[i]);
            nodeWithFailuresResponses[i].setFileAvailable(true);
            responseList.add(nodeWithFailuresResponses[i]);
        }

        Assert.assertEquals("When nodes have failed choose the node where the failure was least recent",
                nodeWithFailuresResponses[3], task.selectEntryCheckResponse(acquisitionFeed.entries[0],
                responseList));

        /**
         * When update times are too old or there are too many failures - null should be returned
         */
        NetworkNode[] nodesWithMultipleFailures = new NetworkNode[3];
        EntryCheckResponse[] nodesWithMultipleFailuresResponse = new EntryCheckResponse[3];
        responseList.clear();
        long failTime = timeNow - (long)(AcquisitionTask.FAILURE_MEMORY_TIME / 4);
        int numFailures = 4;
        for(int i = 0; i < nodesWithMultipleFailuresResponse.length; i++) {
            nodesWithMultipleFailures[i] = new NetworkNode(null, "127.0.0." +i);
            nodesWithMultipleFailuresResponse[i] = new EntryCheckResponse(nodesWithMultipleFailures[i]);
            nodesWithMultipleFailuresResponse[i].setFileAvailable(true);

            for(int j = 0; j < numFailures; j++) {
                AcquisitionTaskHistoryEntry nodeWithMultipleFailuresEntry = new AcquisitionTaskHistoryEntry(
                        acquisitionFeed.entries[0].id, "http://127.0.0.2:8000/catalog/foo",
                        NetworkManager.DOWNLOAD_FROM_PEER_ON_SAME_NETWORK, failTime, failTime,
                        UstadMobileSystemImpl.DLSTATUS_FAILED);
                nodesWithMultipleFailures[i].addAcquisitionHistoryEntry(nodeWithMultipleFailuresEntry);
            }
            responseList.add(nodesWithMultipleFailuresResponse[i]);
        }

        Assert.assertNull("When too many failures have occurred result will be null",
            task.selectEntryCheckResponse(acquisitionFeed.entries[0], responseList));

        /**
         * When there is only one acceptable entryResponse = return it
         */
        responseList.clear();
        responseList.add(wifiDirectResponse);
        Assert.assertEquals("When there is one acceptable EntryResponse - it is returned",
                wifiDirectResponse, task.selectEntryCheckResponse(acquisitionFeed.entries[0],
                responseList));

        /**
         * When there is one unacceptable response - result should be null
         */
        responseList.clear();
        responseList.add(nodesWithMultipleFailuresResponse[0]);
        Assert.assertNull("When there is one unacceptable EntryResponse result is null",
            task.selectEntryCheckResponse(acquisitionFeed.entries[0], responseList));

        Assert.assertNull("When responseList is empty select response returns null",
            task.selectEntryCheckResponse(acquisitionFeed.entries[0], new ArrayList<EntryCheckResponse>()));

        Assert.assertNull("When resposneList is null select response returns null",
                task.selectEntryCheckResponse(acquisitionFeed.entries[0], null));
    }
}
