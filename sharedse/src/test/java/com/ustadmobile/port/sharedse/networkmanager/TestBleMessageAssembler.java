package com.ustadmobile.port.sharedse.networkmanager;

import org.junit.Assert;
import org.junit.Test;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestBleMessageAssembler {

    private byte[][] messageBytes;

    private BleMessage[] messages;

    String fakeServerAddr = "aa";

    private int mtu = 20;

    public void setupMessages(int numMessages, int minMessageLength, int maxMessageLength) {
        messageBytes = new byte[numMessages][];
        messages = new BleMessage[numMessages];
        for(int i = 0; i < numMessages; i++) {
            int messageLength = (int)(Math.random() * (maxMessageLength - minMessageLength))
                    + minMessageLength;
            messageBytes[i] = new byte[messageLength];
            for(int j = 0; j < messageLength; j++) {
                messageBytes[i][j] = (byte)((Math.random() * 256) - 128);
            }

            messages[i] = new BleMessage(NetworkManagerBle.ENTRY_STATUS_REQUEST,
                    BleMessage.getNextMessageIdForReceiver(fakeServerAddr), messageBytes[i]);
        }
    }

    @Test
    public void givenMultipleConnections_whenPacketsSentConcurrently_packetsReassembledCorrectly()
            throws InterruptedException{
        int numMessages = 5;
        setupMessages(numMessages, 10, 40);

        ExecutorService executor = Executors.newFixedThreadPool(5);

        BleMessageAssembler assembler = new BleMessageAssembler();
        final Map<Integer, BleMessage> completedMessageMap = new Hashtable<>();
        for(int i = 0; i < numMessages; i++) {
            final int messageNum = i;
            executor.execute(() -> {
                byte[][] packets = messages[messageNum].getPackets(mtu);
                for(int j = 0; j < packets.length; j++) {
                    BleMessage message = assembler.handleIncomingPacket(fakeServerAddr, packets[j]);
                    if(message != null) {
                        completedMessageMap.put(messageNum, message);
                        break;
                    }
                    try { Thread.sleep((int)(Math.random() * 100)); }
                    catch(InterruptedException e) {}
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(60000, TimeUnit.MILLISECONDS);

        for(int i = 0; i < numMessages; i++) {
            Assert.assertArrayEquals(messages[i].getPayload(),
                    completedMessageMap.get(i).getPayload());
        }
    }

}
