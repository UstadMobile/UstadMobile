package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.core.networkmanager.NetworkManagerListener;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.test.core.impl.PlatformTestUtil;
import com.ustadmobile.test.sharedse.http.RemoteTestServerHttpd;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * Runs a basic test to see if we can connect to a Wifi Direct group
 *
 * Created by mike on 6/2/17.
 */
public class TestWifiDirectGroupConnection {

    public static final int CONNECTION_TIMEOUT = 90 * 1000;

    private static final int CONNECTION_TEST_COUNT = 3;

    private String groupSsid;

    private String groupPasphrase;

    @Test
    public void testWifiDirectGroupConnection() throws IOException{
        NetworkManager manager = UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        SharedSeNetworkTestSuite.assumeNetworkHardwareEnabled();

        UstadMobileSystemImpl.l(UMLog.INFO, 324, "TestWifiDirectGroupConnection: start");
        String createGroupUrl = PlatformTestUtil.getRemoteTestEndpoint() + "?cmd="
                + RemoteTestServerHttpd.CMD_CREATEGROUP;
//        HTTPResult result = UstadMobileSystemImpl.getInstance().makeRequest(createGroupUrl, null, null);

        HttpURLConnection urlConnection = (HttpURLConnection)new URL(createGroupUrl).openConnection();


        Assert.assertEquals("Group created", 200, urlConnection.getResponseCode());

        JSONObject object = new JSONObject(UMIOUtils.readStreamToString(urlConnection.getInputStream()));

        groupSsid = object.getString("ssid");
        groupPasphrase = object.getString("passphrase");

        Assert.assertNotNull("Got ssid", groupSsid);
        Assert.assertNotNull("Got passphrase", groupPasphrase);

        final String[] connectedSsid = new String[1];
        final Object connectionLock = new Object();
        NetworkManagerListener listener = new NetworkManagerListener() {
            @Override
            public void fileStatusCheckInformationAvailable(String[] fileIds) {

            }

            @Override
            public void networkTaskStatusChanged(NetworkTask task) {

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
                if(connected) {
                    connectedSsid[0] = ssid;
                    synchronized (connectionLock) {
                        connectionLock.notify();
                    }
                }
            }
        };
        manager.addNetworkManagerListener(listener);

        try {
            for (int i = 0; i < CONNECTION_TEST_COUNT; i++) {
                UstadMobileSystemImpl.l(UMLog.INFO, 0, "Test WiFi direct group connection run #" + i);
                manager.connectToWifiDirectGroup(groupSsid, groupPasphrase);

                synchronized (connectionLock) {
                    try {connectionLock.wait(CONNECTION_TIMEOUT);}
                    catch(InterruptedException e) {}
                }

                try { Thread.sleep(500); }
                catch(InterruptedException e) {}

                UstadMobileSystemImpl.l(UMLog.DEBUG, 700, "Connected to group SSID: " +
                    manager.getCurrentWifiSsid());
                Assert.assertEquals("Connected to created group ssid: as per getCurrentWifiSsid", groupSsid,
                        manager.getCurrentWifiSsid());
                Assert.assertEquals("Connected to created group ssid: as per ssid passed to event", groupSsid,
                        connectedSsid[0]);

                try { Thread.sleep(2000); }
                catch(InterruptedException e) {}

                manager.restoreWifi();
                synchronized (connectionLock) {
                    try { connectionLock.wait(CONNECTION_TIMEOUT); }
                    catch(InterruptedException e) {}
                }

                try { Thread.sleep(500); }
                catch(InterruptedException e) {}

                UstadMobileSystemImpl.l(UMLog.DEBUG, 700, "Connected to 'normal' SSID: " +
                        manager.getCurrentWifiSsid());
                Assert.assertNotEquals("Connected back to 'normal' wifi as per getCurrentWiFiSsid", groupSsid,
                        manager.getCurrentWifiSsid());
                Assert.assertNotEquals("Connected back to 'normal' wifi as per ssid passed to event", groupSsid,
                        connectedSsid[0]);

            }
        } finally {
            manager.removeNetworkManagerListener(listener);
        }


    }

}
