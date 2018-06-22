package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.NetworkNodeDao;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.core.networkmanager.NetworkManagerListener;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.test.core.annotation.PeerServerRequiredTest;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.core.impl.PlatformTestUtil;
import com.ustadmobile.test.sharedse.SharedSeTestSuite;
import com.ustadmobile.test.sharedse.TestUtilsSE;
import com.ustadmobile.test.sharedse.http.RemoteTestServerHttpd;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

/**
 * Created by kileha3 on 16/05/2017.
 */
@PeerServerRequiredTest
public class TestNetworkManager {
    public static final int NODE_DISCOVERY_TIMEOUT =(5*60 * 1000)+2000;//2min2sec in ms

    @Test
    public void testWifiDirectDiscovery() throws IOException{
        SharedSeNetworkTestSuite.assumeNetworkHardwareEnabled();

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

        SharedSeNetworkTestSuite.assumeNetworkHardwareEnabled();

        final Object nodeDiscoveryLock = new Object();
        NetworkManagerListener responseListener = new NetworkManagerListener() {
            @Override
            public void fileStatusCheckInformationAvailable(String[] fileIds) {

            }

            @Override
            public void networkTaskStatusChanged(NetworkTask task) {

            }

            @Override
            public void networkNodeDiscovered(NetworkNode node) {
                if(node.getBluetoothMacAddress()!=null && node.getBluetoothMacAddress().equals(
                        bluetoothAddr)){
                    synchronized (nodeDiscoveryLock){
                        nodeDiscoveryLock.notify();
                    }
                }
            }

            @Override
            public void networkNodeUpdated(NetworkNode node) {
                if(node.getBluetoothMacAddress()!=null && node.getBluetoothMacAddress().equals(
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

        NetworkNode nodeInDb = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext())
                .getNetworkNodeDao().findNodeByBluetoothAddress(bluetoothAddr);

        if(nodeInDb == null || (System.currentTimeMillis() - nodeInDb.getWifiDirectLastUpdated()) > timeout ) {
            synchronized (nodeDiscoveryLock) {
                try { nodeDiscoveryLock.wait(timeout ); }
                catch(InterruptedException e ) {
                    e.printStackTrace();
                }
            }
        }

        nodeInDb = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext()).getNetworkNodeDao()
                .findNodeByBluetoothAddress(bluetoothAddr);

        Assert.assertNotNull("Remote wifi direct device discovered",
                nodeInDb);
        manager.removeNetworkManagerListener(responseListener);
    }


    @Test
    public void testNetworkServiceDiscovery() throws IOException{
        SharedSeNetworkTestSuite.assumeNetworkHardwareEnabled();

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

        SharedSeNetworkTestSuite.assumeNetworkHardwareEnabled();

        final Object nodeNSDiscoveryLock = new Object();
        NetworkManagerListener responseListener = new NetworkManagerListener() {
            @Override
            public void fileStatusCheckInformationAvailable(String[] fileIds) {

            }

            @Override
            public void networkTaskStatusChanged(NetworkTask task) {

            }

            @Override
            public void networkNodeDiscovered(NetworkNode node) {
                if(node.getIpAddress().equals(SharedSeTestSuite.REMOTE_SLAVE_SERVER) &&
                        (Calendar.getInstance().getTimeInMillis()-node.getNetworkServiceLastUpdated()) < NODE_DISCOVERY_TIMEOUT){
                    synchronized (nodeNSDiscoveryLock){
                        nodeNSDiscoveryLock.notify();
                    }
                }
            }

            @Override
            public void networkNodeUpdated(NetworkNode node) {
                if(node.getIpAddress().equals(SharedSeTestSuite.REMOTE_SLAVE_SERVER) &&
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

        NetworkNodeDao networkNodeDao = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext())
                .getNetworkNodeDao();
        NetworkNode node = networkNodeDao.findNodeByIpAddress(ipAddress);

        long timeSinceNsd =(Calendar.getInstance().getTimeInMillis() - (node != null ? node.getNetworkServiceLastUpdated() : 0));
        if(node == null || timeSinceNsd > NODE_DISCOVERY_TIMEOUT) {
            synchronized (nodeNSDiscoveryLock) {
                try { nodeNSDiscoveryLock.wait(timeout ); }
                catch(InterruptedException e ) {
                    e.printStackTrace();
                }
            }
        }
        manager.removeNetworkManagerListener(responseListener);

        node=networkNodeDao.findNodeByIpAddress(ipAddress);

        Assert.assertNotNull("Remote test slave node discovered via Network Service Discovery", node);
        boolean isWithinDiscoveryTimeRange=
                (Calendar.getInstance().getTimeInMillis()-node.getNetworkServiceLastUpdated()) < NODE_DISCOVERY_TIMEOUT;
        Assert.assertTrue("Was node discovered withing time range", isWithinDiscoveryTimeRange);

    }


    /**
     * Test disabling wifi on the client
     */
//    @Test
    public void testWifiDisabledOnClient() throws IOException {
        NetworkManager manager = UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        SharedSeNetworkTestSuite.assumeNetworkHardwareEnabled();

        final Object nodeUpdateLock = new Object();
        final Object networkConnectedLock = new Object();

        final long[] timeEnabled = new long[]{-1};
        NetworkManagerListener listener = new NetworkManagerListener() {
            @Override
            public void fileStatusCheckInformationAvailable(String[] fileIds) {

            }

            @Override
            public void networkNodeDiscovered(NetworkNode node) {

            }

            @Override
            public void networkNodeUpdated(NetworkNode node) {
                if(timeEnabled[0] == -1)
                    return;//not ready yet

                if(node.getBluetoothMacAddress().equals(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE)) {
                    long wifiDirectUpdated = node.getWifiDirectLastUpdated();
                    long nsdUpdated = node.getNetworkServiceLastUpdated();
                    if(wifiDirectUpdated > timeEnabled[0] && nsdUpdated > timeEnabled[0]) {
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
            testNetworkServiceDiscovery(SharedSeTestSuite.REMOTE_SLAVE_SERVER, NODE_DISCOVERY_TIMEOUT);

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
            //manager.updateClientServices();


            synchronized (nodeUpdateLock) {
                try { nodeUpdateLock.wait(NODE_DISCOVERY_TIMEOUT); }
                catch(InterruptedException e) {}
            }

            NetworkNode node = manager.getNodeByBluetoothAddr(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);
            boolean wifiDirectUpdatedAfterWifiDisabled = node.getWifiDirectLastUpdated() > timeEnabled[0];
            boolean nsdUpdatedAfterWifiDisabled = node.getNetworkServiceLastUpdated() > timeEnabled[0];
            Assert.assertTrue("Wifi direct last updated after WiFi enabled",
                    wifiDirectUpdatedAfterWifiDisabled);
            Assert.assertTrue("Network service discovery last updated after wifi enabled",
                    nsdUpdatedAfterWifiDisabled);
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

    //@Test
    public void testWifiDisabledOnServer() throws IOException {
        NetworkManager manager = UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        SharedSeNetworkTestSuite.assumeNetworkHardwareEnabled();

        final Object nodeUpdateLock = new Object();
        final Object networkConnectedLock = new Object();

        final long[] timeEnabled = new long[]{-1};
        NetworkManagerListener listener = new NetworkManagerListener() {
            @Override
            public void fileStatusCheckInformationAvailable(String[] fileIds) {

            }

            @Override
            public void networkNodeDiscovered(NetworkNode node) {

            }

            @Override
            public void networkNodeUpdated(NetworkNode node) {
                if(timeEnabled[0] == -1)
                    return;//not ready yet

                if(node.getBluetoothMacAddress().equals(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE)) {
                    long wifiDirectUpdated = node.getWifiDirectLastUpdated();
                    long nsdUpdated = node.getNetworkServiceLastUpdated();
                    if(wifiDirectUpdated > timeEnabled[0] && nsdUpdated > timeEnabled[0]) {
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

            }

            @Override
            public void networkTaskStatusChanged(NetworkTask networkTask) {

            }
        };
        manager.addNetworkManagerListener(listener);


        try {
            Assert.assertTrue("Supernode enabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(true));
            testWifiDirectDiscovery(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE, NODE_DISCOVERY_TIMEOUT);

            //Create a wifi direct group before disabling wifi
            String createGroupUrl = PlatformTestUtil.getRemoteTestEndpoint() + "?cmd="
                    + RemoteTestServerHttpd.CMD_CREATEGROUP;
            HttpURLConnection urlConnection = (HttpURLConnection)new URL(createGroupUrl).openConnection();
            Assert.assertEquals("Group created", 200, urlConnection.getResponseCode());

            Assert.assertTrue("Supernode wifi disabled for 20s", TestUtilsSE.disableRemoteWifi(20000));
            UstadMobileSystemImpl.l(UMLog.INFO, 340, "=== wifi disabled on server ===" );
            try { Thread.sleep(20000);}
            catch(InterruptedException e) {}
            timeEnabled[0] = Calendar.getInstance().getTimeInMillis();
            UstadMobileSystemImpl.l(UMLog.INFO, 340, "=== wifi enabled (disable timeout) ===" );
            synchronized (nodeUpdateLock) {
                try { nodeUpdateLock.wait(NODE_DISCOVERY_TIMEOUT);}
                catch(InterruptedException e) {}
            }
            NetworkNode node = manager.getNodeByBluetoothAddr(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);
            boolean wifiDirectUpdatedAfterWifiDisabled = node.getWifiDirectLastUpdated() > timeEnabled[0];
            boolean nsdUpdatedAfterWifiDisabled = node.getNetworkServiceLastUpdated() > timeEnabled[0];
            Assert.assertTrue("Wifi direct last updated after WiFi enabled",
                    wifiDirectUpdatedAfterWifiDisabled);
            Assert.assertTrue("Network service discovery last updated after wifi enabled",
                    nsdUpdatedAfterWifiDisabled);

        }catch(Exception e) {
            UstadMobileSystemImpl.l(0, 0, "WTF", e);
        } finally{
            manager.removeNetworkManagerListener(listener);
        }

    }




}
