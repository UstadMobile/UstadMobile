package com.ustadmobile.port.sharedse.networkmanager;

import org.junit.Before;
import org.junit.Test;

import static com.ustadmobile.port.sharedse.networkmanager.NetworkManager.ENTRY_STATUS_REQUEST;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManager.ENTRY_STATUS_RESPONSE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManager.WIFI_GROUP_CREATION_REQUEST;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManager.WIFI_GROUP_CREATION_RESPONSE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * <h1>BleGattServerTest</h1>
 *
 * Test class which tests {@link com.ustadmobile.port.sharedse.networkmanager.BleGattServer}
 * to make sure it behaves as expected when given different message with different request types.
 *
 * @author kileha3
 */

public class BleGattServerTest {

    private String message = "dce655f2-34f0-469c-b890-a910039b0afc,c9d07319-2ab0-4a53-82cb-02370f5b8699";

    private int defaultMtu = 20;

    private BleGattServer gattServer;

    @Before
    public void setUpSpy(){
        gattServer = spy(BleGattServer.class);
    }

    @Test
    public void givenRequestMessageWithCorrectRequestHeader_whenHandlingIt_thenShouldReturnResponseMessage(){
        BleMessage messageToSend = new BleMessage(ENTRY_STATUS_REQUEST, message.getBytes(), defaultMtu);

        BleMessage responseMessage = gattServer.handleRequest(messageToSend);

        assertEquals("Should return the right response request type",
                ENTRY_STATUS_RESPONSE,responseMessage.getRequestType());
    }


    @Test
    public void givenRequestMessageWithWrongRequestHeader_whenHandlingIt_thenShouldNoReturnResponseMessage(){
        BleMessage messageToSend = new BleMessage(ENTRY_STATUS_REQUEST, message.getBytes(), defaultMtu);

        BleMessage responseMessage = gattServer.handleRequest(messageToSend);

        assertNull("Response message should be null", responseMessage);
    }

    @Test
    public void givenRequestWithAvailableEntries_whenHandlingIt_thenShouldReplyTheyAreAvailable(){
        BleMessage messageToSend = new BleMessage(ENTRY_STATUS_REQUEST, message.getBytes(), defaultMtu);

        BleMessage responseMessage = gattServer.handleRequest(messageToSend);

        //TODO: Query to the database to get Entry status result

    }

    @Test
    public void givenRequestWithUnAvailableEntries_whenHandlingIt_thenShouldReplyTheyAreNotAvailable(){
        BleMessage messageToSend = new BleMessage(ENTRY_STATUS_REQUEST, message.getBytes(), defaultMtu);

        BleMessage responseMessage = gattServer.handleRequest(messageToSend);

        //TODO: Query to the database to get Entry status result
    }


    @Test
    public void givenRequestToCreateGroup_whenHandlingIt_thenShouldCreateAGroupAndPassGroupDetails(){
        NetworkManager networkManager = mock(NetworkManager.class);
        BleMessage messageToSend = new BleMessage(WIFI_GROUP_CREATION_REQUEST, message.getBytes(), defaultMtu);

        BleMessage responseMessage = gattServer.handleRequest(messageToSend);

        //will have been called async - we need to wait for group creation to finish
        verify(networkManager,timeout(5000)).createWifiDirectGroup();

        assertEquals("Should return the right response",
                WIFI_GROUP_CREATION_RESPONSE,responseMessage.getRequestType());
    }
}
