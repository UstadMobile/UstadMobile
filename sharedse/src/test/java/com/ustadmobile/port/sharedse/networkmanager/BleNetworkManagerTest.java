package com.ustadmobile.port.sharedse.networkmanager;



import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.EntryStatusResponse;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.ustadmobile.port.sharedse.networkmanager.BleGattServerTest.MAX_ENTITIES_NUMBER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class which tests {@link NetworkManagerBle}
 * to make sure it behaves as expected on entry status monitoring logic
 *
 * @author kileha3
 */
public class BleNetworkManagerTest {

    private UmAppDatabase umAppDatabase;

    private NetworkManagerBle mockedNetworkManager;

    private NetworkNode networkNode;

    private Object monitor = new Object();

    private long testCaseVerifyTimeOut = TimeUnit.SECONDS.toMillis(5);

    private List<Long> contentEntryUids = new ArrayList<>();

    private List<EntryStatusResponse> entryStatusResponseList = new ArrayList<>();

    private List<ContentEntry> contentEntryList = new ArrayList<>();

    private BleEntryStatusTask mockedEntryStatusTask;

    private BleMessage messageToBeSent = new BleMessage((byte)0, new byte[]{});

    private BleMessageResponseListener mockedResponseListener;

    private Object context;

    @Before
    public void setUp(){
        context =  PlatformTestUtil.getTargetContext();
        mockedEntryStatusTask = mock(BleEntryStatusTask.class);
        networkNode = new NetworkNode();
        mockedNetworkManager = spy(NetworkManagerBle.class);
        mockedResponseListener = mock(BleMessageResponseListener.class);
        mockedNetworkManager.init(context);
        when(mockedNetworkManager
                .makeEntryStatusTask(eq(context),eq(contentEntryUids),any()))
                .thenReturn(mockedEntryStatusTask);

        when(mockedNetworkManager
                .makeEntryStatusTask(eq(context),eq(messageToBeSent),
                        eq(networkNode),eq(mockedResponseListener)))
                .thenReturn(mockedEntryStatusTask);

        UmAppDatabase.getInstance(context).clearAllTables();
        umAppDatabase = UmAppDatabase.getInstance(context);

        for(int i = 0; i < MAX_ENTITIES_NUMBER; i++){
            long currentTimeStamp = Calendar.getInstance().getTimeInMillis();
            ContentEntry contentEntry = new ContentEntry();
            contentEntry.setLastModified(currentTimeStamp);
            contentEntry.setDescription("Content entry description");
            contentEntry.setTitle("Content entry title");
            contentEntryList.add(contentEntry);
        }
        Collections.sort(contentEntryUids);

        UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);
        Long [] contentEntryUids = repository.getContentEntryDao().insert(contentEntryList);
        this.contentEntryUids.addAll(Arrays.asList(contentEntryUids));
    }


    @Test
    public void givenMonitoringAvailabilityStarted_whenNewNodeDiscovered_thenShouldCreateEntryStatusTask() {
        networkNode.setBluetoothMacAddress("00:3F:2F:64:C6:4F");
        mockedNetworkManager.startMonitoringAvailability(monitor, contentEntryUids);
        mockedNetworkManager.handleNodeDiscovered(networkNode);

        /*Verify that entry status task was created, we are interested in call
        from handleNodeDiscovered method since startMonitoringAvailability can
        call this method too*/
        verify(mockedNetworkManager,timeout(testCaseVerifyTimeOut))
                .makeEntryStatusTask(eq(context),eq(contentEntryUids),any());

        //Verify if the run() method was called
        verify(mockedEntryStatusTask,timeout(testCaseVerifyTimeOut)).run();
    }

    @Test
    public void givenEntryStatusNotKnown_whenStartMonitoringAvailabilityCalled_thenShouldCreateEntryStatusTask() {
        networkNode.setBluetoothMacAddress("00:3F:2F:64:C6:4B");
        umAppDatabase.getNetworkNodeDao().insert(networkNode);
        mockedNetworkManager.startMonitoringAvailability(monitor, contentEntryUids);

        //Verify that entry status task was created
        verify(mockedNetworkManager,timeout(testCaseVerifyTimeOut))
                .makeEntryStatusTask(eq(context),eq(contentEntryUids),any());

        //Verify that the run() method was called
        verify(mockedEntryStatusTask,timeout(testCaseVerifyTimeOut)).run();
    }

    @Test
    public void givenNewMessageToBeSent_whenSendMessageCalled_thenShouldCreateEntryStatusTaskAndExecuteIt() {
        networkNode.setBluetoothMacAddress("00:3F:2F:64:C6:4B");

        mockedNetworkManager.sendMessage(context,messageToBeSent,networkNode,mockedResponseListener);

        //Verify that entry status task was created
        //Verify that entry status task was created
        verify(mockedNetworkManager,timeout(testCaseVerifyTimeOut))
                .makeEntryStatusTask(eq(context),eq(messageToBeSent),any(),eq(mockedResponseListener));

        //Verify that the run() method was called
        verify(mockedEntryStatusTask,timeout(testCaseVerifyTimeOut)).run();
    }

    @Test
    public void givenMonitoringAvailabilityStopped_whenNewNodeDiscovered_thenShouldNotCreateEntryStatusTask() {
        mockedNetworkManager.startMonitoringAvailability(monitor, contentEntryUids);
        mockedNetworkManager.stopMonitoringAvailability(monitor);
        mockedNetworkManager.handleNodeDiscovered(networkNode);

        //Verify that entry status task was not created
        verify(mockedNetworkManager.makeEntryStatusTask(context, contentEntryUids,networkNode),
                times(0));
    }

    @Test
    public void givenAvailabilityInformationAlreadyKnown_whenMonitoringAvailability_thenShouldNotCreateEntryStatusTask() {
        networkNode.setBluetoothMacAddress("00:3F:2F:64:C6:4F");
        long nodeId = umAppDatabase.getNetworkNodeDao().insert(networkNode);

        for(int i = 0; i < contentEntryUids.size(); i++){
            long entryId = contentEntryUids.get(i);
            EntryStatusResponse response = new EntryStatusResponse();
            response.setAvailable(true);
            response.setEntryId(entryId);
            response.setResponderNodeId((int) nodeId);
            response.setId(i+1);
            response.setResponseTime(10L);
            response.setEntryUpdatedTime(11L);
            entryStatusResponseList.add(response);
        }

        umAppDatabase.getEntryStatusResponseDao().insert(entryStatusResponseList);
        mockedNetworkManager.startMonitoringAvailability(monitor, contentEntryUids);

        //Verify that entry status task was not created
        verify(mockedNetworkManager, times(0))
                .makeEntryStatusTask(context, contentEntryUids,networkNode);
    }

}
