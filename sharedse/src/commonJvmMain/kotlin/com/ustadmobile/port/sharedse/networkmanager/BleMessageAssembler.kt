package com.ustadmobile.port.sharedse.networkmanager

import java.util.*

/**
 * BleMessageAssembler
 *
 */
class BleMessageAssembler {

    private val clientAddrToMessagesMap: MutableMap<String, Map<Byte, BleMessageInProgress>>

    private class BleMessageInProgress {

        val message: BleMessage

        @Volatile
        private var lastUpdated: Long = 0

        init {
            message = BleMessage()
        }

        fun onPacketReceived(packet: ByteArray): Boolean {
            lastUpdated = System.currentTimeMillis()
            return message.onPackageReceived(packet)
        }
    }

    init {
        clientAddrToMessagesMap = mutableMapOf()
    }

    @Synchronized
    fun handleIncomingPacket(senderAddr: String, packet: ByteArray): BleMessage? {
        var clientMessageIdToMessageMap: MutableMap<Byte, BleMessageInProgress>? = clientAddrToMessagesMap[senderAddr] as MutableMap<Byte, BleMessageInProgress>?
        if (clientMessageIdToMessageMap == null) {
            clientMessageIdToMessageMap = Hashtable()
            clientAddrToMessagesMap[senderAddr] = clientMessageIdToMessageMap
        }

        val messageId = BleMessage.findMessageId(packet)
        var messageInProgress: BleMessageInProgress? = clientMessageIdToMessageMap[messageId]
        if (messageInProgress == null) {
            messageInProgress = BleMessageInProgress()
            clientMessageIdToMessageMap[messageId] = messageInProgress
        }

        val complete = messageInProgress.onPacketReceived(packet)
        if (complete) {
            clientMessageIdToMessageMap.remove(messageId)
            return messageInProgress.message
        } else {
            return null
        }
    }


}
