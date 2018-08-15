package com.ustadmobile.port.sharedse.networkmanager;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_REQUEST;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * <h1>BleMessageTest</h1>
 *
 * Test class which tests {@link BleMessage} to make sure it behaves as expected
 * when creating, processing and receiving packets.
 *
 * @author kileha3
 */

public class BleMessageTest {

    private List<Long> entriesListWithSufficientDuplicates = Arrays.asList(8888888888L,8888888888L,8888888888L,8888888888L,8888888888L);

    private List<Long> entriesListWithInsufficientDuplicates = Arrays.asList(1056289670L,4590875612L,9076137860L,2912543894L);


    @Test
    public void givenPayload_whenPacketizedAndDepacketized_shouldBeEqual() {
        byte[] payload = BleMessageUtil.bleMessageLongToBytes(entriesListWithSufficientDuplicates);

        BleMessage sentMessage = new BleMessage((byte)0, payload, 20);
        BleMessage receivedMessage = new BleMessage(sentMessage.getPackets());

        assertEquals("Messages have same request type", sentMessage.getRequestType(),
                receivedMessage.getRequestType());
        assertTrue("Payload depacketized is the same", Arrays.equals(payload,
                receivedMessage.getPayload()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenEmptyPayload_whenPacketized_shouldThrowIllegalArgumentException() {
        byte[] payload = "".getBytes();

        BleMessage sentMessage = new BleMessage((byte)0, payload, 20);
        new BleMessage(sentMessage.getPackets());
    }

    @Test
    public void givenMessageWithSufficientDuplicates_whenPacketized_thenShouldBeCompressed() {
        byte[] payload = BleMessageUtil.bleMessageLongToBytes(entriesListWithSufficientDuplicates);

        BleMessage sentMessage = new BleMessage((byte)0, payload, 20);
        BleMessage receivedMessage = new BleMessage(sentMessage.getPackets());

        assertTrue("Compressed payload is less compared to the original one",
                payload.length > receivedMessage.getLength());
    }

    @Test
    public void givenMessageWithInsufficientDuplicates_whenPacketized_thenShouldNotBeCompressed() {
        byte[] payload = BleMessageUtil.bleMessageLongToBytes(entriesListWithInsufficientDuplicates);

        BleMessage sentMessage = new BleMessage((byte)0, payload, 20);
        BleMessage receivedMessage = new BleMessage(sentMessage.getPackets());

        assertTrue("Uncompressed payload should have same length",
                payload.length == receivedMessage.getLength());
    }

    @Test
    public void givenPacketizedPayload_whenReceived_thenShouldBeReceivedAsSent(){
        byte[] payload = BleMessageUtil.bleMessageLongToBytes(entriesListWithInsufficientDuplicates);
        BleMessage messageToSend = new BleMessage(ENTRY_STATUS_REQUEST, payload, 20);
        BleMessage sentMessage = new BleMessage();

        for(int packetCounter = 0; packetCounter < messageToSend.getPackets().length;packetCounter++){
            byte [] packet = messageToSend.getPackets()[packetCounter];
            sentMessage.onPackageReceived(packet);
        }

        assertTrue("Packetized payload received as sent",
                Arrays.equals(payload, sentMessage.getPayload()));
    }

}
