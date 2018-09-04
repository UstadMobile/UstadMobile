package com.ustadmobile.port.sharedse.networkmanager;


import android.content.Context;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.EntryStatusResponse;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class which tests {@link com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroidBle}
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
    private List<Long> entries = Arrays.asList(1056289670L,9076137860L,4590875612L,2912543894L);
    private List<EntryStatusResponse> entryStatusResponseList = new ArrayList<>();
    private List<ContentEntry> contentEntryList = new ArrayList<>();
    private BleEntryStatusTask mockedEntryStatusTask;
    private Context context;

    @Before
    public void setUp(){
        context = (Context) PlatformTestUtil.getTargetContext();
        mockedEntryStatusTask = mock(BleEntryStatusTask.class);
        Collections.sort(entries);
        networkNode = new NetworkNode();
        mockedNetworkManager = spy(NetworkManagerBle.class);
        mockedNetworkManager.init(context);
        when(mockedNetworkManager
                .makeEntryStatusTask(eq(context),eq(entries),any()))
                .thenReturn(mockedEntryStatusTask);

        umAppDatabase = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        umAppDatabase.clearAllTables();


        for(int i = 0 ; i < entries.size(); i++){
            long entryId = entries.get(i);
            ContentEntry contentEntry = new ContentEntry();
            contentEntry.setContentEntryUid(entryId);
            contentEntry.setDescription("Content of entry number "+entryId);
            contentEntry.setTitle("Title of entry number "+entryId);
            contentEntryList.add(contentEntry);
        }

        umAppDatabase.getContentEntryDao().insert(contentEntryList);
    }


    @Test
    public void givenMonitoringAvailabilityStarted_whenNewNodeDiscovered_thenShouldCreateEntryStatusTask() {
        networkNode.setBluetoothMacAddress("00:3F:2F:64:C6:4F");
        mockedNetworkManager.startMonitoringAvailability(monitor,entries);
        mockedNetworkManager.handleNodeDiscovered(networkNode);

        /*Verify that entry status task was created, we are interested in call
        from handleNodeDiscovered method since startMonitoringAvailability can
        call this method too*/
        verify(mockedNetworkManager,timeout(testCaseVerifyTimeOut))
                .makeEntryStatusTask(eq(context),eq(entries),any());

        //Verify if the run() method was called
        verify(mockedEntryStatusTask,timeout(testCaseVerifyTimeOut)).run();
    }

    @Test
    public void givenEntryStatusNotKnown_whenStartMonitoringAvailabilityCalled_thenShouldCreateEntryStatusTask() {
        networkNode.setBluetoothMacAddress("00:3F:2F:64:C6:4B");
        umAppDatabase.getNetworkNodeDao().insert(networkNode);
        mockedNetworkManager.startMonitoringAvailability(monitor,entries);

        //Verify that entry status task was created
        verify(mockedNetworkManager,timeout(testCaseVerifyTimeOut))
                .makeEntryStatusTask(eq(context),eq(entries),any());

        //Verify that the run() method was called
        verify(mockedEntryStatusTask,timeout(testCaseVerifyTimeOut)).run();
    }

    @Test
    public void givenMonitoringAvailabilityStopped_whenNewNodeDiscovered_thenShouldNotCreateEntryStatusTask() {
        mockedNetworkManager.startMonitoringAvailability(monitor,entries);
        mockedNetworkManager.stopMonitoringAvailability(monitor);
        mockedNetworkManager.handleNodeDiscovered(networkNode);

        //Verify that entry status task was not created
        verify(mockedNetworkManager.makeEntryStatusTask(context,entries,networkNode),
                times(0));
    }

    @Test
    public void givenAvailabilityInformationAlreadyKnown_whenMonitoringAvailability_thenShouldNotCreateEntryStatusTask() {
        networkNode.setBluetoothMacAddress("00:3F:2F:64:C6:4F");
        long nodeId = umAppDatabase.getNetworkNodeDao().insert(networkNode);

        for(int i = 0 ; i < entries.size(); i++){
            long entryId = entries.get(i);
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
        mockedNetworkManager.startMonitoringAvailability(monitor,entries);

        //Verify that entry status task was not created
        verify(mockedNetworkManager, times(0))
                .makeEntryStatusTask(context,entries,networkNode);
    }

}
