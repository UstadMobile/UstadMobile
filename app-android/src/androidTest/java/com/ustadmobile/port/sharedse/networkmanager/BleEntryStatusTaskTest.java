package com.ustadmobile.port.sharedse.networkmanager;

import android.content.Context;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.EntryStatusResponseDao;
import com.ustadmobile.core.db.dao.NetworkNodeDao;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil.bleMessageLongToBytes;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_RESPONSE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private EntryStatusResponseDao mockedResponseDao;
    private NetworkNode networkNode;
    @Before
    public void setUpSpy(){
        Context context = (Context) PlatformTestUtil.getTargetContext();
        UmAppDatabase.getInstance(context).clearAllTables();
        networkNode = new NetworkNode();
        networkNode.setBluetoothMacAddress("00:3F:2F:64:C6:4F");
        networkNode.setNodeId(1);

        mockedResponseDao = mock(EntryStatusResponseDao.class);
        NetworkNodeDao mockedNodeDao = mock(NetworkNodeDao.class);
        UmAppDatabase mockedDb = mock(UmAppDatabase.class);
        UmAppDatabase.setInstance(mockedDb);
        when(mockedDb.getEntryStatusResponseDao()).thenReturn(mockedResponseDao);
        when(mockedDb.getNetworkNodeDao()).thenReturn(mockedNodeDao);
        when(mockedNodeDao.findNodeByBluetoothAddress(networkNode.getBluetoothMacAddress()))
                .thenReturn(networkNode);

        mockedEntryStatusTask = spy(BleEntryStatusTask.class);
        mockedEntryStatusTask.setContext(context);
        mockedEntryStatusTask.setEntryUidsToCheck(entries);
    }
    @Test
    public void givenBleMessageWithRequest_whenResponseReceived_thenShouldUpdateEntryStatusResponseInDatabase() {
        BleMessage responseMessage = new BleMessage(ENTRY_STATUS_RESPONSE,
                bleMessageLongToBytes(entriesResponse));
        mockedEntryStatusTask.onResponseReceived(networkNode.getBluetoothMacAddress(),responseMessage);

        //Verify that all the entry check status response will be saved to the database
        verify(mockedResponseDao).insert(any());
    }
}
