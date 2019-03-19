package com.ustadmobile.port.sharedse.networkmanager;


import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.EntryStatusResponseDao;
import com.ustadmobile.core.db.dao.NetworkNodeDao;
import com.ustadmobile.lib.database.jdbc.DriverConnectionPoolInitializer;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.lib.db.entities.UmAccount;
import com.ustadmobile.sharedse.SharedSeTestConfig;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil.bleMessageLongToBytes;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_RESPONSE;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.spy;

/**
 * Test class which tests {@link BleEntryStatusTask} to make sure it behaves as expected
 * under different circumstances when response received.
 *
 * @author kileha3
 */
public class BleEntryStatusTaskTest {

    private List<Long> containerUids = Arrays.asList(1056289670L,9076137860L,4590875612L,2912543894L);

    private List<Long> localAvailabilityCheckResponse = Arrays.asList(0L,9076137860000L,0L,2912543894000L);

    private BleEntryStatusTask mockedEntryStatusTask;

    private EntryStatusResponseDao entryStatusResponseDao;

    private NetworkManagerBle managerBle;

    private NetworkNodeDao networkNodeDao;

    private NetworkNode networkNode;

    @Before
    public void setUp(){
        DriverConnectionPoolInitializer.bindDataSource("UmAppDatabase",
                SharedSeTestConfig.TESTDB_JDBCURL_UMMAPPDATABASE, true);
        Object context =  PlatformTestUtil.getTargetContext();
        UmAppDatabase umAppDatabase = UmAppDatabase.getInstance(context);
        umAppDatabase.clearAllTables();

        managerBle = spy(NetworkManagerBle.class);
        managerBle.onCreate();
        managerBle.setContext(context);

        networkNode = new NetworkNode();
        networkNode.setBluetoothMacAddress("00:3F:2F:64:C6:4F");
        networkNode.setNodeId(1);
        networkNodeDao = umAppDatabase.getNetworkNodeDao();
        networkNodeDao.insert(networkNode);

        entryStatusResponseDao = umAppDatabase.getEntryStatusResponseDao();

        mockedEntryStatusTask = spy(BleEntryStatusTask.class);
        mockedEntryStatusTask.setContext(context);
        mockedEntryStatusTask.setManagerBle(spy(NetworkManagerBle.class));
        mockedEntryStatusTask.setEntryUidsToCheck(containerUids);
    }

    @Test
    public void givenBleMessageWithRequest_whenResponseReceived_thenShouldUpdateEntryStatusResponseInDatabase() {

        BleMessage responseMessage = new BleMessage(ENTRY_STATUS_RESPONSE,
                bleMessageLongToBytes(localAvailabilityCheckResponse));
        mockedEntryStatusTask.onResponseReceived(networkNode.getBluetoothMacAddress(),responseMessage, null);

        assertNotNull("entry check status response will be saved to the database",
                entryStatusResponseDao.findByContainerUidAndNetworkNode(containerUids.get(0),
                        networkNode.getNodeId()));
    }


    @Test
    public void givenNode_whenTryToConnectAndFailedMoreThanThreshold_shouldBeDeletedFromDb() {

        for(int i = 0 ; i < 6 ;i++){
            managerBle.handleNodeConnectionHistory(networkNode.getBluetoothMacAddress(),false);
        }

        assertNull("The node was deleted from the db",
                networkNodeDao.findNodeByBluetoothAddress(networkNode.getBluetoothMacAddress()));
    }

    @Test
    public void givenNodeWithFailureBelowThreshold_whenSucceed_shouldResetTheFailureCounterToZero() {

        for(int i = 0 ; i < 3 ;i++){
            managerBle.handleNodeConnectionHistory(networkNode.getBluetoothMacAddress(),false);
        }

        managerBle.handleNodeConnectionHistory(networkNode.getBluetoothMacAddress(), true);

        assertNotNull("The node was not deleted from the db",
                networkNodeDao.findNodeByBluetoothAddress(networkNode.getBluetoothMacAddress()));
    }
}
