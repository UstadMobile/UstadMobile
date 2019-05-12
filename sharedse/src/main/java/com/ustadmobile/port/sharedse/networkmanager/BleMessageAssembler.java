package com.ustadmobile.port.sharedse.networkmanager;

import java.util.Hashtable;
import java.util.Map;

/**
 * BleMessageAssembler
 *
 */
public class BleMessageAssembler {

    private static class BleMessageInProgress {

        private BleMessage message;

        private volatile long lastUpdated;

        public BleMessageInProgress() {
            message = new BleMessage();
        }

        public boolean onPacketReceived(byte[] packet) {
            lastUpdated = System.currentTimeMillis();
            return message.onPackageReceived(packet);
        }

        public BleMessage getMessage() {
            return message;
        }
    }

    private Map<String, Map<Byte, BleMessageInProgress>> clientAddrToMessagesMap;

    public BleMessageAssembler() {
        clientAddrToMessagesMap = new Hashtable<>();
    }

    public synchronized BleMessage handleIncomingPacket(String senderAddr, byte[] packet) {
        Map<Byte, BleMessageInProgress> clientMessageIdToMessageMap = clientAddrToMessagesMap
                .get(senderAddr);
        if(clientMessageIdToMessageMap == null) {
            clientMessageIdToMessageMap = new Hashtable<>();
            clientAddrToMessagesMap.put(senderAddr, clientMessageIdToMessageMap);
        }

        byte messageId = BleMessage.findMessageId(packet);
        BleMessageInProgress messageInProgress = clientMessageIdToMessageMap.get(messageId);
        if(messageInProgress == null) {
            messageInProgress = new BleMessageInProgress();
            clientMessageIdToMessageMap.put(messageId, messageInProgress);
        }

        boolean complete = messageInProgress.onPacketReceived(packet);
        if(complete) {
            clientMessageIdToMessageMap.remove(messageId);
            return messageInProgress.getMessage();
        }else {
            return null;
        }
    }


}
