package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.dao.EntryStatusResponseDao;
import com.ustadmobile.core.networkmanager.AvailabilityMonitorRequest;
import com.ustadmobile.core.networkmanager.NetworkManagerCore;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.core.networkmanager.NetworkManagerListener;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.core.impl.PlatformTestUtil;
import com.ustadmobile.test.sharedse.SharedSeTestSuite;
import com.ustadmobile.test.sharedse.TestUtilsSE;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by kileha3 on 16/05/2017.
 */
public class TestEntryStatusTask{



    public static final String ENTRY_ID="202b10fe-b028-4b84-9b84-852aa766607d";

    public static final String ENTRY_ID_NOT_PRESENT = "202b10fe-b028-4b84-9b84-852aa766607dx";

    public static final String[] ENTRY_IDS = new String[]{ENTRY_ID, ENTRY_ID_NOT_PRESENT};

    private static final int DEFAULT_WAIT_TIME =10000;

    public static final Hashtable EXPECTED_AVAILABILITY = new Hashtable();

    public static final int AVAILABILITY_MONITOR_TIMEOUT = 150000;//2.5 mins

    static {
        EXPECTED_AVAILABILITY.put(ENTRY_ID, Boolean.TRUE);
        EXPECTED_AVAILABILITY.put(ENTRY_ID_NOT_PRESENT, Boolean.FALSE);
    }


    @Before
    public void checkNetworkEnabled() {
        SharedSeNetworkTestSuite.assumeNetworkHardwareEnabled();
    }

