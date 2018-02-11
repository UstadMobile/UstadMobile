package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.db.dao.DownloadJobItemHistoryDao;
import com.ustadmobile.core.fs.db.ContainerFileHelper;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.ContainerFileEntry;
import com.ustadmobile.lib.db.entities.ContainerFileEntryWithContainerFile;
import com.ustadmobile.lib.db.entities.ContainerFileWithRelations;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadJobItemHistory;
import com.ustadmobile.lib.db.entities.DownloadJobWithRelations;
import com.ustadmobile.lib.db.entities.EntryStatusResponseWithNode;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.lib.util.UmUuidUtil;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.DownloadTask;
import com.ustadmobile.port.sharedse.networkmanager.LocalMirrorFinder;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.core.networkmanager.NetworkManagerListener;
import com.ustadmobile.lib.db.entities.NetworkNode;
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
import org.mockito.Mockito;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import fi.iki.elonen.router.RouterNanoHTTPD;

import static org.mockito.Mockito.*;
import static org.mockito.AdditionalMatchers.gt;

/**
 * Test the acquisition task. The OPDS feed on which the acquisition is based and the EPUBs are in
 * the resources of the sharedse-tests module.
 *
 * Created by kileha3 on 17/05/2017.
 */
public class TestDownloadTask {

    private static final int DEFAULT_ACQUIRE_TIMEOUT = 120000;//default acquire timeout: 2mins

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

    @BeforeClass
    public static void stopAllOtherDownloads() {
        DbManager.getInstance(PlatformTestUtil.getTargetContext()).getDownloadJobDao()
                .updateJobStatusByRange(0, 20, NetworkTask.STATUS_STOPPED);
    }

    @AfterClass
    public static void stopHttpResourcesServer() throws IOException {
        if(resourcesHttpd != null) {
            resourcesHttpd.stop();
            resourcesHttpd = null;
        }
    }

    public static void testAcquisition(NetworkNode remoteNode, LocalMirrorFinder mirrorFinder,
                                       boolean localNetworkEnabled, boolean wifiDirectEnabled,
                                       int expectedLocalDownloadMode, int acquireTimeout)
            throws IOException, InterruptedException,XmlPullParserException{

        final NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        manager.clearNetworkNodeAcquisitionHistory();
        final Object acquireLock = new Object();

        //make sure we don't have any of the entries in question already
        DbManager dbManager = DbManager.getInstance(PlatformTestUtil.getTargetContext());

        long startTime = System.currentTimeMillis();

        ContainerFileHelper.getInstance().deleteAllContainerFilesByEntryId(PlatformTestUtil.getTargetContext(),
                ENTRY_ID_PRESENT);
        ContainerFileHelper.getInstance().deleteAllContainerFilesByEntryId(PlatformTestUtil.getTargetContext(),
                ENTRY_ID_NOT_PRESENT);

        DownloadJob job = makeDownloadJob(manager, wifiDirectEnabled, localNetworkEnabled);
        UmLiveData<DownloadJobWithRelations> jobLiveData = dbManager.getDownloadJobDao().getByIdLive(
                job.getId());

        UmObserver<DownloadJobWithRelations> observer = (downloadJob) -> {
            synchronized (acquireLock) {
                if(downloadJob != null && downloadJob.getStatus() > 20) {
                    acquireLock.notifyAll();
                }
            }
        };

        jobLiveData.observeForever(observer);

        manager.queueDownloadJob(job.getId());
        synchronized (acquireLock){
            acquireLock.wait(acquireTimeout);
        }

        Assert.assertEquals("Download job reported as complete",
                NetworkTask.STATUS_COMPLETE, jobLiveData.getValue().getStatus());

        jobLiveData.removeObserver(observer);

        DownloadJobItem presentJobItem = jobLiveData.getValue().getJobItemByEntryId(ENTRY_ID_PRESENT);
        List<DownloadJobItemHistory> presentJobHistoryList = dbManager.getDownloadJobItemHistoryDao()
                .findHistoryItemsByDownloadJobItem(presentJobItem.getId());

        UstadMobileSystemImpl.l(UMLog.DEBUG, 646, "Test job id = " + job.getId());
        DownloadJobItemHistory lastHistoryItem = presentJobHistoryList.get(presentJobHistoryList.size()-1);
        Assert.assertEquals("Last history entry was downloaded from expected network", expectedLocalDownloadMode,
                lastHistoryItem.getMode());
        Assert.assertTrue("Last history entry was successful", lastHistoryItem.isSuccessful());
//
//        //check history was recorded on the node
//        //Assertion has failed 27/06/17 - not able to reproduce again.
        if(expectedLocalDownloadMode != NetworkManager.DOWNLOAD_FROM_CLOUD) {
            List<DownloadJobItemHistory> nodeHistoryList = dbManager.getDownloadJobItemHistoryDao()
                    .findHistoryItemsByNetworkNodeSince(remoteNode.getNodeId(), startTime);
            Assert.assertNotNull("Remote node has acquisition history", nodeHistoryList);
            Assert.assertTrue("Remote node has at least one additional acquisition history entries",
                    nodeHistoryList.size() > 0);
        }

        ContainerFileEntryWithContainerFile localContainerFile = dbManager.getContainerFileEntryDao()
                .findContainerFileEntryWithContainerFileByEntryId(ENTRY_ID_PRESENT);
        Assert.assertTrue("Downloaded container file for locally downloaded entry exists after download",
                new File(localContainerFile.getContainerFile().getNormalizedPath()).exists());

        ContainerFileEntryWithContainerFile cloudContainerFile = dbManager.getContainerFileEntryDao()
                .findContainerFileEntryWithContainerFileByEntryId(ENTRY_ID_NOT_PRESENT);
        Assert.assertTrue("Downloaded container file for cloud downloaded entry exists after download",
                new File(cloudContainerFile.getContainerFile().getNormalizedPath()).exists());
    }

