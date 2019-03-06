package com.ustadmobile.port.sharedse.networkmanager;

import com.google.gson.Gson;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.database.jdbc.DriverConnectionPoolInitializer;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;
import com.ustadmobile.sharedse.SharedSeTestConfig;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.ustadmobile.port.sharedse.networkmanager.BleGattServer.GROUP_CREATION_TIMEOUT;
import static com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil.bleMessageLongToBytes;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_REQUEST;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_RESPONSE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_DIRECT_GROUP_ACTIVE_STATUS;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_DIRECT_GROUP_UNDER_CREATION_STATUS;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_CREATION_RESPONSE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_REQUEST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test class which tests {@link com.ustadmobile.port.sharedse.networkmanager.BleGattServer}
 * to make sure it behaves as expected when given different message with different request types.
 *
 * @author kileha3
 */

public class BleGattServerTest {

    private NetworkManagerBle mockedNetworkManager;

    private List<Long> containerUids = new ArrayList<>();

    private BleGattServer gattServer;

    private List<Container> containerList = new ArrayList<>();

    private WiFiDirectGroupBle wiFiDirectGroupBle;

    private CountDownLatch mLatch = new CountDownLatch(1);

    static final int MAX_ENTITIES_NUMBER = 4;

    private UmAppDatabase umAppDatabase;


    @Before
    public void setUp() throws IOException {
        DriverConnectionPoolInitializer.bindDataSource("UmAppDatabase",
                SharedSeTestConfig.TESTDB_JDBCURL_UMMAPPDATABASE, true);
        Object context =  PlatformTestUtil.getTargetContext();
        mockedNetworkManager = spy(NetworkManagerBle.class);
        mockedNetworkManager.onCreate();
        mockedNetworkManager.setContext(context);
        EmbeddedHTTPD httpd = new EmbeddedHTTPD(0,context);
        httpd.start();

        when(mockedNetworkManager.getHttpd()).thenReturn(httpd);

        umAppDatabase =  UmAppDatabase.getInstance(context);
        umAppDatabase.clearAllTables();

        gattServer = spy(BleGattServer.class);
        wiFiDirectGroupBle = new WiFiDirectGroupBle("NetworkSsId","@@@1234");
        gattServer.setNetworkManager(mockedNetworkManager);

        for(int i = 0; i < MAX_ENTITIES_NUMBER; i++){
            long currentTimeStamp = Calendar.getInstance().getTimeInMillis();
            Container entryFile = new Container();
            entryFile.setLastModified(currentTimeStamp);
            containerList.add(entryFile);
        }

        containerUids.addAll(Arrays.asList(umAppDatabase.getContainerDao().insert(containerList)));

    }

    @Test
    public void givenRequestMessageWithCorrectRequestHeader_whenHandlingIt_thenShouldReturnResponseMessage(){
        BleMessage messageToSend = new BleMessage(ENTRY_STATUS_REQUEST, bleMessageLongToBytes(containerUids));

        BleMessage responseMessage = gattServer.handleRequest(messageToSend);

        assertEquals("Should return the right response request type",
                ENTRY_STATUS_RESPONSE,responseMessage.getRequestType());
    }


    @Test
    public void givenRequestMessageWithWrongRequestHeader_whenHandlingIt_thenShouldNotReturnResponseMessage(){
        BleMessage messageToSend = new BleMessage((byte) 0, bleMessageLongToBytes(containerUids));

        BleMessage responseMessage = gattServer.handleRequest(messageToSend);

        assertNull("Response message should be null", responseMessage);
    }


    @Test
    public void givenNoWifiDirectGroupExisting_whenWifiDirectGroupRequested_thenShouldCreateAGroupAndPassGroupDetails(){
        doAnswer(invocation -> {
            gattServer.groupCreated(wiFiDirectGroupBle,null);
            return null;
        }).when(mockedNetworkManager).createWifiDirectGroup();

        BleMessage messageToSend = new BleMessage(WIFI_GROUP_REQUEST, bleMessageLongToBytes(containerUids));

        BleMessage responseMessage = gattServer.handleRequest(messageToSend);

        WiFiDirectGroupBle groupBle = new Gson().fromJson(new String(responseMessage.getPayload()),
                WiFiDirectGroupBle.class);

        //Verify that wifi direct group creation was initiated
        verify(mockedNetworkManager).createWifiDirectGroup();

        assertEquals("Should return the right response",
                WIFI_GROUP_CREATION_RESPONSE,responseMessage.getRequestType());

        assertTrue("Returned the right Wifi direct group information",
                wiFiDirectGroupBle.getPassphrase().equals(groupBle.getPassphrase()) &&
                        wiFiDirectGroupBle.getSsid().equals(groupBle.getSsid()));

    }