    @Test
    public void testEntryStatusBluetooth() throws IOException, InterruptedException {
        Assert.assertTrue("Test slave supernode enabled",
                TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(true));
        TestNetworkManager.testWifiDirectDiscovery(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE,
                TestNetworkManager.NODE_DISCOVERY_TIMEOUT);
        testEntryStatusBluetooth(EXPECTED_AVAILABILITY, TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);
        Assert.assertTrue("Supernode disabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(false));
    }

    @Test
    public void testEntryStatusHttp() throws IOException, InterruptedException {
        NetworkManager networkManager = UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();

        Assert.assertTrue("Test slave supernode enabled",
                TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(true));
        TestNetworkManager.testNetworkServiceDiscovery(SharedSeTestSuite.REMOTE_SLAVE_SERVER,
                TestNetworkManager.NODE_DISCOVERY_TIMEOUT);

        NetworkNode remoteNode = networkManager.getNodeByIpAddress(
                SharedSeTestSuite.REMOTE_SLAVE_SERVER);

        testEntryStatus(EXPECTED_AVAILABILITY, remoteNode, false, true);
        Assert.assertTrue("Supernode disabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(false));
    }


//    @Test
//    public void testEntryStatusBluetoothOnFailure() throws IOException, InterruptedException {
//        String wrongBluetoothAddr = "00:AA:BB:CC:DD:EE";
//        NetworkNode wrongNode = new NetworkNode(wrongBluetoothAddr, null);
//        wrongNode.setBluetoothMacAddress(wrongBluetoothAddr);
//        testEntryStatusBluetooth(null, wrongNode);
//    }

    public static void testEntryStatusBluetooth(Hashtable expectedAvailability, String remoteBluetoothAddr) throws IOException, InterruptedException {
        NetworkNode networkNode= DbManager.getInstance(PlatformTestUtil.getTargetContext())
                .getNetworkNodeDao().findNodeByBluetoothAddress(remoteBluetoothAddr);
        if(networkNode == null)
            throw new IllegalArgumentException("testEntryStatuBluetooth Hashtable, String requires the bluetooth address to have been discovered");

        testEntryStatusBluetooth(expectedAvailability, networkNode);
    }

//    @Test
//    public void testEntryStatusStop() throws IOException, InterruptedException {
//        final NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
//        NetworkNode remoteNode = manager.getNodeByBluetoothAddr(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);
//        List<NetworkNode> nodeList = new ArrayList<>();
//        nodeList.add(remoteNode);
//        long taskId = manager.requestFileStatus(Arrays.asList(ENTRY_IDS),manager.getContext(),nodeList, true, false);
//        NetworkTask task = manager.getTaskById(taskId, NetworkManagerCore.QUEUE_ENTRY_STATUS);
//        task.stop(NetworkTask.STATUS_STOPPED);
//        try { Thread.sleep(1000); }
//        catch(InterruptedException e){}
//        Assert.assertEquals("Task status is stopped", task.getStatus(), NetworkTask.STATUS_STOPPED);
//        Assert.assertTrue("Task is stopped", task.isStopped());
//    }

//    @Test
    public void testMonitoringAvailability() throws IOException{
        final NetworkManager manager = UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        manager.getKnownNodes().clear();
        manager.getEntryResponses().clear();
        final HashMap<String, Boolean> actualEntryStatuses = new HashMap();
        Assert.assertTrue("Test slave supernode enabled",
                TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(true));

        final Object discoverLock = new Object();
        NetworkManagerListener listener = new NetworkManagerListener() {
            @Override
            public void fileStatusCheckInformationAvailable(String[] fileIds) {
                for(int i = 0; i < fileIds.length; i++) {
                    actualEntryStatuses.put(fileIds[i], manager.isFileAvailable(fileIds[i]));
                }

                if(doesAvailabilityMatch(EXPECTED_AVAILABILITY, actualEntryStatuses)) {
                    synchronized (discoverLock) {
                        discoverLock.notify();
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

        AvailabilityMonitorRequest request = new AvailabilityMonitorRequest(EXPECTED_AVAILABILITY.keySet());
        manager.startMonitoringAvailability(request, true);
        synchronized (discoverLock) {
            try { discoverLock.wait(AVAILABILITY_MONITOR_TIMEOUT);}
            catch(InterruptedException e) {}
        }

        manager.stopMonitoringAvailability(request);
        manager.removeNetworkManagerListener(listener);

        Iterator<String> keyIterator = EXPECTED_AVAILABILITY.keySet().iterator();
        String currentKey;
        while(keyIterator.hasNext()) {
            currentKey = keyIterator.next();
            Assert.assertEquals("Expected availability matches actual availability for " + currentKey,
                    EXPECTED_AVAILABILITY.get(currentKey), actualEntryStatuses.get(currentKey));
        }


        Assert.assertTrue("Test slave supernode disabled",
                TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(false));
    }


    /**
     * Test using entry status checking over bluetooth. Communicate only with the given deice with
     * the specified bluetooth mac address. Before running this test the given remote device should
     * already have been discovered.
     *
     * This method takes a Hashtable specifying the results that should be obtained from the entry
     * status task (e.g. whether a given file is reported as available or unavailable). e.g.
     *  hashtable.put("entry-id-available", Boolean.TRUE)
     *  hashtable.put("entry-id-unavailable", Boolean.FALSE)
     *
     * The method will assert that the availability found matches each entry for expected availability.
     *
     * @param expectedAvailability Hashtable in the form of file id -> boolean specifying the expected availability to assert.
     * @param remoteNode The network node to connect to
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public static void testEntryStatus(Hashtable expectedAvailability, NetworkNode remoteNode, boolean useBluetooth, boolean useHttp) throws IOException, InterruptedException {
        final NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();


        final Object statusRequestLock=new Object();
        final Hashtable actualAvailability = new Hashtable();
        final long taskId[] = new long[]{-1};
        final boolean taskCompleted[] = new boolean[]{false};

        final EntryStatusResponseDao responseDao = DbManager.getInstance(PlatformTestUtil.getTargetContext())
                .getEntryStatusResponseDao();

        NetworkManagerListener responseListener = new NetworkManagerListener() {

            @Override
            public void fileStatusCheckInformationAvailable(String[] fileIds) {
                for(int i = 0; i < fileIds.length; i++) {
                    actualAvailability.put(fileIds[i], responseDao.isEntryAvailableLocally(fileIds[i]));
                }
            }

            @Override
            public void networkTaskStatusChanged(NetworkTask task) {
                if(task.getTaskId() == taskId[0]) {
                    taskCompleted[0] = true;
                    synchronized (statusRequestLock){
                        statusRequestLock.notify();
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
            public void fileAcquisitionInformationAvailable(String entryId, long downloadId, int downloadSource) {

            }

            @Override
            public void wifiConnectionChanged(String ssid, boolean connected, boolean connectedOrConnecting) {

            }
        };
        manager.addNetworkManagerListener(responseListener);

        List<NetworkNode> nodeList=new ArrayList<>();
        nodeList.add(remoteNode);

        List<String> entryList= Arrays.asList(ENTRY_IDS);

        taskId[0] = manager.requestFileStatus(entryList,manager.getContext(),nodeList, useBluetooth,
                useHttp);
        synchronized (statusRequestLock){
            statusRequestLock.wait(DEFAULT_WAIT_TIME*6);
        }
        Assert.assertTrue("Task completed", taskCompleted[0]);

        manager.removeNetworkManagerListener(responseListener);

        if(expectedAvailability == null) {
            //This test doesn't really expect a particular result - it's done - as long as it completed
            return;
        }


        Enumeration expectedKeysEnumeration = expectedAvailability.keys();
        Object currentIdKey;
        while(expectedKeysEnumeration.hasMoreElements()) {
            currentIdKey = expectedKeysEnumeration.nextElement();
            String message = currentIdKey + " expected availability : " +
                    expectedAvailability.get(currentIdKey);
            Assert.assertEquals(message, expectedAvailability.get(currentIdKey),
                    actualAvailability.get(currentIdKey));
        }
    }

    public static void testEntryStatusBluetooth(Hashtable expectedAvailability, NetworkNode remoteNode) throws IOException, InterruptedException{
        testEntryStatus(expectedAvailability, remoteNode, true, false);
    }

    static boolean doesAvailabilityMatch(Map expectedAvailability, Map actualAvailability){
        Iterator expectedKeysIterator = expectedAvailability.keySet().iterator();
        Object currentIdKey;
        boolean availabilityMatch = true;
        while(expectedKeysIterator.hasNext()) {
            currentIdKey = expectedKeysIterator.next();
            availabilityMatch &= actualAvailability.containsKey(currentIdKey)
                    && actualAvailability.get(currentIdKey).equals(actualAvailability.get(currentIdKey));
        }

        return availabilityMatch;
    }


}
