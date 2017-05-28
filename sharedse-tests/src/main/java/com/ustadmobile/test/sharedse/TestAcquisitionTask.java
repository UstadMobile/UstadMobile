package com.ustadmobile.test.sharedse;

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.impl.AcquisitionManager;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerListener;
import com.ustadmobile.port.sharedse.networkmanager.NetworkNode;
import com.ustadmobile.port.sharedse.networkmanager.NetworkTask;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import fi.iki.elonen.router.RouterNanoHTTPD;

import static com.ustadmobile.test.core.buildconfig.TestConstants.TEST_REMOTE_SLAVE_SERVER_PORT;
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

    private ArrayList<HashMap<String,Integer>> downloadSources=new ArrayList<>();

    private static RouterNanoHTTPD resourcesHttpd;

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

    @Test
    public void testAcquisition() throws IOException, InterruptedException, XmlPullParserException {
        final NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        Assume.assumeTrue("Network test wifi and bluetooth enabled",
                manager.isBluetoothEnabled() && manager.isWiFiEnabled());

        final boolean[] fileAvailable = new boolean[ENTRY_IDS.length];
        final Object nodeDiscoveryLock = new Object();
        final Object statusRequestLock=new Object();
        final Object acquisitionLock=new Object();
        NetworkManagerListener responseListener = new NetworkManagerListener() {
            @Override
            public void fileStatusCheckInformationAvailable(List<String> fileIds) {
                for(int i = 0; i < ENTRY_IDS.length; i++){
                    if(fileIds.contains(ENTRY_IDS[i])) {
                        fileAvailable[i] = manager.isFileAvailable(ENTRY_IDS[i]);
                    }
                }
            }

            @Override
            public void entryStatusCheckCompleted(NetworkTask task) {
                synchronized (statusRequestLock){
                    statusRequestLock.notify();
                }
            }

            @Override
            public void networkNodeDiscovered(NetworkNode node) {
                if(node.getDeviceBluetoothMacAddress() != null &&
                        node.getDeviceBluetoothMacAddress().equals(
                                TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE)){

                    synchronized (nodeDiscoveryLock){
                        nodeDiscoveryLock.notify();
                    }
                }
            }

            @Override
            public void networkNodeUpdated(NetworkNode node) {

            }

            @Override
            public void fileAcquisitionInformationAvailable(String entryId, long downloadId, int sources) {
                HashMap<String,Integer> sourceData=new HashMap<>();
                sourceData.put(entryId,sources);
                if(!downloadSources.contains(sourceData)){
                    downloadSources.add(sourceData);
                }

                if(downloadSources.size()>=1){
                    synchronized (acquisitionLock){
                       acquisitionLock.notify();
                    }
                }
            }

            @Override
            public void wifiConnectionChanged(String ssid) {

            }


        };
        manager.addNetworkManagerListener(responseListener);
        //enable supernode mode on the remote test device
        String enableNodeUrl = PlatformTestUtil.getRemoteTestEndpoint() + "?cmd=SUPERNODE&enabled=true";
        HTTPResult result = UstadMobileSystemImpl.getInstance().makeRequest(enableNodeUrl, null, null);
        Assert.assertEquals("Supernode mode reported as enabled", 200, result.getStatus());
        if(manager.getNodeByBluetoothAddr(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE)==null){
            synchronized (nodeDiscoveryLock){
                nodeDiscoveryLock.wait(TestNetworkManager.NODE_DISCOVERY_TIMEOUT);
            }
        }

        NetworkNode networkNode=manager.getNodeByBluetoothAddr(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);
        Assert.assertNotNull("Remote test slave node discovered", networkNode);

        List<NetworkNode> nodeList=new ArrayList<>();
        nodeList.add(networkNode);

        List<String> entryLIst=new ArrayList<>();
        Collections.addAll(entryLIst, ENTRY_IDS);

        //disable supernode mode on the remote test device
        String disableNodeUrl = PlatformTestUtil.getRemoteTestEndpoint() + "?cmd=SUPERNODE&enabled=false";
        result = UstadMobileSystemImpl.getInstance().makeRequest(disableNodeUrl, null, null);
        Assert.assertEquals("Supernode mode reported as enabled", 200, result.getStatus());

        manager.requestFileStatus(entryLIst,manager.getContext(),nodeList, true, false);
        synchronized (statusRequestLock){
            statusRequestLock.wait(DEFAULT_WAIT_TIME*2);
        }

        Assert.assertTrue("Available entry reported as locally available", fileAvailable[0]);
        Assert.assertFalse("Unavailable entry reported as not available",  fileAvailable[1]);

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

        manager.requestAcquisition(feed,manager.getContext());

        synchronized (acquisitionLock){
            acquisitionLock.wait(DEFAULT_WAIT_TIME*10);
        }
        Assert.assertThat("Available entry reported,can be downloaded locally",
                downloadSources.get(0).get(ENTRY_IDS[0]),is(NetworkManager.DOWNLOAD_FROM_PEER_ON_SAME_NETWORK));
        Assert.assertThat("Unavailable entry reported,can be downloaded from cloud",
                downloadSources.get(1).get(ENTRY_IDS[1]),is(NetworkManager.DOWNLOAD_FROM_CLOUD));
    }
}
