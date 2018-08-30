package com.ustadmobile.port.sharedse.networkmanager;


import android.content.Context;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.EntryStatusResponse;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroidBle;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Test class which tests {@link com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroidBle} to make sure it behaves as expected
 * on entry status monitoring logic
 *
 * @author kileha3
 */
public class BleNetworkManagerTest {

    private UmAppDatabase umAppDatabase;
    private NetworkManagerAndroidBle networkManager;
    private NetworkNode networkNode;
    private Object monitor = new Object();
    private List<Long> entries = Arrays.asList(1056289670L,4590875612L,9076137860L,2912543894L);
    private List<ContentEntry> contentEntryList = new ArrayList<>();
    private List<EntryStatusResponse> entryStatusResponseList = new ArrayList<>();

    @Before
    public void setUp(){
        Context context = (Context) PlatformTestUtil.getTargetContext();
        umAppDatabase = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        networkManager = (NetworkManagerAndroidBle) UstadMobileSystemImplAndroid
                .getInstanceAndroid().getNetworkManagerBle();
        networkManager.init(context);
        networkNode = new NetworkNode();

        for(int i = 0 ; i < entries.size(); i++){
            long entryId = entries.get(i);
            ContentEntry contentEntry = new ContentEntry();
            contentEntry.setContentEntryUid(entryId);
            contentEntry.setDescription("Content of entry number "+entryId);
            contentEntry.setTitle("Title of entry number "+entryId);
            contentEntryList.add(contentEntry);
        }

        for(int i = 0 ; i < entries.size(); i++){
            long entryId = entries.get(i);
            EntryStatusResponse response = new EntryStatusResponse();
            response.setAvailable(true);
            response.setEntryId(entryId);
            response.setResponderNodeId(1);
            response.setId(i+1);
            response.setResponseTime(10L);
            response.setUpdated(11L);
            entryStatusResponseList.add(response);
        }

    }


    @Test
    public void givenMonitoringAvailabilityStarted_whenNewNodeDiscovered_thenShouldCreateEntryStatusTask() {
        networkNode.setBluetoothMacAddress("00:3F:2F:64:C6:4F");
        networkManager.startMonitoringAvailability(monitor,entries);
        networkManager.handleNodeDiscovered(networkNode);

        Vector<BleEntryStatusTask> statusTasks = networkManager.getEntryStatusTasks();
        boolean taskCreated = false;
        for(int i = 0; i < statusTasks.size(); i++){
            taskCreated = statusTasks.get(i).getNetworkNode()
                    .getBluetoothMacAddress().equals(networkNode.getBluetoothMacAddress());
            if(taskCreated){
                break;
            }
        }

        assertTrue("EntryStatusTask for the network node was created", taskCreated);
    }

    @Test
    public void givenEntryStatusNotKnown_whenStartMonitoringAvailabilityCalled_thenShouldCreateEntryStatusTask() {
        //Insert entries content
        umAppDatabase.getContentEntryDao().insert(contentEntryList);
        networkManager.startMonitoringAvailability(monitor,entries);
        assertTrue("EntryStatusTask was created for unknown entries response",
                networkManager.getEntryStatusTasks().size() > 0);
    }

    @Test
    public void givenMonitoringAvailabilityStopped_whenNewNodeDiscovered_thenShouldNotCreateEntryStatusTask() {
        networkNode.setBluetoothMacAddress("00:8D:E9:A1:61:88");
        networkManager.startMonitoringAvailability(monitor,entries);
        networkManager.stopMonitoringAvailability(monitor);
        networkManager.handleNodeDiscovered(networkNode);

        Vector<BleEntryStatusTask> statusTasks = networkManager.getEntryStatusTasks();
        boolean taskCreated = false;
        for(int i = 0; i < statusTasks.size(); i++){
            taskCreated = statusTasks.get(i).getNetworkNode()
                    .getBluetoothMacAddress().equals(networkNode.getBluetoothMacAddress());
            if(taskCreated){
                break;
            }
        }

        assertFalse("EntryStatusTask for the network node was not created", taskCreated);
    }

    @Test
    public void givenAvailabilityInformationAlreadyKnown_whenMonitoringAvailability_thenShouldNotCreateEntryStatusTask() {
        umAppDatabase.getEntryStatusResponseDao().insert(entryStatusResponseList);
        networkManager.startMonitoringAvailability(monitor,entries);

        Vector<BleEntryStatusTask> statusTasks = networkManager.getEntryStatusTasks();
        boolean taskCreated = false;
        for(int i = 0; i < statusTasks.size(); i++){
            taskCreated = statusTasks.get(i).getNetworkNode()
                    .getBluetoothMacAddress().equals(networkNode.getBluetoothMacAddress());
            if(taskCreated){
                break;
            }
        }

        assertFalse("EntryStatus tasks for the entries were not created", taskCreated);
    }

}
