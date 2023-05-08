package com.ustadmobile.sharedse.network

import com.ustadmobile.sharedse.ext.compressWithGzip
import com.ustadmobile.sharedse.ext.decompressWithGzip
import com.ustadmobile.sharedse.io.ByteBufferSe
import com.ustadmobile.sharedse.io.GzInputStreamConstants.GZIP_MAGIC
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.MINIMUM_MTU_SIZE
import kotlin.jvm.Synchronized
import kotlin.math.ceil
import kotlin.math.min

/**
 * This class converts bytes
 *
 *
 *
 *
 *
 * Use [BleMessage.BleMessage] to receive message
 * sent by peer device.
 *
 *
 * Use [BleMessage.getPackets] to get the packets to be sent
 * to the peer device.
 *
 *
 * Bluetooth Low Energy can use a packet size between 20 and 512 bytes. BleMessage will packetize
 * a payload accordingly, and use Gzip compression if that reduces the size.
 *
 *
 * **Packet Structure**
 *
 *
 * Byte 0: Request code (8 bit integer value between 0 and 255)
 * Byte 1-3: Maximum Transfer Unit (MTU)
 * Byte 4-7: Payload length
 * Byte 8-onwards: Payload
 *
 *
 * Use [BleMessage.getPayload] to get the actual payload sent from the peer device
 *
 *
 * Use [BleMessage.getLength] to get the payload length
 *
 *
 * Use [BleMessage.getRequestType] to get request type
 *
 *
 * Use [BleMessage.getMtu] to get the MTU used to send th packets
 *
 * @author kileha3
 */
class BleMessage {

    /**
     * Get received message payload
     * @return Message payload in byte array.
     */
    var payload: ByteArray? = null
        private set

    /**
     * Get request type from the received message payload.
     * @return Request status as byte
     */
    var requestType: Byte = 0
        private set

    /**
     * Get Maximum Transfer Unit from message packets
     * @return MTU value
     */
    var mtu: Int = 0
        private set

    /**
     * Get received length from message payload
     * @return Length in integer
     */
    var length: Int = 0
        private set

    /**
     *
     * @return unique identifier of the message under process.
     */
    var messageId: Byte = 0
        private set


    private var packetReceiveBuffer: Array<ByteArray>? = null

    private var onPacketReceivedCount = 0


    /**
     * Constructor which will be used when receiving packets
     */
    constructor() {}


    /**
     * Constructor which will be used when sending the message
     * @param requestType Type of the request that will be contained.
     * @param payload The actual payload to be sent
     */
    constructor(requestType: Byte, messageId: Byte, payload: ByteArray) {
        this.requestType = requestType
        this.payload = payload
        this.length = payload.size
        this.messageId = messageId
    }


    /**
     * Constructor which will be used when receiving the message
     * @param packetsReceived Received packets
     */
    constructor(packetsReceived: Array<ByteArray>) {
        constructFromPackets(packetsReceived)
    }

    private fun constructFromPackets(packetsReceived: Array<ByteArray>) {
        messageId = packetsReceived[0][0]
        val messageBytes = depacketizePayload(packetsReceived)
        assignHeaderValuesFromFirstPacket(packetsReceived[0])

        val receivedPayload = ByteArray(length)
        messageBytes.copyInto(receivedPayload, 0, HEADER_SIZE, HEADER_SIZE + length)

        val isCompressed = (receivedPayload.isNotEmpty() &&
                receivedPayload[0] == GZIP_MAGIC.toByte()
                && receivedPayload[1] == (GZIP_MAGIC shr 8).toByte())
        this.payload = receivedPayload
        if (isCompressed) {
            this.payload = receivedPayload.decompressWithGzip()
        }
    }

    private fun assignHeaderValuesFromFirstPacket(packet: ByteArray) {
        messageId = packet[0]
        requestType = packet[1]


        mtu = ByteBufferSe.wrap(byteArrayOf(packet[2], packet[3])).getShort().toInt()
        // length = ByteBuffer.wrap(Array.copyOfRange(packet, payloadLengthStartIndex + 1,
        //          payLoadStartIndex + 1)).int
        length = ByteBufferSe.wrap(packet.copyOfRange(payloadLengthStartIndex + 1, payLoadStartIndex + 1)).getInt()

    }