    public static void testAcquisition(NetworkNode remoteNode, LocalMirrorFinder mirrorFinder, boolean localNetworkEnabled, boolean wifiDirectEnabled, int expectedLocalDownloadMode) throws IOException, InterruptedException,XmlPullParserException{
        testAcquisition(remoteNode, mirrorFinder, localNetworkEnabled, wifiDirectEnabled, expectedLocalDownloadMode, DEFAULT_ACQUIRE_TIMEOUT);
    }

    private static DownloadJob makeDownloadJob(NetworkManager manager, boolean wifiDirectDownloadEnabled,
                                               boolean lanDownloadEnabled) throws XmlPullParserException, IOException{
        //Create a Download Job manually - just insert the OpdsEntries and Links, then make the job.
        String catalogUrl = UMFileUtil.joinPaths(httpRoot,
                "com/ustadmobile/test/sharedse/test-acquisition-task-feed.opds");

        OpdsEntryWithRelations entry1 = new OpdsEntryWithRelations();
        entry1.setUuid(UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()));
        entry1.setEntryId(ENTRY_ID_PRESENT);
        entry1.setTitle("The Little Chicks");
        entry1.setUrl(catalogUrl);
        OpdsLink acquireLink1 = new OpdsLink(entry1.getUuid(), "application/epub+zip",
                "thelittlechicks.epub?speedLimit=128000", OpdsEntry.LINK_REL_ACQUIRE);
        entry1.setLinks(Arrays.asList(acquireLink1));


