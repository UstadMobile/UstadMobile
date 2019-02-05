package com.ustadmobile.port.sharedse.networkmanager;


import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.EntryStatusResponseDao;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil.bleMessageLongToBytes;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_RESPONSE;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.spy;

/**
 * Test class which tests {@link BleEntryStatusTask} to make sure it behaves as expected
 * under different circumstances when response received.
 *
 * @author kileha3
 */
public class BleEntryStatusTaskTest {

    private List<Long> entries = Arrays.asList(1056289670L,9076137860L,4590875612L,2912543894L);

    private List<Long> entriesResponse = Arrays.asList(0L,9076137860000L,0L,2912543894000L);

    private BleEntryStatusTask mockedEntryStatusTask;

    private EntryStatusResponseDao entryStatusResponseDao;

    private NetworkNode networkNode;

    @Before
    public void setUp(){
        Object context =  PlatformTestUtil.getTargetContext();
        UmAppDatabase umAppDatabase = UmAppDatabase.getInstance(context);
        umAppDatabase.clearAllTables();

        networkNode = new NetworkNode();
        networkNode.setBluetoothMacAddress("00:3F:2F:64:C6:4F");
        networkNode.setNodeId(1);
        umAppDatabase.getNetworkNodeDao().insert(networkNode);

        entryStatusResponseDao = umAppDatabase.getEntryStatusResponseDao();

        mockedEntryStatusTask = spy(BleEntryStatusTask.class);
        mockedEntryStatusTask.setContext(context);
        mockedEntryStatusTask.setEntryUidsToCheck(entries);
    }
    @Test
    public void givenBleMessageWithRequest_whenResponseReceived_thenShouldUpdateEntryStatusResponseInDatabase() {

        BleMessage responseMessage = new BleMessage(ENTRY_STATUS_RESPONSE,
                bleMessageLongToBytes(entriesResponse));
        mockedEntryStatusTask.onResponseReceived(networkNode.getBluetoothMacAddress(),responseMessage);

        assertNotNull("entry check status response will be saved to the database",
                entryStatusResponseDao.findByEntryIdAndNetworkNode(entries.get(0), networkNode.getNodeId()));
    }
}
