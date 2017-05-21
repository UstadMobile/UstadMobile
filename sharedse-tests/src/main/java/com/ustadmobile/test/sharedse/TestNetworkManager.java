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
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.ustadmobile.test.core.buildconfig.TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE;
import static com.ustadmobile.test.core.buildconfig.TestConstants.TEST_REMOTE_SLAVE_SERVER_PORT;

/**
 * Created by kileha3 on 16/05/2017.
 */

public class TestNetworkManager {

    public static final int NODE_DISCOVERY_TIMEOUT =(2*60 * 1000)+2000;//2min2sec in ms


    @Test
    public void testDiscovery() throws IOException{
        NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();

        Assert.assertTrue("Bluetooth enabled : required to test discovery", manager.isBluetoothEnabled());
        Assert.assertTrue("WiFi enabled: required to test discovery", manager.isWiFiEnabled());

        final Object nodeDiscoveryLock = new Object();
        NetworkManagerListener responseListener = new NetworkManagerListener() {
            @Override
            public void fileStatusCheckInformationAvailable(List<String> fileIds) {

            }

            @Override
            public void entryStatusCheckCompleted(NetworkTask task) {

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
            public void fileAcquisitionInformationAvailable(String entryId, long downloadId, int downloadSource) {

            }
        };
        manager.addNetworkManagerListener(responseListener);

        //enable supernode mode on the remote test device
        String enableNodeUrl = PlatformTestUtil.getRemoteTestEndpoint() + "?cmd=SUPERNODE&enabled=true";
        HTTPResult result = UstadMobileSystemImpl.getInstance().makeRequest(enableNodeUrl, null, null);
        Assert.assertEquals("Supernode mode reported as enabled", 200, result.getStatus());

        if(manager.getNodeByBluetoothAddr(TEST_REMOTE_BLUETOOTH_DEVICE) == null) {
            synchronized (nodeDiscoveryLock) {
                try { nodeDiscoveryLock.wait(NODE_DISCOVERY_TIMEOUT ); }
                catch(InterruptedException e ) {
                    e.printStackTrace();
                }
            }
        }

        //disable supernode mode on the remote test device
        String disableNodeUrl =PlatformTestUtil.getRemoteTestEndpoint()+  "?cmd=SUPERNODE&enabled=false";
        result = UstadMobileSystemImpl.getInstance().makeRequest(disableNodeUrl, null, null);
        Assert.assertEquals("Supernode mode reported as enabled", 200, result.getStatus());

        Assert.assertNotNull("Remote test slave node discovered",
                manager.getNodeByBluetoothAddr(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE));

    }


}
