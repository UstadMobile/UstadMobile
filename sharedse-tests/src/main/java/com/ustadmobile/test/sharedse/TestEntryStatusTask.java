package com.ustadmobile.test.sharedse;

import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.EntryStatusTask;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.core.networkmanager.NetworkManagerListener;
import com.ustadmobile.core.networkmanager.NetworkNode;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.test.core.buildconfig.TestConstants;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by kileha3 on 16/05/2017.
 */
public class TestEntryStatusTask{



    public static final String ENTRY_ID="202b10fe-b028-4b84-9b84-852aa766607d";

    public static final String ENTRY_ID_NOT_PRESENT = "202b10fe-b028-4b84-9b84-852aa766607dx";

    public static final String[] ENTRY_IDS = new String[]{ENTRY_ID, ENTRY_ID_NOT_PRESENT};

    private static final int DEFAULT_WAIT_TIME =10000;

    public static final Hashtable EXPECTED_AVAILABILITY = new Hashtable();

    static {
        EXPECTED_AVAILABILITY.put(ENTRY_ID, Boolean.TRUE);
        EXPECTED_AVAILABILITY.put(ENTRY_ID_NOT_PRESENT, Boolean.FALSE);
    }


    @Test
    public void testEntryStatusBluetooth() throws IOException, InterruptedException {
        Assert.assertTrue("Test slave supernode enabled",
                TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(true));
        TestNetworkManager.testWifiDirectDiscovery(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE,
                TestNetworkManager.NODE_DISCOVERY_TIMEOUT);
        testEntryStatusBluetooth(EXPECTED_AVAILABILITY, TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);
        Assert.assertTrue("Supernod disabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(false));
    }

    @Test
    public void testEntryStatusBluetoothOnFailure() throws IOException, InterruptedException {
        String wrongBluetoothAddr = "00:AA:BB:CC:DD:EE";
        NetworkNode wrongNode = new NetworkNode(wrongBluetoothAddr, null);
        wrongNode.setDeviceBluetoothMacAddress(wrongBluetoothAddr);
        testEntryStatusBluetooth(null, wrongNode);
    }

    public static void testEntryStatusBluetooth(Hashtable expectedAvailability, String remoteBluetoothAddr) throws IOException, InterruptedException {
        final NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        NetworkNode networkNode= manager.getNodeByBluetoothAddr(remoteBluetoothAddr);
        if(networkNode == null)
            throw new IllegalArgumentException("testEntryStatuBluetooth Hashtable, String requires the bluetooth address to have been discovered");

        testEntryStatusBluetooth(expectedAvailability, networkNode);
    }

    @Test
    public void testEntryStatusStop() throws IOException, InterruptedException {
        final NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        NetworkNode remoteNode = manager.getNodeByBluetoothAddr(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);
        List<NetworkNode> nodeList = new ArrayList<>();
        nodeList.add(remoteNode);
        long taskId = manager.requestFileStatus(Arrays.asList(ENTRY_IDS),manager.getContext(),nodeList, true, false);
        NetworkTask task = manager.getNetworkTaskByTaskId(taskId, NetworkManager.QUEUE_ENTRY_STATUS);
        task.stop(NetworkTask.STATUS_STOPPED);
        try { Thread.sleep(1000); }
        catch(InterruptedException e){}
        Assert.assertEquals("Task status is stopped", task.getStatus(), NetworkTask.STATUS_STOPPED);
        Assert.assertTrue("Task is stopped", task.isStopped());
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
    public static void testEntryStatusBluetooth(Hashtable expectedAvailability, NetworkNode remoteNode) throws IOException, InterruptedException {
        final NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        Assume.assumeTrue("Network test wifi and bluetooth enabled",
                manager.isBluetoothEnabled() && manager.isWiFiEnabled());

        final Object statusRequestLock=new Object();
        final Hashtable actualAvailability = new Hashtable();
        final long taskId[] = new long[]{-1};
        final boolean taskCompleted[] = new boolean[]{false};

        NetworkManagerListener responseListener = new NetworkManagerListener() {

            @Override
            public void fileStatusCheckInformationAvailable(List<String> fileIds) {
                for(int i = 0; i < fileIds.size(); i++) {
                    actualAvailability.put(fileIds.get(i), manager.isFileAvailable(fileIds.get(i)));
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

        List<String> entryLIst=new ArrayList<>();
        for(int i = 0; i < ENTRY_IDS.length; i++) {
            entryLIst.add(ENTRY_IDS[i]);
        }

        taskId[0] = manager.requestFileStatus(entryLIst,manager.getContext(),nodeList, true, false);
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


}