    /**
     * Get constructed payload packets to be transferred
     * @param mtu Packets maximum transfer unit
     * @return Constructed packets in byte arrays
     */
    fun getPackets(mtu: Int): Array<ByteArray> {
        this.mtu = mtu
        val compressedPayload = this.payload?.compressWithGzip()//  compressPayload(this.payload)

        return if (compressedPayload!!.size < this.payload!!.size) {
            packetizePayload(compressedPayload)
        } else {
            packetizePayload(this.payload!!)
        }

    }

    private fun calculateNumPackets(payloadLength: Int, mtu: Int): Int {
        return ceil((payloadLength + HEADER_SIZE) / (mtu - 1).toDouble()).toInt()
    }

    /**
     * Internal method for constructing payload packets from the message payload
     * @param payload Message payload to be packetized
     * @return Constructed payload packets in byte arrays
     */
    private fun packetizePayload(payload: ByteArray): Array<ByteArray> {
        if (payload.isEmpty()) {
            throw IllegalArgumentException()
        } else {
            val numPackets = calculateNumPackets(payload.size, mtu)
            val headerBuffer = ByteBufferSe.allocate(HEADER_SIZE)
            val header = headerBuffer
                    .put(requestType)
                    .putShort(mtu.toShort())
                    .putInt(payload.size).array()
            val totalPayLoad = header + payload
            val packets = Array(numPackets) { ByteArray(mtu) }
            for (i in packets.indices) {
                packets[i][0] = messageId
                val payloadPos = i * (mtu - 1)
                totalPayLoad.copyInto(packets[i], 1, payloadPos, payloadPos + min(mtu - 1, totalPayLoad.size - payloadPos))
            }

            return packets
        }
    }

    /**
     * Internal method for reconstructing the payload from packets. Checks that the message id is
     * as expected, and then strips it out.
     *
     * @param packets Payload packets to be depacketized
     *
     * @return Constructed payload in byte array
     */
    private fun depacketizePayload(packets: Array<ByteArray>): ByteArray {
        return packets.fold(ByteArray(0)) { acc: ByteArray, bytes: ByteArray ->
            val packetMessageId = bytes[0]
            if(packetMessageId != messageId)
                throw IllegalArgumentException("Packet message id error: expected $messageId / received $packetMessageId")

            acc + bytes.copyOfRange(1, bytes.size)
        }
    }


    /**
     * Called when packet is received from the other peer for assembling
     * @param packet packet received from the other peer
     * @return True if the packets are all received else False.
     */
    fun onPackageReceived(packet: ByteArray): Boolean {
        if (onPacketReceivedCount == 0) {
            assignHeaderValuesFromFirstPacket(packet)
            packetReceiveBuffer = Array(calculateNumPackets(length, mtu)) { ByteArray(mtu) }
        }

        if (onPacketReceivedCount < packetReceiveBuffer!!.size) {
            packetReceiveBuffer!![onPacketReceivedCount++] = packet
        }

        if (onPacketReceivedCount == packetReceiveBuffer!!.size) {
            constructFromPackets(packetReceiveBuffer!!)
            return true
        } else {
            return false
        }
    }


    /**
     * Reset a message
     */
    fun reset() {
        requestType = 0
        length = 0
        mtu = MINIMUM_MTU_SIZE
        payload = byteArrayOf()
    }

    companion object {

        const val MESSAGE_TYPE_HTTP = 101.toByte()

        private const val payloadLengthStartIndex = 3

        private const val payLoadStartIndex = 7

        const val HEADER_SIZE = 1 + 2 + 4//Request type (byte - 1byte), mtu (short - 2 byts), length (int - 4bytes)

        private val messageIds = mutableMapOf<String, Byte>()

        /**
         * Find message ID from received packets
         * @param packet received packets
         * @return message identifier
         */
        fun findMessageId(packet: ByteArray): Byte {
            return packet[0]
        }

        /**
         * Generate next message identifier for the node
         * @param receiverAddr bluetooth address
         * @return unique message identifier
         */
        @Synchronized
        fun getNextMessageIdForReceiver(receiverAddr: String): Byte {
            val lastMessageId = messageIds[receiverAddr] ?: (-128).toByte()
            val nextMessageId: Byte
            if (lastMessageId == 127.toByte()) {
                nextMessageId = (-128).toByte()
            } else {
                nextMessageId = (lastMessageId + 1).toByte()
            }

            messageIds[receiverAddr] = nextMessageId

            return nextMessageId
        }

    }
}
