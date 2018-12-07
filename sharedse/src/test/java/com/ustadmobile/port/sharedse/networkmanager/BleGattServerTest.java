package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil.bleMessageLongToBytes;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_REQUEST;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_RESPONSE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_DIRECT_GROUP_ACTIVE_STATUS;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_DIRECT_GROUP_UNDER_CREATION_STATUS;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_CREATION_RESPONSE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_INFO_SEPARATOR;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_REQUEST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Test class which tests {@link com.ustadmobile.port.sharedse.networkmanager.BleGattServer}
 * to make sure it behaves as expected when given different message with different request types.
 *
 * @author kileha3
 */

public class BleGattServerTest {

    private UmAppDatabase repository;

    private NetworkManagerBle mockedNetworkManager;

    private List<Long> entries = Arrays.asList(1056289670L,9076137860L,4590875612L,2912543894L);

    private BleGattServer gattServer;

    private List<ContentEntry> contentEntryList = new ArrayList<>();

    private WiFiDirectGroupBle wiFiDirectGroupBle;

    private final Object wifiCreationLock = new Object();

    private long testCaseTimeOut = TimeUnit.SECONDS.toMillis(1);

    @Before
    public void setUpSpy(){
        Object context =  PlatformTestUtil.getTargetContext();
        Collections.sort(entries);
        mockedNetworkManager = spy(NetworkManagerBle.class);
        mockedNetworkManager.init(context);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        repository.clearAllTables();
        gattServer = spy(BleGattServer.class);
        wiFiDirectGroupBle = new WiFiDirectGroupBle("NetworkSsId","@@@1234");
        gattServer.setNetworkManager(mockedNetworkManager);

        long currentTimeStamp = Calendar.getInstance().getTimeInMillis();
        for(int i = 0 ; i < entries.size(); i++){
            long entryId = entries.get(i);
            ContentEntry contentEntry = new ContentEntry();
            contentEntry.setContentEntryUid(entryId);
            contentEntry.setLastUpdateTime(currentTimeStamp);
            contentEntry.setDescription("Content of entry number "+entryId);
            contentEntry.setTitle("Title of entry number "+entryId);
            contentEntryList.add(contentEntry);
        }
    }

    @Test
    public void givenRequestMessageWithCorrectRequestHeader_whenHandlingIt_thenShouldReturnResponseMessage(){
        BleMessage messageToSend = new BleMessage(ENTRY_STATUS_REQUEST, bleMessageLongToBytes(entries));

        BleMessage responseMessage = gattServer.handleRequest(messageToSend);

        assertEquals("Should return the right response request type",
                ENTRY_STATUS_RESPONSE,responseMessage.getRequestType());
    }


    @Test
    public void givenRequestMessageWithWrongRequestHeader_whenHandlingIt_thenShouldNotReturnResponseMessage(){
        BleMessage messageToSend = new BleMessage((byte) 0, bleMessageLongToBytes(entries));

        BleMessage responseMessage = gattServer.handleRequest(messageToSend);

        assertNull("Response message should be null", responseMessage);
    }


    @Test
    public void givenNoWifiDirectGroupExisting_whenWifiDirectGroupRequested_thenShouldCreateAGroupAndPassGroupDetails(){
        doAnswer(invocation -> {
            gattServer.groupCreated(wiFiDirectGroupBle,null);
            return null;
        }).when(mockedNetworkManager).createWifiDirectGroup();

        BleMessage messageToSend = new BleMessage(WIFI_GROUP_REQUEST, bleMessageLongToBytes(entries));

        BleMessage responseMessage = gattServer.handleRequest(messageToSend);
        String [] groupInfo = new String(responseMessage.getPayload()).split(WIFI_GROUP_INFO_SEPARATOR);
        WiFiDirectGroupBle groupBle = new WiFiDirectGroupBle(groupInfo[0],groupInfo[1]);

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
                synchronized (wifiCreationLock){
                    wifiCreationLock.wait(testCaseTimeOut);
                    gattServer.groupCreated(wiFiDirectGroupBle,null);
                }
            }
            return null;
        }).when(mockedNetworkManager).createWifiDirectGroup();

        mockedNetworkManager.setWifiDirectGroupChangeStatus(WIFI_DIRECT_GROUP_UNDER_CREATION_STATUS);

        BleMessage messageToSend = new BleMessage(WIFI_GROUP_REQUEST, bleMessageLongToBytes(entries));
        BleMessage responseMessage = gattServer.handleRequest(messageToSend);
        String [] groupInfo = new String(responseMessage.getPayload()).split(WIFI_GROUP_INFO_SEPARATOR);
        WiFiDirectGroupBle groupBle = new WiFiDirectGroupBle(groupInfo[0],groupInfo[1]);

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

        BleMessage messageToSend = new BleMessage(WIFI_GROUP_REQUEST, bleMessageLongToBytes(entries));
        mockedNetworkManager.setWifiDirectGroupChangeStatus(WIFI_DIRECT_GROUP_ACTIVE_STATUS);

        BleMessage responseMessage = gattServer.handleRequest(messageToSend);
        String [] groupInfo = new String(responseMessage.getPayload()).split(WIFI_GROUP_INFO_SEPARATOR);
        WiFiDirectGroupBle groupBle = new WiFiDirectGroupBle(groupInfo[0],groupInfo[1]);

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
            synchronized (wifiCreationLock){
                wifiCreationLock.wait(testCaseTimeOut);
                gattServer.groupCreated(wiFiDirectGroupBle,null);
            }
            return null;
        }).when(mockedNetworkManager).createWifiDirectGroup();

        mockedNetworkManager.removeWifiDirectGroup();

        BleMessage messageToSend = new BleMessage(WIFI_GROUP_REQUEST, bleMessageLongToBytes(entries));
        BleMessage responseMessage = gattServer.handleRequest(messageToSend);
        String [] groupInfo = new String(responseMessage.getPayload()).split(WIFI_GROUP_INFO_SEPARATOR);
        WiFiDirectGroupBle groupBle = new WiFiDirectGroupBle(groupInfo[0],groupInfo[1]);

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
        BleMessage messageToSend = new BleMessage(ENTRY_STATUS_REQUEST, bleMessageLongToBytes(entries));
        Long [] rowCount = repository.getContentEntryDao().insert(contentEntryList);

        assertTrue("Content added successfully", rowCount.length == entries.size());

        BleMessage responseMessage = gattServer.handleRequest(messageToSend);
        List<Long> responseList = BleMessageUtil.bleMessageBytesToLong(responseMessage.getPayload());
        int availabilityCounter = 0;
        for(long response: responseList){
            if(response != 0){
                availabilityCounter++;
            }
        }

        assertTrue("All requested entry uuids status are available",
                entries.size()==availabilityCounter);

    }

    @Test
    public void givenRequestWithUnAvailableEntries_whenHandlingIt_thenShouldReplyTheyAreNotAvailable(){
        BleMessage messageToSend = new BleMessage(ENTRY_STATUS_REQUEST, bleMessageLongToBytes(entries));
        repository.clearAllTables();

        BleMessage responseMessage = gattServer.handleRequest(messageToSend);
        List<Long> responseList = BleMessageUtil.bleMessageBytesToLong(responseMessage.getPayload());
        int availabilityCounter = 0;
        for(long response: responseList){
            if(response != 0){
                availabilityCounter++;
            }
        }

        assertTrue("All requested entry uuids status are not available",
                availabilityCounter == 0);
    }

}

