package com.ustadmobile.test.sharedse;

import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.core.networkmanager.NetworkManagerListener;
import com.ustadmobile.core.networkmanager.NetworkNode;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

/**
 * Created by kileha3 on 16/05/2017.
 */

public class TestNetworkManager {
    public static final int NODE_DISCOVERY_TIMEOUT =(2*60 * 1000)+2000;//2min2sec in ms

    @Test
    public void testWifiDirectDiscovery() throws IOException{
        //enable supernode mode on the remote test device
        Assert.assertTrue("Supernode enabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(true));
        testWifiDirectDiscovery(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE, NODE_DISCOVERY_TIMEOUT);
        Assert.assertTrue("Supernode disabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(false));
    }


    /**
     * Test discovery over WiFi direct and ensure that the given WiFi direct device has been discovered
     *
     * @param bluetoothAddr Bluetooth address of wifi direct device to discover
     * @param timeout Timeout to wait for discovery to occur
     * @throws IOException
     */
    public static void testWifiDirectDiscovery(final String bluetoothAddr, final int timeout) throws IOException{
        NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();

        Assume.assumeTrue("Network test is enabled: wifi and bluetooth enabled",
                manager.isBluetoothEnabled() && manager.isWiFiEnabled());

        final Object nodeDiscoveryLock = new Object();
        NetworkManagerListener responseListener = new NetworkManagerListener() {
            @Override
            public void fileStatusCheckInformationAvailable(List<String> fileIds) {

            }

            @Override
            public void networkTaskCompleted(NetworkTask task) {

            }

            @Override
            public void networkNodeDiscovered(NetworkNode node) {
                if(node.getDeviceBluetoothMacAddress()!=null && node.getDeviceBluetoothMacAddress().equals(
                        bluetoothAddr)){
                    synchronized (nodeDiscoveryLock){
                        nodeDiscoveryLock.notify();
                    }
                }
            }

            @Override
            public void networkNodeUpdated(NetworkNode node) {
                if(node.getDeviceBluetoothMacAddress()!=null && node.getDeviceBluetoothMacAddress().equals(
                        bluetoothAddr)){
                    synchronized (nodeDiscoveryLock){
                        nodeDiscoveryLock.notify();
                    }
                }
            }

            @Override
            public void fileAcquisitionInformationAvailable(String entryId, long downloadId, int downloadSource) {

            }

            @Override
            public void wifiConnectionChanged(String ssid, boolean connected, boolean connectedOrConnecting) {

            }


        };
        manager.addNetworkManagerListener(responseListener);


        if(manager.getNodeByBluetoothAddr(bluetoothAddr) == null) {
            synchronized (nodeDiscoveryLock) {
                try { nodeDiscoveryLock.wait(timeout ); }
                catch(InterruptedException e ) {
                    e.printStackTrace();
                }
            }
        }

        Assert.assertNotNull("Remote wifi direct device discovered",
                manager.getNodeByBluetoothAddr(bluetoothAddr));
        manager.removeNetworkManagerListener(responseListener);
    }


    @Test
    public void testNetworkServiceDiscovery() throws IOException{
        Assert.assertTrue("Test slave supernode enabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(true));
        testNetworkServiceDiscovery(SharedSeTestSuite.REMOTE_SLAVE_SERVER, NODE_DISCOVERY_TIMEOUT);
        Assert.assertTrue("Test slave supernode enabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(false));
    }


    /**
     * Test using the standard network service discovery.
     *
     * Devices *MUST* be on the same network to be able to receive udp broadcasts from each other.
     *
     * Some older versions of Android have buggy implementations of Network Service Discovery; see
     *  https://issuetracker.google.com/issues/36952181
     *
     * @param ipAddress IP address to be discovered
     * @throws IOException
     */
    public static void testNetworkServiceDiscovery(String ipAddress, int timeout) {
        NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();

        Assume.assumeTrue("Network test wifi and bluetooth enabled",
                manager.isBluetoothEnabled() && manager.isWiFiEnabled());

        //enable supernode mode on the remote test device
//        String enableNodeUrl = PlatformTestUtil.getRemoteTestEndpoint() + "?cmd=SUPERNODE&enabled=true";
//        HTTPResult result = UstadMobileSystemImpl.getInstance().makeRequest(enableNodeUrl, null, null);
//        Assert.assertEquals("Supernode mode reported as enabled", 200, result.getStatus());

        final Object nodeNSDiscoveryLock = new Object();
        NetworkManagerListener responseListener = new NetworkManagerListener() {
            @Override
            public void fileStatusCheckInformationAvailable(List<String> fileIds) {

            }

            @Override
            public void networkTaskCompleted(NetworkTask task) {

            }

            @Override
            public void networkNodeDiscovered(NetworkNode node) {
                if(node.getDeviceIpAddress().equals(SharedSeTestSuite.REMOTE_SLAVE_SERVER) &&
                        (Calendar.getInstance().getTimeInMillis()-node.getNetworkServiceLastUpdated()) < NODE_DISCOVERY_TIMEOUT){
                    synchronized (nodeNSDiscoveryLock){
                        nodeNSDiscoveryLock.notify();
                    }
                }
            }

            @Override
            public void networkNodeUpdated(NetworkNode node) {
                if(node.getDeviceIpAddress().equals(SharedSeTestSuite.REMOTE_SLAVE_SERVER) &&
                        (Calendar.getInstance().getTimeInMillis()-node.getNetworkServiceLastUpdated()) < NODE_DISCOVERY_TIMEOUT){
                    synchronized (nodeNSDiscoveryLock){
                        nodeNSDiscoveryLock.notify();
                    }
                }
            }

            @Override
            public void fileAcquisitionInformationAvailable(String entryId, long downloadId, int downloadSource) {

            }

            @Override
            public void wifiConnectionChanged(String ssid, boolean connected, boolean connectedOrConnecting) {

            }

        };
        manager.addNetworkManagerListener(responseListener);

        NetworkNode node =manager.getNodeByIpAddress(ipAddress);
        long timeSinceNsd =(Calendar.getInstance().getTimeInMillis() - (node != null ? node.getNetworkServiceLastUpdated() : 0));
        if(node == null || timeSinceNsd > NODE_DISCOVERY_TIMEOUT) {
            synchronized (nodeNSDiscoveryLock) {
                try { nodeNSDiscoveryLock.wait(timeout ); }
                catch(InterruptedException e ) {
                    e.printStackTrace();
                }
            }
        }

        node=manager.getNodeByIpAddress(ipAddress);
        Assert.assertNotNull("Remote test slave node discovered via Network Service Discovery", node);
        boolean isWithinDiscoveryTimeRange=
                (Calendar.getInstance().getTimeInMillis()-node.getNetworkServiceLastUpdated()) < NODE_DISCOVERY_TIMEOUT;
        Assert.assertThat("Was node discovered withing time range",isWithinDiscoveryTimeRange,is(true));
        manager.removeNetworkManagerListener(responseListener);
    }




}
