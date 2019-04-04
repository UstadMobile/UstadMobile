package com.ustadmobile.port.sharedse.networkmanager;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.DEFAULT_MTU_SIZE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_REQUEST;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Test class which tests {@link BleMessage} to make sure it behaves as expected
 * when creating, processing and receiving packets.
 *
 * @author kileha3
 */

public class BleMessageTest {

    private List<Long> entriesListWithSufficientDuplicates =
            Arrays.asList(8888888888L,8888888888L,8888888888L,8888888888L,8888888888L);

    private List<Long> entriesListWithInsufficientDuplicates =
            Arrays.asList(1056289670L,4590875612L,9076137860L,2912543894L);

    private byte[] longerPayload = "The quick brown fox jumped over the lazy dog.".getBytes();

    @Test
    public void givenPayload_whenPacketizedAndDepacketized_shouldBeEqual() {
        byte[] payload = BleMessageUtil.bleMessageLongToBytes(entriesListWithSufficientDuplicates);

        BleMessage sentMessage = new BleMessage((byte)0, (byte)42, payload);
        BleMessage receivedMessage = new BleMessage(sentMessage.getPackets(DEFAULT_MTU_SIZE));

        assertEquals("Messages have same request type", sentMessage.getRequestType(),
                receivedMessage.getRequestType());
        assertTrue("Payload depacketized is the same", Arrays.equals(payload,
                receivedMessage.getPayload()));
        assertEquals("Same message id", sentMessage.getMessageId(), receivedMessage.getMessageId());
    }

    @Test
    public void givenSinglePacketPayload_whenPacketizedAndDepacketized_shouldBeEqual() {
        byte[] payload = new byte[]{1, 2, 3, 4, 5, 6};
        BleMessage sentMessage = new BleMessage((byte)0, (byte)42, payload);
        BleMessage receivedMessage = new BleMessage(sentMessage.getPackets(DEFAULT_MTU_SIZE));

        assertEquals("Messages have same request type", sentMessage.getRequestType(),
                receivedMessage.getRequestType());
        assertTrue("Payload depacketized is the same", Arrays.equals(payload,
                receivedMessage.getPayload()));
        assertEquals("Same message id", sentMessage.getMessageId(), receivedMessage.getMessageId());
    }

    @Test
    public void givenLongerPayload_whenPacketizedAndDepacketized_shouldBeEqual() {
        BleMessage sentMessage = new BleMessage((byte)0, (byte)42, longerPayload);
        BleMessage receivedMessage = new BleMessage(sentMessage.getPackets(DEFAULT_MTU_SIZE));

        assertEquals("Messages have same request type", sentMessage.getRequestType(),
                receivedMessage.getRequestType());
        assertTrue("Payload depacketized is the same", Arrays.equals(longerPayload,
                receivedMessage.getPayload()));
        assertEquals("Same message id", sentMessage.getMessageId(), receivedMessage.getMessageId());
    }



    @Test(expected = IllegalArgumentException.class)
    public void givenEmptyPayload_whenPacketized_shouldThrowIllegalArgumentException() {
        byte[] payload = "".getBytes();

        BleMessage sentMessage = new BleMessage((byte)0, (byte)42, payload);
        new BleMessage(sentMessage.getPackets(DEFAULT_MTU_SIZE));
    }

    @Test
    public void givenMessageWithSufficientDuplicates_whenPacketized_thenShouldBeCompressed() {
        byte[] payload = BleMessageUtil.bleMessageLongToBytes(entriesListWithSufficientDuplicates);

        BleMessage sentMessage = new BleMessage((byte)0, (byte)42,  payload);
        BleMessage receivedMessage = new BleMessage(sentMessage.getPackets(DEFAULT_MTU_SIZE));

        assertEquals("After depacketize the massage payload is the same after g-zipping",
                payload.length, receivedMessage.getPayload().length);
    }

    @Test
    public void givenMessageWithInsufficientDuplicates_whenPacketized_thenShouldNotBeCompressed() {
        byte[] payload = BleMessageUtil.bleMessageLongToBytes(entriesListWithInsufficientDuplicates);

        BleMessage sentMessage = new BleMessage((byte)0, (byte)42, payload);
        BleMessage receivedMessage = new BleMessage(sentMessage.getPackets(20));

       assertEquals("Uncompressed payload should have same length",
               payload.length, receivedMessage.getPayload().length);
    }

    @Test
    public void givenPacketizedPayload_whenReceived_thenShouldBeReceivedAsSent(){
        byte[] payload = BleMessageUtil.bleMessageLongToBytes(entriesListWithInsufficientDuplicates);
        BleMessage messageToSend = new BleMessage(ENTRY_STATUS_REQUEST, (byte)42,  payload);
        BleMessage sentMessage = new BleMessage();

        byte[][] packets = messageToSend.getPackets(DEFAULT_MTU_SIZE);

        for (byte[] packet : packets) {
            sentMessage.onPackageReceived(packet);
        }

        assertTrue("Packetized payload received as sent",
                Arrays.equals(payload, sentMessage.getPayload()));
    }

    @Test
    public void givenCreatedMessage_whenResetCalled_thenShouldResetTheMessage(){
        byte[] payload = BleMessageUtil.bleMessageLongToBytes(entriesListWithInsufficientDuplicates);
        BleMessage messageToSend = new BleMessage(ENTRY_STATUS_REQUEST, (byte)42, payload);

        assertTrue("Message was created and is not null",
                messageToSend.getPayload().length > 0);
        messageToSend.reset();

        assertEquals("Message was reset", 0,
                messageToSend.getPayload().length);
    }

}
