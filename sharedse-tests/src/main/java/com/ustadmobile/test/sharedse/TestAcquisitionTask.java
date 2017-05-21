package com.ustadmobile.test.sharedse;

import com.ustadmobile.core.impl.AcquisitionManager;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerListener;
import com.ustadmobile.port.sharedse.networkmanager.NetworkNode;
import com.ustadmobile.port.sharedse.networkmanager.NetworkTask;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.ustadmobile.test.core.buildconfig.TestConstants.TEST_REMOTE_SLAVE_SERVER_PORT;
import static org.hamcrest.CoreMatchers.is;

/**
 * Created by kileha3 on 17/05/2017.
 */

public class TestAcquisitionTask{
    private static final int DEFAULT_WAIT_TIME =20000;
    private static final String FEED_SRC_URL ="opds:///com.ustadmobile.app.devicefeed";
    private static final String FEED_TITLE="The Little Chicks";
    private static final String FEED_ENTRY_UPDATED="2016-11-04T13:38:49Z";

    private static final String FEED_LINK_MIME ="application/dir";
    private static final String FEED_LINK_HREF ="/storage/emulated/0/ustadmobileContent";

    private static final String ENTRY_LINK_REL="http://opds-spec.org/acquisition";
    private static final String ENTRY_LINK_MIME="application/epub+zip";
    private static final String ENTRY_LINK_HREF="/media/eXeUpload/d3288c3b-89b3-4541-a1f0-13ccf0b0eacc.um.TheLittleChicks.epub";
    private static final String ENTRY_ID="202b10fe-b028-4b84-9b84-852aa766607d";
    private static final String ENTRY_ID_NOT_PRESENT = "202b10fe-b028-4b84-9b84-852aa766607dx";
    public static final String[] ENTRY_IDS = new String[]{ENTRY_ID, ENTRY_ID_NOT_PRESENT};

    ArrayList<HashMap<String,Integer>> downloadSources=new ArrayList<>();


    public void testAcquisition() throws IOException, InterruptedException {
        final NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        Assert.assertTrue("Bluetooth enabled : required to test discovery", manager.isBluetoothEnabled());
        Assert.assertTrue("WiFi enabled: required to test discovery", manager.isWiFiEnabled());

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
                //TODO; Notify here
                synchronized (statusRequestLock){
                    statusRequestLock.notify();
                }
            }

            @Override
            public void networkNodeDiscovered(NetworkNode node) {
                if(node.getDeviceBluetoothMacAddress().equals(
                        TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE)){
                    synchronized (nodeDiscoveryLock){
                        nodeDiscoveryLock.notify();
                    }
                }
            }

            @Override
            public void fileAcquisitionInformationAvailable(String entryId, long downloadId, int source) {
                for(String entry:ENTRY_IDS){
                    if(entryId.equals(entry)){
                        HashMap<String,Integer> map=new HashMap<>();
                        map.put(entryId,source);
                        if(!downloadSources.contains(map)){
                            downloadSources.add(map);
                        }
                    }
                }

                if(downloadSources.size()==2){
                    acquisitionLock.notify();
                }
            }


        };
        manager.addNetworkManagerListener(responseListener);
        //enable supernode mode on the remote test device
        String enableNodeUrl = "http://"+ PlatformTestUtil.getRemoteTestEndpoint() +":"
                + TEST_REMOTE_SLAVE_SERVER_PORT + "/?cmd=SUPERNODE&enabled=true";
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
        String disableNodeUrl = "http://"+ PlatformTestUtil.getRemoteTestEndpoint() +":"
                + TEST_REMOTE_SLAVE_SERVER_PORT + "/?cmd=SUPERNODE&enabled=false";
        result = UstadMobileSystemImpl.getInstance().makeRequest(disableNodeUrl, null, null);
        Assert.assertEquals("Supernode mode reported as enabled", 200, result.getStatus());

        manager.requestFileStatus(entryLIst,manager.getContext(),nodeList);
        synchronized (statusRequestLock){
            statusRequestLock.wait(DEFAULT_WAIT_TIME*6);
        }

        Assert.assertTrue("Available entry reported as locally available", fileAvailable[0]);
        Assert.assertFalse("Unavailable entry reported as not available", fileAvailable[1]);

        //Create a feed manually
        UstadJSOPDSFeed feed=new UstadJSOPDSFeed(FEED_SRC_URL,FEED_TITLE,ENTRY_ID);
        feed.addLink(AcquisitionManager.LINK_REL_DOWNLOAD_DESTINATION,
                FEED_LINK_MIME, FEED_LINK_HREF);
        feed.addLink(UstadJSOPDSItem.LINK_REL_SELF_ABSOLUTE, UstadJSOPDSItem.TYPE_ACQUISITIONFEED,
                "http://"+ PlatformTestUtil.getRemoteTestEndpoint() +":"
                        + TEST_REMOTE_SLAVE_SERVER_PORT +"/catalog/acquire.opds");
        UstadJSOPDSEntry entry=new UstadJSOPDSEntry(feed);

        for (String entryId : ENTRY_IDS) {
            entry.id = entryId;
            entry.title = FEED_TITLE;
            entry.updated = FEED_ENTRY_UPDATED;
            entry.addLink(ENTRY_LINK_REL, ENTRY_LINK_MIME, ENTRY_LINK_HREF);
        }

        feed.addEntry(entry);
        manager.requestAcquisition(feed,manager.getContext());

        synchronized (acquisitionLock){
            acquisitionLock.wait(DEFAULT_WAIT_TIME*6);
        }
        Assert.assertThat("Available entry reported,can be downloaded locally",
                downloadSources.get(0).get(ENTRY_IDS[0]),is(NetworkManager.DOWNLOAD_SOURCE_PEER_SAME_NETWORK));
        Assert.assertThat("Unavailable entry reported,can be downloaded from cloud",
                downloadSources.get(1).get(ENTRY_IDS[1]),is(NetworkManager.DOWNLOAD_SOURCE_CLOUD));
    }
}
