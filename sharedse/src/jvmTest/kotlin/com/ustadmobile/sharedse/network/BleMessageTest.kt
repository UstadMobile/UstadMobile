package com.ustadmobile.sharedse.network

import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.MINIMUM_MTU_SIZE
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.ENTRY_STATUS_REQUEST
import junit.framework.TestCase.assertTrue
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

/**
 * Test class which tests [BleMessage] to make sure it behaves as expected
 * when creating, processing and receiving packets.
 *
 * @author kileha3
 */

class BleMessageTest {

    private val entriesListWithSufficientDuplicates = Arrays.asList(8888888888L, 8888888888L, 8888888888L, 8888888888L, 8888888888L)

    private val entriesListWithInsufficientDuplicates = Arrays.asList(1056289670L, 4590875612L, 9076137860L, 2912543894L)

    private val longerPayload = "The quick brown fox jumped over the lazy dog.".toByteArray()

    @Test
    fun givenPayload_whenPacketizedAndDepacketized_shouldBeEqual() {
        val payload = BleMessageUtil.bleMessageLongToBytes(entriesListWithSufficientDuplicates)

        val sentMessage = BleMessage(0.toByte(), 42.toByte(), payload)
        val receivedMessage = BleMessage(sentMessage.getPackets(MINIMUM_MTU_SIZE))

        assertEquals("Messages have same request type", sentMessage.requestType.toLong(),
                receivedMessage.requestType.toLong())
        assertTrue("Payload depacketized is the same", Arrays.equals(payload,
                receivedMessage.payload))
        assertEquals("Same message id", sentMessage.messageId.toLong(), receivedMessage.messageId.toLong())
    }

    @Test
    fun givenLargeBinaryPayload_whenPacketizedAndDepacketized_shouldBeEqual() {
        val payload = javaClass.getResourceAsStream("/com/ustadmobile/port/sharedse/container/testfile2.png").readBytes()
        val sentMessage = BleMessage(0.toByte(), 42.toByte(), payload)
        val receivedMessage = BleMessage(sentMessage.getPackets(512))


        Assert.assertEquals("Sent message length is correct", payload.size,
                sentMessage.length)
        Assert.assertArrayEquals("message is the same after beign received", payload,
                receivedMessage.payload)
    }

    @Test
    fun givenSinglePacketPayload_whenPacketizedAndDepacketized_shouldBeEqual() {
        val payload = byteArrayOf(1, 2, 3, 4, 5, 6)
        val sentMessage = BleMessage(0.toByte(), 42.toByte(), payload)
        val receivedMessage = BleMessage(sentMessage.getPackets(MINIMUM_MTU_SIZE))

        assertEquals("Messages have same request type", sentMessage.requestType.toLong(),
                receivedMessage.requestType.toLong())
        assertTrue("Payload depacketized is the same", Arrays.equals(payload,
                receivedMessage.payload))
        assertEquals("Same message id", sentMessage.messageId.toLong(), receivedMessage.messageId.toLong())
    }

    @Test
    fun givenLongerPayload_whenPacketizedAndDepacketized_shouldBeEqual() {
        val sentMessage = BleMessage(0.toByte(), 42.toByte(), longerPayload)
        val receivedMessage = BleMessage(sentMessage.getPackets(MINIMUM_MTU_SIZE))

        assertEquals("Messages have same request type", sentMessage.requestType.toLong(),
                receivedMessage.requestType.toLong())
        assertTrue("Payload depacketized is the same", Arrays.equals(longerPayload,
                receivedMessage.payload))
        assertEquals("Same message id", sentMessage.messageId.toLong(), receivedMessage.messageId.toLong())
    }


    @Test(expected = IllegalArgumentException::class)
    fun givenEmptyPayload_whenPacketized_shouldThrowIllegalArgumentException() {
        val payload = "".toByteArray()

        val sentMessage = BleMessage(0.toByte(), 42.toByte(), payload)
        BleMessage(sentMessage.getPackets(MINIMUM_MTU_SIZE))
    }

    @Test
    fun givenMessageWithSufficientDuplicates_whenPacketized_thenShouldBeCompressed() {
        val payload = BleMessageUtil.bleMessageLongToBytes(entriesListWithSufficientDuplicates)

        val sentMessage = BleMessage(0.toByte(), 42.toByte(), payload)
        val receivedMessage = BleMessage(sentMessage.getPackets(MINIMUM_MTU_SIZE))

        assertEquals("After depacketize the massage payload is the same after g-zipping",
                payload.size.toLong(), receivedMessage.payload!!.size.toLong())
    }

    @Test
    fun givenMessageWithInsufficientDuplicates_whenPacketized_thenShouldNotBeCompressed() {
        val payload = BleMessageUtil.bleMessageLongToBytes(entriesListWithInsufficientDuplicates)

        val sentMessage = BleMessage(0.toByte(), 42.toByte(), payload)
        val receivedMessage = BleMessage(sentMessage.getPackets(20))

        assertEquals("Uncompressed payload should have same length",
                payload.size.toLong(), receivedMessage.payload!!.size.toLong())
    }

    @Test
    fun givenPacketizedPayload_whenReceived_thenShouldBeReceivedAsSent() {
        val payload = BleMessageUtil.bleMessageLongToBytes(entriesListWithInsufficientDuplicates)
        val messageToSend = BleMessage(ENTRY_STATUS_REQUEST, 42.toByte(), payload)
        val sentMessage = BleMessage()

        val packets = messageToSend.getPackets(MINIMUM_MTU_SIZE)

        for (packet in packets) {
            sentMessage.onPackageReceived(packet)
        }

        assertTrue("Packetized payload received as sent",
                Arrays.equals(payload, sentMessage.payload))
    }

    @Test
    fun givenCreatedMessage_whenResetCalled_thenShouldResetTheMessage() {
        val payload = BleMessageUtil.bleMessageLongToBytes(entriesListWithInsufficientDuplicates)
        val messageToSend = BleMessage(ENTRY_STATUS_REQUEST, 42.toByte(), payload)

        assertTrue("Message was created and is not null",
                messageToSend.payload!!.size > 0)
        messageToSend.reset()

        assertEquals("Message was reset", 0,
                messageToSend.payload!!.size.toLong())
    }

}
