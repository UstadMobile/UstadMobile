package com.ustadmobile.test.sharedse;

import com.ustadmobile.core.networkmanager.AvailabilityMonitorRequest;
import com.ustadmobile.core.networkmanager.NetworkManagerListener;
import com.ustadmobile.core.networkmanager.NetworkNode;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;

import static com.ustadmobile.test.sharedse.TestEntryStatusTask.EXPECTED_AVAILABILITY;
import static com.ustadmobile.test.sharedse.TestEntryStatusTask.doesAvailabilityMatch;
import static com.ustadmobile.test.sharedse.TestEntryStatusTask.AVAILABILITY_MONITOR_TIMEOUT;

/**
 * Test the mechanism by which we wathc the entry status
 */
public class TestNetworkManagerEntryStatusMonitoring {

    @Test(timeout = com.ustadmobile.test.sharedse.TestEntryStatusTask.AVAILABILITY_MONITOR_TIMEOUT)
    public void testEntryStatusMonitor() throws IOException {
        final NetworkManager manager = UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        manager.resetKnownNodeInfo();

        final Hashtable<String, Boolean> actualEntryStatuses = new Hashtable();
        Assert.assertTrue("Test slave supernode enabled",
                TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(true));
        final Object discoverLock = new Object();
        NetworkManagerListener listener = new NetworkManagerListener() {
            @Override
            public void fileStatusCheckInformationAvailable(String[] fileIds) {
                for(int i = 0; i < fileIds.length; i++) {
                    actualEntryStatuses.put(fileIds[i], manager.isFileAvailable(fileIds[i]));
                }

                if(doesAvailabilityMatch(EXPECTED_AVAILABILITY, actualEntryStatuses)) {
                    synchronized (discoverLock) {
                        discoverLock.notifyAll();
                    }
                }
            }

            @Override
            public void networkNodeDiscovered(NetworkNode node) {

            }

            @Override
            public void networkNodeUpdated(NetworkNode node) {

            }

            @Override
            public void fileAcquisitionInformationAvailable(String entryId, long downloadId, int downloadSource) {

            }

            @Override
            public void wifiConnectionChanged(String ssid, boolean connected, boolean connectedOrConnecting) {

            }

            @Override
            public void networkTaskStatusChanged(NetworkTask networkTask) {

            }
        };
        manager.addNetworkManagerListener(listener);

        AvailabilityMonitorRequest request = new AvailabilityMonitorRequest(EXPECTED_AVAILABILITY.keySet());
        manager.startMonitoringAvailability(request);
        synchronized (discoverLock) {
            try { discoverLock.wait(AVAILABILITY_MONITOR_TIMEOUT);}
            catch(InterruptedException e) {}
        }

        manager.stopMonitoringAvailability(request);
        manager.removeNetworkManagerListener(listener);

        Iterator<String> keyIterator = EXPECTED_AVAILABILITY.keySet().iterator();
        String currentKey;
        while(keyIterator.hasNext()) {
            currentKey = keyIterator.next();
            Assert.assertEquals("Expected availability matches actual availability for " + currentKey,
                    EXPECTED_AVAILABILITY.get(currentKey), actualEntryStatuses.get(currentKey));
        }


        Assert.assertTrue("Test slave supernode disabled",
                TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(false));
    }
}
