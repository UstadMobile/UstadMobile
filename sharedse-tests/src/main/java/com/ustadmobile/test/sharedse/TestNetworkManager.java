package com.ustadmobile.test.sharedse;

import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UMLog;
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
            public void networkTaskStatusChanged(NetworkTask task) {

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
            public void networkTaskStatusChanged(NetworkTask task) {

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


    /**
     * Test disabling wifi on the client
     */
    @Test
    public void testWifiDisabledOnClient() throws IOException {
        NetworkManager manager = UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        final Object nodeUpdateLock = new Object();
        final Object networkConnectedLock = new Object();

        final long[] timeEnabled = new long[]{-1};
        NetworkManagerListener listener = new NetworkManagerListener() {
            @Override
            public void fileStatusCheckInformationAvailable(List<String> fileIds) {

            }

            @Override
            public void networkNodeDiscovered(NetworkNode node) {

            }

            @Override
            public void networkNodeUpdated(NetworkNode node) {
                if(timeEnabled[0] == -1)
                    return;//not ready yet

                if(node.getDeviceBluetoothMacAddress().equals(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE)) {
                    long wifiDirectUpdated = node.getWifiDirectLastUpdated();
                    long nsdUpdated = node.getNetworkServiceLastUpdated();
                    //TODO: check both NSD and wifi direct
                    if(wifiDirectUpdated > timeEnabled[0]) {
                        synchronized (nodeUpdateLock) {
                            nodeUpdateLock.notify();
                        }
                    }
                }
            }

            @Override
            public void fileAcquisitionInformationAvailable(String entryId, long downloadId, int downloadSource) {

            }

            @Override
            public void wifiConnectionChanged(String ssid, boolean connected, boolean connectedOrConnecting) {
                if(connected) {
                    synchronized (networkConnectedLock) {
                        networkConnectedLock.notify();
                    }
                }
            }

            @Override
            public void networkTaskStatusChanged(NetworkTask networkTask) {

            }
        };
        manager.addNetworkManagerListener(listener);

        try {
            Assert.assertTrue("Supernode enabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(true));
            testWifiDirectDiscovery(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE, NODE_DISCOVERY_TIMEOUT);

            Assume.assumeTrue("Can disable wifi", manager.setWifiEnabled(false));
            UstadMobileSystemImpl.l(UMLog.INFO, 302, "=== wifi disabled===");
            try { Thread.sleep(1000); }
            catch(InterruptedException e) {}
            //manager.stopP2P();
            try { Thread.sleep(1000); }
            catch(InterruptedException e) {}
            manager.setWifiEnabled(true);
            timeEnabled[0] = Calendar.getInstance().getTimeInMillis();
            UstadMobileSystemImpl.l(UMLog.INFO, 302, "=== wifi enabled===");
            try { Thread.sleep(20000); }
            catch(InterruptedException e) {}
            //manager.startP2P();
            //manager.startClientMode();
            manager.updateClientServices();


            synchronized (nodeUpdateLock) {
                try { nodeUpdateLock.wait(NODE_DISCOVERY_TIMEOUT); }
                catch(InterruptedException e) {}
            }

            NetworkNode node = manager.getNodeByBluetoothAddr(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);
            boolean updatedAfterWifiDisabled = node.getWifiDirectLastUpdated() > timeEnabled[0];
            Assert.assertTrue("Wifi direct last updated after WiFi enabled",
                    updatedAfterWifiDisabled);
            //TODO: check NSD as well
//            Assert.assertTrue("Network service discovery last updated after wifi enabled",
//                    node.getNetworkServiceLastUpdated() > timeEnabled[0]);
        }finally{
            //Assert.assertTrue("Supernode enabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(false));
            if(manager.getCurrentWifiSsid() == null) {
                synchronized (networkConnectedLock) {
                    try { networkConnectedLock.wait(NODE_DISCOVERY_TIMEOUT); }
                    catch(InterruptedException e) {}
                }
            }
            manager.removeNetworkManagerListener(listener);
        }

    }




}
