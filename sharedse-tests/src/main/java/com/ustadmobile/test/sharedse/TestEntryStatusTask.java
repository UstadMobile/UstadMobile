package com.ustadmobile.test.sharedse;

import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
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



    @Test
    public void testEntryStatusBluetooth() throws IOException, InterruptedException {
        Hashtable entryTable = new Hashtable();
        entryTable.put(ENTRY_ID, Boolean.TRUE);
        entryTable.put(ENTRY_ID_NOT_PRESENT, Boolean.FALSE);
        Assert.assertTrue("Test slave supernode enabled",
                TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(true));
        TestNetworkManager.testWifiDirectDiscovery(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE,
                TestNetworkManager.NODE_DISCOVERY_TIMEOUT);
        testEntryStatusBluetooth(entryTable, TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);
        Assert.assertTrue("Supernod disabled", TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(false));
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
     * @param remoteBluetoothAddr The bluetooth address of the device to check for entry status
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public static void testEntryStatusBluetooth(Hashtable expectedAvailability, String remoteBluetoothAddr) throws IOException, InterruptedException {
        final NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        Assume.assumeTrue("Network test wifi and bluetooth enabled",
                manager.isBluetoothEnabled() && manager.isWiFiEnabled());

        final Object statusRequestLock=new Object();
        final Hashtable actualAvailability = new Hashtable();

        NetworkManagerListener responseListener = new NetworkManagerListener() {
            @Override
            public void fileStatusCheckInformationAvailable(List<String> fileIds) {
                for(int i = 0; i < fileIds.size(); i++) {
                    actualAvailability.put(fileIds.get(i), manager.isFileAvailable(fileIds.get(i)));
                }
            }

            @Override
            public void networkTaskCompleted(NetworkTask task) {
                //TODO; Notify here
                synchronized (statusRequestLock){
                    statusRequestLock.notify();
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

        NetworkNode networkNode=manager.getNodeByBluetoothAddr(remoteBluetoothAddr);
        List<NetworkNode> nodeList=new ArrayList<>();
        nodeList.add(networkNode);

        List<String> entryLIst=new ArrayList<>();
        for(int i = 0; i < ENTRY_IDS.length; i++) {
            entryLIst.add(ENTRY_IDS[i]);
        }

        manager.requestFileStatus(entryLIst,manager.getContext(),nodeList, true, false);
        synchronized (statusRequestLock){
            statusRequestLock.wait(DEFAULT_WAIT_TIME*6);
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


        manager.removeNetworkManagerListener(responseListener);
    }


}
