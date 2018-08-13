package com.ustadmobile.port.sharedse.networkmanager;


import com.ustadmobile.lib.db.entities.NetworkNode;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * <h1>BleNetworkManagerTest</h1>
 *
 * Test class which tests {@link NetworkManager} to make sure it behaves as expected
 * on entry status monitoring logic
 *
 * @author kileha3
 */
public class BleNetworkManagerTest {

    @Test
    public void givenEntryStatusNotKnown_whenStartMonitoringAvailabilityCalled_thenShouldCreateEntryStatusTask() {
        NetworkManager testManager = spy(NetworkManager.class);

        BleEntryStatusTask task1 = mock(BleEntryStatusTask.class);

        when(testManager.makeEntryStatusTask(any(), any())).thenReturn(task1);

        Object availabilityClient = new Object();
        testManager.startMonitoringAvailability(availabilityClient, Collections.singletonList(64L));

        verify(testManager).makeEntryStatusTask(any(), any());

        //will have been called async - we need to wait for it to run
        verify(task1, timeout(5000)).run();

    }

    @Test
    public void givenMonitoringAvailabilityStarted_whenNewNodeDiscovered_thenShouldCreateEntryStatusTask() {
        NetworkManager testManager = spy(NetworkManager.class);
        BleEntryStatusTask task1 = mock(BleEntryStatusTask.class);
        List<Long> entryUUID = Collections.singletonList(64L);
        NetworkNode node = new NetworkNode();
        when(testManager.makeEntryStatusTask(entryUUID, eq(node))).thenReturn(task1);
        Object availabilityClient = new Object();


        testManager.startMonitoringAvailability(availabilityClient, entryUUID);
        testManager.handleNodeDiscovered(node);

        verify(testManager).makeEntryStatusTask(any(), node);

        //will have been called async - we need to wait for it to run
        verify(task1, timeout(5000)).run();
    }

    @Test
    public void givenMonitoringAvailabilityStopped_whenNewNodeDiscovered_thenShouldNotCreateEntryStatusTask() {
        NetworkManager testManager = spy(NetworkManager.class);
        Object availabilityClient = new Object();
        NetworkNode node = new NetworkNode();


        testManager.startMonitoringAvailability(availabilityClient, Collections.singletonList(64L));
        testManager.stopMonitoringAvailability(availabilityClient);
        testManager.handleNodeDiscovered(node);

        //Verify that makeEntryStatusTask was not called even once
        verify(testManager,never()).makeEntryStatusTask(any(), node);
    }

    @Test
    public void givenAvailabilityInformationAlreadyKnown_whenMonitoringAvailability_thenShouldNotCreateEntryStatusTask() {
        //TODO: This should be linked to the database
    }

}