    @Test
    public void givenWifiDirectGroupUnderCreation_whenWifiDirectGroupRequested_thenShouldWaitAndProvideGroupDetails() {
        doAnswer(invocation -> {
            if(mockedNetworkManager.getWifiDirectGroupChangeStatus() ==
                    WIFI_DIRECT_GROUP_UNDER_CREATION_STATUS){

                try { mLatch.await(GROUP_CREATION_TIMEOUT, TimeUnit.SECONDS); }
                catch(InterruptedException e) {
                    e.printStackTrace();
                    mLatch.countDown();
                }
                gattServer.groupCreated(wiFiDirectGroupBle,null);
            }
            return null;
        }).when(mockedNetworkManager).createWifiDirectGroup();

        mockedNetworkManager.setWifiDirectGroupChangeStatus(WIFI_DIRECT_GROUP_UNDER_CREATION_STATUS);

        BleMessage messageToSend = new BleMessage(WIFI_GROUP_REQUEST, bleMessageLongToBytes(containerUids));
        BleMessage responseMessage = gattServer.handleRequest(messageToSend);

        WiFiDirectGroupBle groupBle = new Gson().fromJson(new String(responseMessage.getPayload()),
                WiFiDirectGroupBle.class);

        //Verify that wifi direct group creation was initiated
        verify(mockedNetworkManager).createWifiDirectGroup();

        assertEquals("Should return the right response",
                WIFI_GROUP_CREATION_RESPONSE,responseMessage.getRequestType());

        assertTrue("Returned the right group information",
                wiFiDirectGroupBle.getPassphrase().equals(groupBle.getPassphrase()) &&
                        wiFiDirectGroupBle.getSsid().equals(groupBle.getSsid()));
    }

    @Test
    public void givenWiFiDirectGroupExists_whenWifiDirectGroupRequested_thenShouldProvideGroupDetails(){

        doAnswer(invocation -> {
            if(mockedNetworkManager.getWifiDirectGroupChangeStatus() == WIFI_DIRECT_GROUP_ACTIVE_STATUS){
                gattServer.groupCreated(wiFiDirectGroupBle,null);
            }
            return null;
        }).when(mockedNetworkManager).createWifiDirectGroup();

        BleMessage messageToSend = new BleMessage(WIFI_GROUP_REQUEST, bleMessageLongToBytes(containerUids));
        mockedNetworkManager.setWifiDirectGroupChangeStatus(WIFI_DIRECT_GROUP_ACTIVE_STATUS);

        BleMessage responseMessage = gattServer.handleRequest(messageToSend);

        WiFiDirectGroupBle groupBle = new Gson().fromJson(new String(responseMessage.getPayload()),
                WiFiDirectGroupBle.class);

        //Verify that wifi direct group creation was initiated
        verify(mockedNetworkManager).createWifiDirectGroup();

        assertEquals("Should return the right response",
                WIFI_GROUP_CREATION_RESPONSE,responseMessage.getRequestType());

        assertTrue("Returned the right group information",
                wiFiDirectGroupBle.getPassphrase().equals(groupBle.getPassphrase()) &&
                        wiFiDirectGroupBle.getSsid().equals(groupBle.getSsid()));
    }

    @Test
    public void givenWifiDirectGroupBeingRemoved_whenWifiDirectGroupRequested_thenShouldWaitAndCreateNewGroup() {
        doAnswer(invocation -> {
            try { mLatch.await(GROUP_CREATION_TIMEOUT, TimeUnit.SECONDS); }
            catch(InterruptedException e) {
                e.printStackTrace();
                mLatch.countDown();
            }
            gattServer.groupCreated(wiFiDirectGroupBle,null);
            return null;
        }).when(mockedNetworkManager).createWifiDirectGroup();

        mockedNetworkManager.removeWifiDirectGroup();

        BleMessage messageToSend = new BleMessage(WIFI_GROUP_REQUEST, bleMessageLongToBytes(containerUids));
        BleMessage responseMessage = gattServer.handleRequest(messageToSend);

        WiFiDirectGroupBle groupBle = new Gson().fromJson(new String(responseMessage.getPayload()),
                WiFiDirectGroupBle.class);

        //Verify that wifi direct group creation was initiated
        verify(mockedNetworkManager).createWifiDirectGroup();

        assertEquals("Should return the right response",
                WIFI_GROUP_CREATION_RESPONSE,responseMessage.getRequestType());

        assertTrue("Returned the right group information",
                wiFiDirectGroupBle.getPassphrase().equals(groupBle.getPassphrase()) &&
                        wiFiDirectGroupBle.getSsid().equals(groupBle.getSsid()));
    }


    @Test
    public void givenRequestWithAvailableEntries_whenHandlingIt_thenShouldReplyTheyAreAvailable(){


        BleMessage messageToSend = new BleMessage(ENTRY_STATUS_REQUEST, bleMessageLongToBytes(containerUids));

        BleMessage responseMessage = gattServer.handleRequest(messageToSend);
        List<Long> responseList = BleMessageUtil.bleMessageBytesToLong(responseMessage.getPayload());
        int availabilityCounter = 0;
        for(long response: responseList){
            if(response != 0){
                availabilityCounter++;
            }
        }

        assertEquals("All requested entry uuids status are available",
                containerUids.size(), availabilityCounter);

    }

    @Test
    public void givenRequestWithUnAvailableEntries_whenHandlingIt_thenShouldReplyTheyAreNotAvailable(){

        BleMessage messageToSend = new BleMessage(ENTRY_STATUS_REQUEST, bleMessageLongToBytes(containerUids));
        umAppDatabase.clearAllTables();
        BleMessage responseMessage = gattServer.handleRequest(messageToSend);
        List<Long> responseList = BleMessageUtil.bleMessageBytesToLong(responseMessage.getPayload());
        int availabilityCounter = 0;
        for(long response: responseList){
            if(response != 0){
                availabilityCounter++;
            }
        }

        assertEquals("All requested entry uuids status are not available",
                0, availabilityCounter);
    }

}