        OpdsEntryWithRelations entry2 = new OpdsEntryWithRelations();
        entry2.setUuid(UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()));
        entry2.setEntryId(ENTRY_ID_NOT_PRESENT);
        entry2.setTitle("I come from a cloud");
        entry2.setUrl(catalogUrl);
        OpdsLink acquireLink2 = new OpdsLink(entry2.getUuid(), "application/epub+zip",
                "icomefromacloud.epub?speedLimit=128000", OpdsEntry.LINK_REL_ACQUIRE);
        entry2.setLinks(Arrays.asList(acquireLink2));


        DbManager dbManager = DbManager.getInstance(PlatformTestUtil.getTargetContext());
        dbManager.getOpdsLinkDao().insert(Arrays.asList(acquireLink1, acquireLink2));
        dbManager.getOpdsEntryDao().insertList(Arrays.asList((OpdsEntry)entry1, entry2));


        String storageDir = UstadMobileSystemImpl.getInstance().getStorageDirs(
                CatalogPresenter.SHARED_RESOURCE, PlatformTestUtil.getTargetContext())[0].getDirURI();
        DownloadJob job = manager.buildDownloadJob(Arrays.asList(entry1, entry2), storageDir, false,
                wifiDirectDownloadEnabled, lanDownloadEnabled);
        return job;
    }


    /**
     * Main acquisition test: test downloading from the same network (e.g. use with network
     * service discovery) and test downloading using WiFi direct when not on the same network
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws XmlPullParserException
     */
    @Test
    public void testAcquisitionLocalWifi() throws IOException, InterruptedException, XmlPullParserException {
        final NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        SharedSeNetworkTestSuite.assumeNetworkHardwareEnabled();
        Assert.assertTrue("Supernode mode enabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(true));

        TestNetworkManager.testNetworkServiceDiscovery(SharedSeTestSuite.REMOTE_SLAVE_SERVER,
                TestNetworkManager.NODE_DISCOVERY_TIMEOUT);
        //TODO: Test entry status over http here


        NetworkNode remoteNode = DbManager.getInstance(PlatformTestUtil.getTargetContext())
                .getNetworkNodeDao().findNodeByIpAddress(TestConstants.TEST_REMOTE_SLAVE_SERVER);
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

        NetworkNode remoteNode = DbManager.getInstance(PlatformTestUtil.getTargetContext())
                .getNetworkNodeDao().findNodeByBluetoothAddress(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);
        testAcquisition(remoteNode, manager, false, true,
                NetworkManager.DOWNLOAD_FROM_PEER_ON_DIFFERENT_NETWORK);
        Assert.assertTrue(TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(false));
    }


    /**
     * Test what happens with acquisition over WiFi direct when the bluetooth connection to the
     * remote node fails. After a number of failures the scoring mechanism should result in the
     * download taking place from the cloud.
     *
     * @throws Exception
     */
//    @Test(timeout = 10 * 60 * 1000)//for debugging purposes - should normally complete in 30s
    public void testAcquisitionBluetoothFail() throws Exception {
        final NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        SharedSeNetworkTestSuite.assumeNetworkHardwareEnabled();
        Assert.assertTrue("Supernode mode enabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(true));
        TestNetworkManager.testWifiDirectDiscovery(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE,
                TestNetworkManager.NODE_DISCOVERY_TIMEOUT);
        TestEntryStatusTask.testEntryStatusBluetooth(TestEntryStatusTask.EXPECTED_AVAILABILITY,
                TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);

        final NetworkNode remoteNode = manager.getNodeByBluetoothAddr(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);

        final NetworkNode wrongAddressNode = new NetworkNode(remoteNode.getWifiDirectMacAddress(),
                null);
        wrongAddressNode.setBluetoothMacAddress("00:11:22:33:44:55");
        wrongAddressNode.setWifiDirectLastUpdated(Calendar.getInstance().getTimeInMillis());
//        EntryCheckResponse wrongAddressNodeResponse = new EntryCheckResponse(wrongAddressNode);
//        wrongAddressNodeResponse.setFileAvailable(true);
//        wrongAddressNodeResponse.setLastChecked(Calendar.getInstance().getTimeInMillis());
//        final List<EntryCheckResponse> entryCheckResponseList = new ArrayList<>();
//        entryCheckResponseList.add(wrongAddressNodeResponse);
//
//
//        LocalMirrorFinder mirrorFinder= new LocalMirrorFinder() {
//            @Override
//            public List<EntryCheckResponse> getEntryResponsesWithLocalFile(String entryId) {
//                if(entryId.equals(ENTRY_ID_PRESENT))
//                    return entryCheckResponseList;
//                else
//                    return null;
//            }
//        };
//
//        testAcquisition(remoteNode, mirrorFinder, false, true, NetworkManager.DOWNLOAD_FROM_CLOUD);
//        Assert.assertTrue("Supernode mode disabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(false));
    }

    /**
     *
     * @throws Exception
     */
//    @Test
    public void testAcquisitionStop() throws Exception {
//        final NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
//        SharedSeNetworkTestSuite.assumeNetworkHardwareEnabled();
//        CatalogPresenter.removeEntry(ENTRY_ID_PRESENT, CatalogPresenter.SHARED_RESOURCE,
//                PlatformTestUtil.getTargetContext());
//        CatalogPresenter.removeEntry(ENTRY_ID_NOT_PRESENT, CatalogPresenter.SHARED_RESOURCE,
//                PlatformTestUtil.getTargetContext());
//
//        Assert.assertTrue("Supernode mode enabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(true));
//
//        TestNetworkManager.testWifiDirectDiscovery(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE,
//                TestNetworkManager.NODE_DISCOVERY_TIMEOUT);
//        TestNetworkManager.testNetworkServiceDiscovery(SharedSeTestSuite.REMOTE_SLAVE_SERVER,
//                TestNetworkManager.NODE_DISCOVERY_TIMEOUT);
//
//
//        NetworkNode remoteNode = manager.getNodeByBluetoothAddr(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);
//        UstadJSOPDSFeed feed = makeAcquisitionTestFeed();
//        manager.requestAcquisition(feed, manager, false, false);
//        DownloadTask task = manager.getAcquisitionTaskByEntryId(ENTRY_ID_PRESENT);
//        try { Thread.sleep(1000); }
//        catch(InterruptedException e){}
//        task.stop(NetworkTask.STATUS_STOPPED);
//        try { Thread.sleep(3000); }
//        catch(InterruptedException e){}
//        //TODO: Fix me - what's happening: Acquisition Task is being restarted when stopped, timer task is null on acquisitiontask.java line 325
//
//        Assert.assertEquals("Task status is stopped", NetworkTask.STATUS_STOPPED, task.getStatus());
//        Assert.assertTrue("Task is stopped", task.isStopped());
//        CatalogEntryInfo presentEntryInfo = CatalogPresenter.getEntryInfo(ENTRY_ID_PRESENT,
//                CatalogPresenter.SHARED_RESOURCE, PlatformTestUtil.getTargetContext());
//        Assert.assertEquals("Entry 1 not acquired", CatalogPresenter.STATUS_NOT_ACQUIRED,
//                presentEntryInfo.acquisitionStatus);
//        CatalogEntryInfo notPresentEntryInfo = CatalogPresenter.getEntryInfo(ENTRY_ID_NOT_PRESENT,
//                CatalogPresenter.SHARED_RESOURCE, PlatformTestUtil.getTargetContext());
//        Assert.assertEquals("Entry 2 not acquired", CatalogPresenter.STATUS_NOT_ACQUIRED,
//                notPresentEntryInfo.acquisitionStatus);
//        Assert.assertTrue("Supernode mode disabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(false));

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
        wifiDirectNode.setNodeId(0);
        wifiDirectNode.setWifiDirectLastUpdated(timeNow);
        EntryStatusResponseWithNode wifiDirectResponse = new EntryStatusResponseWithNode();
        wifiDirectResponse.setAvailable(true);
        wifiDirectResponse.setNetworkNode(wifiDirectNode);

        NetworkNode sameNetworkNode = new NetworkNode(null, "127.0.0.2");
        sameNetworkNode.setNodeId(1);
        sameNetworkNode.setNetworkServiceLastUpdated(timeNow);
        sameNetworkNode.setNsdServiceName("lan-node");

        EntryStatusResponseWithNode sameNetworkResponse = new EntryStatusResponseWithNode(sameNetworkNode);
        sameNetworkResponse.setAvailable(true);
        sameNetworkResponse.setNetworkNode(sameNetworkNode);

        ArrayList<EntryStatusResponseWithNode> responseList = new ArrayList<>();
        responseList.add(wifiDirectResponse);
        responseList.add(sameNetworkResponse);

        DownloadJobItemHistoryDao mockHistoryDao1 = mock(DownloadJobItemHistoryDao.class);


        DownloadJobItemHistory historyItem1 = new DownloadJobItemHistory();
        ArrayList<DownloadJobItemHistory> list1 = new ArrayList<>();
        list1.add(historyItem1);
        when(mockHistoryDao1.findHistoryItemsByNetworkNodeSince(anyInt(), anyLong()))
                .thenReturn(new ArrayList<DownloadJobItemHistory>());

        DownloadTask task = new DownloadTask(null,
                (NetworkManager)UstadMobileSystemImpl.getInstance().getNetworkManager());
        Assert.assertEquals("When WiFi direct and local network responses are available, local network response will be chosen",
                sameNetworkResponse, task.selectEntryStatusResponse(responseList, mockHistoryDao1));


        /*
         * When given a choice between a node with failures and a node without - select the node without recent failures
         */
        NetworkNode nodeWithoutFailures = new NetworkNode(null, "127.0.0.1");
        nodeWithoutFailures.setNodeId(3);
        nodeWithoutFailures.setNetworkServiceLastUpdated(timeNow);
        nodeWithoutFailures.setNsdServiceName("without-failures");
        EntryStatusResponseWithNode nodeWithoutFailuresResponse = new EntryStatusResponseWithNode(nodeWithoutFailures);
        nodeWithoutFailuresResponse.setAvailable(true);

        NetworkNode nodeWithFailures= new NetworkNode(null, "127.0.0.2");
        nodeWithFailures.setNetworkServiceLastUpdated(timeNow);
        nodeWithoutFailures.setNsdServiceName("with-failures");
        EntryStatusResponseWithNode nodeWithFailuresResponse = new EntryStatusResponseWithNode(nodeWithFailures);
        nodeWithFailuresResponse.setAvailable(true);
        long failureTime = timeNow - 20000;//20 seconds ago

        DownloadJobItemHistory failureHistoryEntry = new DownloadJobItemHistory(
                nodeWithFailures.getNodeId(), NetworkManager.DOWNLOAD_FROM_PEER_ON_SAME_NETWORK,
                false, failureTime - 10000, failureTime);
        ArrayList<DownloadJobItemHistory> failureList = new ArrayList<>();
        failureList.add(failureHistoryEntry);
        DownloadJobItemHistoryDao historyDao2 = Mockito.mock(DownloadJobItemHistoryDao.class);
        when(historyDao2.findHistoryItemsByNetworkNodeSince(eq(nodeWithFailures.getNodeId()), gt(0L)))
                .thenReturn(failureList);

        responseList.clear();
        responseList.add(nodeWithFailuresResponse);
        responseList.add(nodeWithoutFailuresResponse);
        Assert.assertEquals("When downloading prefer a network node without recent failures",
                nodeWithoutFailuresResponse, task.selectEntryStatusResponse(responseList,
                historyDao2));

        /*
         * When nodes have failed - choose the node where it has been the longest time since that failure
         */
        responseList.clear();
        NetworkNode[] nodesWithFailures = new NetworkNode[4];
        EntryStatusResponseWithNode[] nodeWithFailuresResponses = new EntryStatusResponseWithNode[4];
        DownloadJobItemHistoryDao historyDao3 = Mockito.mock(DownloadJobItemHistoryDao.class);
        for(int i = 0; i < nodesWithFailures.length; i++) {
            nodesWithFailures[i] = new NetworkNode(null, "127.0.0." + i);
            nodesWithFailures[i].setNodeId(i);
            nodesWithFailures[i].setNetworkServiceLastUpdated(timeNow);
            nodesWithFailures[i].setNsdServiceName("node-with-failures-"+i);
            failureTime = timeNow - (((long)DownloadTask.FAILURE_MEMORY_TIME / 4)*i);
            DownloadJobItemHistory nodeWithFailuresHistory = new DownloadJobItemHistory(
                    nodesWithFailures[i].getNodeId(), NetworkManager.DOWNLOAD_FROM_PEER_ON_SAME_NETWORK,
                    false, failureTime, failureTime);

            when(historyDao3.findHistoryItemsByNetworkNodeSince(eq(i), gt(0L)))
                    .thenReturn(Arrays.asList(nodeWithFailuresHistory));

            nodeWithFailuresResponses[i] = new EntryStatusResponseWithNode(nodesWithFailures[i]);
            nodeWithFailuresResponses[i].setAvailable(true);
            responseList.add(nodeWithFailuresResponses[i]);
        }

        Assert.assertEquals("When nodes have failed choose the node where the failure was least recent",
                nodeWithFailuresResponses[3], task.selectEntryStatusResponse(responseList, historyDao3));

        /**
         * When update times are too old or there are too many failures - null should be returned
         */
        DownloadJobItemHistoryDao mockHistoryDao4 = mock(DownloadJobItemHistoryDao.class);
        NetworkNode[] nodesWithMultipleFailures = new NetworkNode[3];
        EntryStatusResponseWithNode[] nodesWithMultipleFailuresResponse = new EntryStatusResponseWithNode[3];
        responseList.clear();
        long failTime = timeNow - (long)(DownloadTask.FAILURE_MEMORY_TIME / 4);
        int numFailures = 4;
        for(int i = 0; i < nodesWithMultipleFailuresResponse.length; i++) {
            nodesWithMultipleFailures[i] = new NetworkNode(null, "127.0.0." +i);
            nodesWithMultipleFailures[i].setNodeId(i);
            nodesWithMultipleFailuresResponse[i] = new EntryStatusResponseWithNode(nodesWithMultipleFailures[i]);
            nodesWithMultipleFailuresResponse[i].setAvailable(true);

            ArrayList<DownloadJobItemHistory> itemHistoryList = new ArrayList<>();
            for(int j = 0; j < numFailures; j++) {
                DownloadJobItemHistory itemHistory = new DownloadJobItemHistory(
                        nodesWithMultipleFailures[i].getNodeId(), NetworkManager.DOWNLOAD_FROM_PEER_ON_SAME_NETWORK,
                        false, failTime, failTime);
                itemHistoryList.add(itemHistory);
            }
            responseList.add(nodesWithMultipleFailuresResponse[i]);
            when(mockHistoryDao4.findHistoryItemsByNetworkNodeSince(eq(i), gt(0L)))
                .thenReturn(itemHistoryList);
        }

        Assert.assertNull("When too many failures have occurred result will be null",
            task.selectEntryStatusResponse(responseList, mockHistoryDao4));

        /**
         * When there is only one acceptable entryResponse = return it
         */
        responseList.clear();
        responseList.add(wifiDirectResponse);
        Assert.assertEquals("When there is one acceptable EntryResponse - it is returned",
                wifiDirectResponse, task.selectEntryStatusResponse(responseList,
                mock(DownloadJobItemHistoryDao.class)));

        /**
         * When there is one unacceptable response - result should be null
         */
        responseList.clear();
        responseList.add(nodesWithMultipleFailuresResponse[0]);

        Assert.assertNull("When there is one unacceptable EntryResponse result is null",
            task.selectEntryStatusResponse(responseList, mockHistoryDao4));

        Assert.assertNull("When responseList is empty select response returns null",
            task.selectEntryStatusResponse(new ArrayList<EntryStatusResponseWithNode>(), mockHistoryDao4));

        Assert.assertNull("When responseList is null select response returns null",
                task.selectEntryStatusResponse(null, mockHistoryDao4));
    }
}
