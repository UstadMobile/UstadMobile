package com.ustadmobile.test.sharedse;

import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerListener;
import com.ustadmobile.port.sharedse.networkmanager.NetworkNode;
import com.ustadmobile.port.sharedse.networkmanager.NetworkTask;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.ustadmobile.test.core.buildconfig.TestConstants.TEST_REMOTE_SLAVE_SERVER_PORT;

/**
 * Created by kileha3 on 16/05/2017.
 */

public class TestEntryStatusTask{



    public static final String ENTRY_ID="202b10fe-b028-4b84-9b84-852aa766607d";

    public static final String ENTRY_ID_NOT_PRESENT = "202b10fe-b028-4b84-9b84-852aa766607dx";

    public static final String[] ENTRY_IDS = new String[]{ENTRY_ID, ENTRY_ID_NOT_PRESENT};

    private static final int DEFAULT_WAIT_TIME =10000;



    @Test
    public void testEntryStatusBluetooth() throws IOException, InterruptedException {
        final NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        Assume.assumeTrue("Network test wifi and bluetooth enabled",
                manager.isBluetoothEnabled() && manager.isWiFiEnabled());

        final boolean[] fileAvailable = new boolean[ENTRY_IDS.length];
        final Object nodeDiscoveryLock = new Object();
        final Object statusRequestLock=new Object();
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
                if(node.getDeviceBluetoothMacAddress() != null &&
                    node.getDeviceBluetoothMacAddress().equals(
                    TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE)){

                    synchronized (nodeDiscoveryLock){
                       nodeDiscoveryLock.notify();
                   }
                }
            }

            @Override
            public void fileAcquisitionInformationAvailable(String entryId, long downloadId, int downloadSource) {

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
        for(int i = 0; i < ENTRY_IDS.length; i++) {
            entryLIst.add(ENTRY_IDS[i]);
        }

        //disable supernode mode on the remote test device
        String disableNodeUrl = PlatformTestUtil.getRemoteTestEndpoint() + "?cmd=SUPERNODE&enabled=false";
        result = UstadMobileSystemImpl.getInstance().makeRequest(disableNodeUrl, null, null);
        Assert.assertEquals("Supernode mode reported as enabled", 200, result.getStatus());

        manager.requestFileStatus(entryLIst,manager.getContext(),nodeList, true, false);
        synchronized (statusRequestLock){
            statusRequestLock.wait(DEFAULT_WAIT_TIME*6);
        }

        Assert.assertTrue("Available entry reported as locally available", fileAvailable[0]);
        Assert.assertFalse("Unavailable entry reported as not available", fileAvailable[1]);
    }


}
