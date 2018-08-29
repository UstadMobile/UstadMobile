package com.ustadmobile.port.sharedse.networkmanager;


import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.NetworkNodeDao;
import com.ustadmobile.lib.db.entities.NetworkNode;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import javax.naming.Context;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class which tests {@link NetworkManagerBle} to make sure it behaves as expected
 * on entry status monitoring logic
 *
 * @author kileha3
 */
public class BleNetworkManagerTest {

    private NetworkManagerBle testManager;

    private List<Long> entries;

    private NetworkNode mockedNode;

    private BleEntryStatusTask mockedStatusTask;

    private Context mockedContext;

    private Object availabilityClient = new Object();

    @Before
    public void setUp() {
        testManager = spy(NetworkManagerBle.class);
        mockedContext = mock(Context.class);
        entries = Arrays.asList(1056289670L,4590875612L,9076137860L,2912543894L);
        mockedNode = mock(NetworkNode.class);
        testManager.init(mockedContext);
        mockedStatusTask = mock(BleEntryStatusTask.class);
        NetworkNodeDao mockedDao = mock(NetworkNodeDao.class);
        UmAppDatabase appDatabase = mock(UmAppDatabase.class);
        when(appDatabase.getNetworkNodeDao()).thenReturn(mockedDao);
        when(mockedDao.findNodeByBluetoothAddress(any())).thenReturn(mockedNode);
        when(testManager.makeEntryStatusTask(any(),any(),any())).thenReturn(mockedStatusTask);
    }

    @Test
    public void givenEntryStatusNotKnown_whenStartMonitoringAvailabilityCalled_thenShouldCreateEntryStatusTask() {
        Object availabilityClient = new Object();
        testManager.startMonitoringAvailability(availabilityClient, entries);

        verify(testManager).makeEntryStatusTask(any(),any(),any());

        //will have been called async - we need to wait for it to run
        verify(mockedStatusTask, timeout(5000)).run();

    }

    @Test
    public void givenMonitoringAvailabilityStarted_whenNewNodeDiscovered_thenShouldCreateEntryStatusTask() {
        testManager.startMonitoringAvailability(availabilityClient, entries);

        testManager.handleNodeDiscovered(mockedNode);
        verify(testManager).makeEntryStatusTask(mockedContext,entries, mockedNode);

        //will have been called async - we need to wait for it to run
        verify(mockedStatusTask, timeout(5000)).run();
    }

    @Test
    public void givenMonitoringAvailabilityStopped_whenNewNodeDiscovered_thenShouldNotCreateEntryStatusTask() {
        testManager.startMonitoringAvailability(availabilityClient, entries);
        testManager.stopMonitoringAvailability(availabilityClient);
        testManager.handleNodeDiscovered(mockedNode);

        //Verify that makeEntryStatusTask was not called even once
        verify(testManager,never()).makeEntryStatusTask(mockedContext,entries, mockedNode);
    }

    @Test
    public void givenAvailabilityInformationAlreadyKnown_whenMonitoringAvailability_thenShouldNotCreateEntryStatusTask() {
        //TODO: This should be linked to the database
    }

}
