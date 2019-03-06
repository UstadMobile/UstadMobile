package com.ustadmobile.port.sharedse.networkmanager;



import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.database.jdbc.DriverConnectionPoolInitializer;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.EntryStatusResponse;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.sharedse.SharedSeTestConfig;
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

    private List<Long> containerUids = new ArrayList<>();

    private List<EntryStatusResponse> entryStatusResponseList = new ArrayList<>();

    private List<Container> containerList = new ArrayList<>();

    private BleEntryStatusTask mockedEntryStatusTask;

    private BleMessage messageToBeSent = new BleMessage((byte)0, new byte[]{});

    private BleMessageResponseListener mockedResponseListener;

    private Object context;

    @Before
    public void setUp(){
        DriverConnectionPoolInitializer.bindDataSource("UmAppDatabase",
                SharedSeTestConfig.TESTDB_JDBCURL_UMMAPPDATABASE, true);
        context =  PlatformTestUtil.getTargetContext();
        mockedEntryStatusTask = mock(BleEntryStatusTask.class);
        networkNode = new NetworkNode();
        networkNode.setNetworkNodeLastUpdated(System.currentTimeMillis());
        mockedNetworkManager = spy(NetworkManagerBle.class);
        mockedResponseListener = mock(BleMessageResponseListener.class);
        mockedNetworkManager.setContext(context);
        when(mockedNetworkManager
                .makeEntryStatusTask(eq(context),eq(containerUids),any()))
                .thenReturn(mockedEntryStatusTask);

        when(mockedNetworkManager
                .makeEntryStatusTask(eq(context),eq(messageToBeSent),
                        eq(networkNode),any(BleMessageResponseListener.class)))
                .thenReturn(mockedEntryStatusTask);

        umAppDatabase = UmAppDatabase.getInstance(context);
        umAppDatabase.clearAllTables();


        for(int i = 0; i < MAX_ENTITIES_NUMBER; i++){
            long currentTimeStamp = Calendar.getInstance().getTimeInMillis();
            Container entryFile = new Container();
            entryFile.setLastModified(currentTimeStamp);
            containerList.add(entryFile);
        }
        Collections.sort(containerUids);

        containerUids.addAll(Arrays.asList(umAppDatabase.getContainerDao().insert(containerList)));
    }


    @Test
    public void givenMonitoringAvailabilityStarted_whenNewNodeDiscovered_thenShouldCreateEntryStatusTask() {
        networkNode.setBluetoothMacAddress("00:3F:2F:64:C6:4F");
        mockedNetworkManager.startMonitoringAvailability(monitor, containerUids);
        mockedNetworkManager.handleNodeDiscovered(networkNode);

        /*Verify that entry status task was created, we are interested in call
        from handleNodeDiscovered method since startMonitoringAvailability can
        call this method too*/
        verify(mockedNetworkManager,timeout(testCaseVerifyTimeOut))
                .makeEntryStatusTask(eq(context),eq(containerUids),any());

        //Verify if the run() method was called
        verify(mockedEntryStatusTask,timeout(testCaseVerifyTimeOut)).run();
    }

    @Test
    public void givenEntryStatusNotKnown_whenStartMonitoringAvailabilityCalled_thenShouldCreateEntryStatusTask() {
        networkNode.setBluetoothMacAddress("00:3F:2F:64:C6:4B");
        umAppDatabase.getNetworkNodeDao().insert(networkNode);
        mockedNetworkManager.startMonitoringAvailability(monitor, containerUids);

        //Verify that entry status task was created
        verify(mockedNetworkManager,timeout(testCaseVerifyTimeOut))
                .makeEntryStatusTask(eq(context),eq(containerUids),any());

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
                .makeEntryStatusTask(eq(context),eq(messageToBeSent),any(),
                        any(BleMessageResponseListener.class));

        //Verify that the run() method was called
        verify(mockedEntryStatusTask,timeout(testCaseVerifyTimeOut)).run();
    }

    @Test
    public void givenMonitoringAvailabilityStopped_whenNewNodeDiscovered_thenShouldNotCreateEntryStatusTask() {
        mockedNetworkManager.startMonitoringAvailability(monitor, containerUids);
        mockedNetworkManager.stopMonitoringAvailability(monitor);
        mockedNetworkManager.handleNodeDiscovered(networkNode);

        //Verify that entry status task was not created
        verify(mockedNetworkManager, times(0))
                .makeEntryStatusTask(context, containerUids,networkNode);
    }

    @Test
    public void givenAvailabilityInformationAlreadyKnown_whenMonitoringAvailability_thenShouldNotCreateEntryStatusTask() {
        networkNode.setBluetoothMacAddress("00:3F:2F:64:C6:4F");
        long nodeId = umAppDatabase.getNetworkNodeDao().insert(networkNode);

        for(int i = 0; i < containerUids.size(); i++){
            long entryId = containerUids.get(i);
            EntryStatusResponse response = new EntryStatusResponse();
            response.setAvailable(true);
            response.setErContainerUid(entryId);
            response.setErNodeId((int) nodeId);
            response.setErId(i+1);
            response.setResponseTime(10L);
            entryStatusResponseList.add(response);
        }

        umAppDatabase.getEntryStatusResponseDao().insert(entryStatusResponseList);
        mockedNetworkManager.startMonitoringAvailability(monitor, containerUids);

        //Verify that entry status task was not created
        verify(mockedNetworkManager, times(0))
                .makeEntryStatusTask(context, containerUids,networkNode);
    }

}
