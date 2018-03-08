package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.core.networkmanager.AvailabilityMonitorRequest;
import com.ustadmobile.core.networkmanager.NetworkManagerListener;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.test.core.annotation.PeerServerRequiredTest;
import com.ustadmobile.test.sharedse.TestUtilsSE;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Test the mechanism by which we wathc the entry status
 */
@PeerServerRequiredTest
public class TestNetworkManagerEntryStatusMonitoring {

    @Test(timeout = TestEntryStatusTask.AVAILABILITY_MONITOR_TIMEOUT)
    public void testEntryStatusMonitor() throws IOException {
        final NetworkManager manager = UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        SharedSeNetworkTestSuite.assumeNetworkHardwareEnabled();

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

                if(TestEntryStatusTask.doesAvailabilityMatch(TestEntryStatusTask.EXPECTED_AVAILABILITY, actualEntryStatuses)) {
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

        AvailabilityMonitorRequest request = new AvailabilityMonitorRequest(TestEntryStatusTask.EXPECTED_AVAILABILITY.keySet());
        manager.startMonitoringAvailability(request, true);
        synchronized (discoverLock) {
            try { discoverLock.wait(TestEntryStatusTask.AVAILABILITY_MONITOR_TIMEOUT);}
            catch(InterruptedException e) {}
        }

        manager.stopMonitoringAvailability(request);
        manager.removeNetworkManagerListener(listener);

        Iterator<String> keyIterator = TestEntryStatusTask.EXPECTED_AVAILABILITY.keySet().iterator();
        String currentKey;
        while(keyIterator.hasNext()) {
            currentKey = keyIterator.next();
            Assert.assertEquals("Expected availability matches actual availability for " + currentKey,
                    TestEntryStatusTask.EXPECTED_AVAILABILITY.get(currentKey), actualEntryStatuses.get(currentKey));
        }


        Assert.assertTrue("Test slave supernode disabled",
                TestUtilsSE.setRemoteTestSlaveSupernodeEnabled(false));
    }
}
