package com.ustadmobile.sharedse.network

import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlin.jvm.Synchronized
import kotlin.jvm.Volatile

/**
 * BleMessageAssembler
 *
 */
class BleMessageAssembler {

    private val clientAddrToMessagesMap: MutableMap<String, Map<Byte, BleMessageInProgress>> = mutableMapOf()

    private class BleMessageInProgress {

        val message: BleMessage = BleMessage()

        @Volatile
        private var lastUpdated: Long = 0

        fun onPacketReceived(packet: ByteArray): Boolean {
            lastUpdated = getSystemTimeInMillis()
            return message.onPackageReceived(packet)
        }
    }

    @Synchronized
    fun handleIncomingPacket(senderAddr: String, packet: ByteArray): BleMessage? {
        var clientMessageIdToMessageMap: MutableMap<Byte, BleMessageInProgress>? = clientAddrToMessagesMap[senderAddr] as MutableMap<Byte, BleMessageInProgress>?
        if (clientMessageIdToMessageMap == null) {
            clientMessageIdToMessageMap = mutableMapOf()
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
