package com.ustadmobile.sharedse.network

import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.ENTRY_STATUS_REQUEST
import org.junit.Assert
import org.junit.Test
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TestBleMessageAssembler {

    private lateinit var messages: Array<BleMessage?>

    internal var fakeServerAddr = "aa"

    private val mtu = 20

    fun setupMessages(numMessages: Int, minMessageLength: Int, maxMessageLength: Int) {
        var messageBytes = Array(numMessages) { ByteArray(numMessages) }
        messages = arrayOfNulls(numMessages)
        for (i in 0 until numMessages) {
            val messageLength = (Math.random() * (maxMessageLength - minMessageLength)).toInt() + minMessageLength
            messageBytes[i] = ByteArray(messageLength)
            for (j in 0 until messageLength) {
                messageBytes[i][j] = (Math.random() * 256 - 128).toInt().toByte()
            }

            messages[i] = BleMessage(ENTRY_STATUS_REQUEST,
                    BleMessage.getNextMessageIdForReceiver(fakeServerAddr), messageBytes[i])
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenMultipleConnections_whenPacketsSentConcurrently_packetsReassembledCorrectly() {
        val numMessages = 5
        setupMessages(numMessages, 10, 40)

        val executor = Executors.newFixedThreadPool(5)

        val assembler = BleMessageAssembler()
        val completedMessageMap = Hashtable<Int, BleMessage>()
        for (i in 0 until numMessages) {
            executor.execute {
                val packets = messages[i]!!.getPackets(mtu)
                for (j in packets.indices) {
                    val message = assembler.handleIncomingPacket(fakeServerAddr, packets[j])
                    if (message != null) {
                        completedMessageMap[i] = message
                        break
                    }
                    try {
                        Thread.sleep((Math.random() * 100).toInt().toLong())
                    } catch (e: InterruptedException) {
                    }

                }
            }
        }

        executor.shutdown()
        executor.awaitTermination(60000, TimeUnit.MILLISECONDS)

        for (i in 0 until numMessages) {
            Assert.assertArrayEquals(messages[i]!!.payload,
                    completedMessageMap[i]!!.payload)
        }
    }

}
