package com.ustadmobile.port.sharedse.networkmanager;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil.bleMessageLongToBytes;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_REQUEST;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_RESPONSE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_CREATION_REQUEST;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_CREATION_RESPONSE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Test class which tests {@link com.ustadmobile.port.sharedse.networkmanager.BleGattServer}
 * to make sure it behaves as expected when given different message with different request types.
 *
 * @author kileha3
 */

public class BleGattServerTest {

    private BleGattServer gattServer;

    private List<Long> entries;

    private NetworkManagerBle mockedNetworkManager;

    @Before
    public void setUpSpy(){
        gattServer = spy(BleGattServer.class);
        entries = Arrays.asList(1056289670L,4590875612L,9076137860L,2912543894L);
        mockedNetworkManager = mock(NetworkManagerBle.class);
    }

    @Test
    public void givenRequestMessageWithCorrectRequestHeader_whenHandlingIt_thenShouldReturnResponseMessage(){
        BleMessage messageToSend = new BleMessage(ENTRY_STATUS_REQUEST, bleMessageLongToBytes(entries));

        BleMessage responseMessage = gattServer.handleRequest(messageToSend);

        assertEquals("Should return the right response request type",
                ENTRY_STATUS_RESPONSE,responseMessage.getRequestType());
    }


    @Test
    public void givenRequestMessageWithWrongRequestHeader_whenHandlingIt_thenShouldNoReturnResponseMessage(){
        BleMessage messageToSend = new BleMessage((byte) 0, bleMessageLongToBytes(entries));

        BleMessage responseMessage = gattServer.handleRequest(messageToSend);

        assertNull("Response message should be null", responseMessage);
    }


    @Test
    public void givenRequestToCreateGroup_whenHandlingIt_thenShouldCreateAGroupAndPassGroupDetails(){
        BleMessage messageToSend = new BleMessage(WIFI_GROUP_CREATION_REQUEST,
                bleMessageLongToBytes(entries));

        BleMessage responseMessage = gattServer.handleRequest(messageToSend);

        //will have been called async - we need to wait for group creation to finish
        verify(mockedNetworkManager).createWifiDirectGroup(any());

        assertEquals("Should return the right response",
                WIFI_GROUP_CREATION_RESPONSE,responseMessage.getRequestType());
    }


    @Test
    public void givenRequestWithAvailableEntries_whenHandlingIt_thenShouldReplyTheyAreAvailable(){
        BleMessage messageToSend = new BleMessage(ENTRY_STATUS_REQUEST, bleMessageLongToBytes(entries));

        BleMessage responseMessage = gattServer.handleRequest(messageToSend);

        //TODO: Query to the database to get Entry status result

    }

    @Test
    public void givenRequestWithUnAvailableEntries_whenHandlingIt_thenShouldReplyTheyAreNotAvailable(){
        BleMessage messageToSend = new BleMessage(ENTRY_STATUS_REQUEST,bleMessageLongToBytes(entries));

        BleMessage responseMessage = gattServer.handleRequest(messageToSend);

        //TODO: Query to the database to get Entry status result
    }

}
