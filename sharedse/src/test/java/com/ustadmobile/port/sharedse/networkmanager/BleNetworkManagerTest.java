package com.ustadmobile.port.sharedse.networkmanager;


import org.junit.Before;
import org.junit.Test;

/**
 * Test class which tests {@link NetworkManagerBle} to make sure it behaves as expected
 * on entry status monitoring logic
 *
 * @author kileha3
 */
public class BleNetworkManagerTest {




    @Before
    public void setUp() {


    }



    @Test
    public void givenMonitoringAvailabilityStarted_whenNewNodeDiscovered_thenShouldCreateEntryStatusTask() {
        //TODO: Implement with better options

    }

    @Test
    public void givenEntryStatusNotKnown_whenStartMonitoringAvailabilityCalled_thenShouldCreateEntryStatusTask() {
        //TODO: Implement with better options

    }

    @Test
    public void givenMonitoringAvailabilityStopped_whenNewNodeDiscovered_thenShouldNotCreateEntryStatusTask() {
        //TODO: Implement with better options
    }

    @Test
    public void givenAvailabilityInformationAlreadyKnown_whenMonitoringAvailability_thenShouldNotCreateEntryStatusTask() {
        //TODO: This should be linked to the database
    }

}
