package com.ustadmobile.port.sharedse.networkmanager;


import com.ustadmobile.lib.db.entities.NetworkNode;

import org.junit.Test;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestNetworkManagerBle {

    @Test
    public void givenEntryStatusNotKnown_whenStartMonitoringAvailabilityCalled_thenShouldCreateEntryStatusTask() {
        NetworkManager testManager = spy(NetworkManager.class);

        BleEntryStatusTask task1 = mock(BleEntryStatusTask.class);

        when(testManager.makeEntryStatusTask(any(), any())).thenReturn(task1);

        Object availabilityClient = new Object();
        testManager.startMonitoringAvailability(availabilityClient, Arrays.asList(64L));

        verify(testManager).makeEntryStatusTask(any(), any());

        //will have been called async - we need to wait for it to run
        verify(task1, timeout(5000)).run();

    }

    @Test
    public void givenMonitoringAvailabilityStarted_whenNewNodeDiscovered_thenShouldCreateEntryStatusTask() {
        NetworkManager testManager = spy(NetworkManager.class);
        BleEntryStatusTask task1 = mock(BleEntryStatusTask.class);

        //TODO: first argument matcher should check that the given uid is in the list
        NetworkNode node = new NetworkNode();
        when(testManager.makeEntryStatusTask(any(), eq(node))).thenReturn(task1);
        Object availabilityClient = new Object();


        testManager.startMonitoringAvailability(availabilityClient, Arrays.asList(64L));
        testManager.handleNodeDiscovered(node);

        verify(testManager).makeEntryStatusTask(any(), node);

        //will have been called async - we need to wait for it to run
        verify(task1, timeout(5000)).run();
    }

    public void givenMonitoringAvailabilityStopped_whenNewNodeDiscovered_thenShouldNotCreateEntryStatusTask() {

    }

    public void givenAvailabilityInformationAlreadyKnown_whenMonitoringAvailability_thenShouldNotCreateEntryStatusTask() {

    }

}
