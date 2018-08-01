package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.port.sharedse.networkmanager.BleMessage;

import org.junit.Test;

import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * <h1>BleMessageTests</h1>
 *
 * Bluetooth Low Energy Test Class tests {@link BleMessage} to make sure it behaves as expected.
 *
 * @author kileha3
 */

public class BleMessageTests {

    private String messageWithSufficientDuplicates = "dce655f2-34f0-469c-b890-a910039b0afc,c9d07319-2ab0-4a53-82cb-02370f5b8699,f912c86a-7f3b-406b-aef9-816e47bc00c0," +
            "dce655f2-34f0-469c-b890-a910039b0afc,c9d07319-2ab0-4a53-82cb-02370f5b8699,f912c86a-7f3b-406b-aef9-816e47bc00c0";

    private String messageWithInsufficientDuplicates = "dce655f2-34f0-469c-b890-a910039b0afc,c9d07319-2ab0-4a53-82cb-02370f5b8699";


    @Test
    public void givenPayload_whenPacketizedAndDepacketized_shouldBeEqual() {
        byte[] payload = messageWithSufficientDuplicates.getBytes();

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
        byte[] payload = messageWithSufficientDuplicates.getBytes();

        BleMessage sentMessage = new BleMessage((byte)0, payload, 20);
        BleMessage receivedMessage = new BleMessage(sentMessage.getPackets());
        assertTrue("Compressed payload is less compared to the original one",
                payload.length > receivedMessage.getLength());
    }

    @Test
    public void givenMessageWithInsufficientDuplicates_whenPacketized_thenShouldNotBeCompressed() {
        byte[] payload = messageWithInsufficientDuplicates.getBytes();

        BleMessage sentMessage = new BleMessage((byte)0, payload, 20);
        BleMessage receivedMessage = new BleMessage(sentMessage.getPackets());

        assertTrue("Uncompressed payload should have same length",
                payload.length == receivedMessage.getLength());
    }

}
