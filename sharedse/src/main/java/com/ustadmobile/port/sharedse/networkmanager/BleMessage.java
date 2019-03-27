package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.util.UMIOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.DEFAULT_MTU_SIZE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_REQUEST;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_RESPONSE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_CREATION_RESPONSE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_REQUEST;

/**
 * This class converts bytes
 *
 * <p>
 * <p>
 * Use {@link BleMessage#BleMessage(byte[][])} to receive message
 * sent by peer device.
 * <p>
 * Use {@link BleMessage#getPackets(int)} to get the packets to be sent
 * to the peer device.
 * <p>
 * Bluetooth Low Energy can use a packet size between 20 and 512 bytes. BleMessage will packetize
 * a payload accordingly, and use Gzip compression if that reduces the size.
 * <p>
 * <b>Packet Structure</b>
 * <p>
 * Byte 0: Request code (8 bit integer value between 0 and 255)
 * Byte 1-3: Maximum Transfer Unit (MTU)
 * Byte 4-7: Payload length
 * Byte 8-onwards: Payload
 *<p>
 * Use {@link BleMessage#getPayload()} to get the actual payload sent from the peer device
 * <p>
 * Use {@link BleMessage#getLength()} to get the payload length
 * <p>
 * Use {@link BleMessage#getRequestType()} to get request type
 * <p>
 * Use {@link BleMessage#getMtu()} to get the MTU used to send th packets
 *
 * @author kileha3
 */
public class BleMessage {

    private byte[] payload;

    private byte requestType;

    private int mtu;

    private int length;

    private byte messageId;

    private static final int payloadLengthStartIndex = 3;

    private static final int payLoadStartIndex = 7;

    public static final int HEADER_SIZE = 1 + 2 + 4;//Request type (byte - 1byte), mtu (short - 2 byts), length (int - 4bytes)


    private byte[][] packetReceiveBuffer;

    private int onPacketReceivedCount = 0;

    private static Map<String, Byte> messageIds = new Hashtable<>();

    public static byte findMessageId(byte[] packet) {
        return packet[0];
    }

    public static byte getNextMessageIdForReceiver(String receiverAddr) {
        Byte lastMessageId = messageIds.get(receiverAddr);
        byte nextMessageId;
        if(lastMessageId == null) {
            nextMessageId = (byte)-128;
        }else if(lastMessageId == 127){
            nextMessageId = (byte)-128;
        }else {
            nextMessageId = (byte)(lastMessageId + 1);
        }

        messageIds.put(receiverAddr, nextMessageId);

        return nextMessageId;
    }





    /**
     * Constructor which will be used when receiving packets
     */
    public BleMessage(){ }


    /**
     * Constructor which will be used when sending the message
     * @param requestType Type of the request that will be contained.
     * @param payload The actual payload to be sent
     */
    public BleMessage(byte requestType, byte messageId, byte[] payload){
        this.requestType = requestType;
        this.payload = payload;
        this.length = payload.length;
        this.messageId = messageId;
    }


    /**
     * Constructor which will be used when receiving the message
     * @param packetsReceived Received packets
     */
    public BleMessage(byte[][] packetsReceived){
        constructFromPackets(packetsReceived);
    }

    private void constructFromPackets(byte[][] packetsReceived) {
        messageId = packetsReceived[0][0];
        byte[] messageBytes = depacketizePayload(packetsReceived);
        assignHeaderValuesFromFirstPacket(packetsReceived[0]);

        byte[] receivedPayload = new byte[length];
        System.arraycopy(messageBytes, HEADER_SIZE, receivedPayload, 0, length);

        boolean isCompressed = receivedPayload.length > 0 &&
                (receivedPayload[0] == (byte) (GZIPInputStream.GZIP_MAGIC))
                && (receivedPayload[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
        this.payload = receivedPayload;
        if(isCompressed){
            this.payload = decompressPayload(receivedPayload);
        }
    }

    private void assignHeaderValuesFromFirstPacket(byte[] packet) {
        messageId = packet[0];
        requestType = packet[1];
        mtu = ByteBuffer.wrap(new byte[]{packet[2], packet[3]}).getShort();
        length = ByteBuffer.wrap(Arrays.copyOfRange(packet, payloadLengthStartIndex + 1,
                payLoadStartIndex + 1)).getInt();
    }


    /**
     * Get constructed payload packets to be transferred
     * @param mtu Packets maximum transfer unit
     * @return Constructed packets in byte arrays
     */
    public byte[][] getPackets(int mtu) {
        this.mtu = mtu;
        byte[] compressedPayload = compressPayload(this.payload);

        if(compressedPayload.length < this.payload.length) {
            return packetizePayload(compressedPayload);
        }else {
            return packetizePayload(this.payload);
        }

    }

    /**
     * Get received message payload
     * @return Message payload in byte array.
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * Internal message to compress message payload
     * @param payload payload to be compressed
     * @return Compressed payload in byte array, null if something goes wrong
     */
    private byte[] compressPayload(byte[] payload){
        try (
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream gos = new GZIPOutputStream(bos)
        ){
            gos.write(payload);
            gos.flush();
            gos.close();
            return bos.toByteArray();
        }catch (IOException e) {
            //Very unlikely as we are reading from memory into memory
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Internal method for decompressing compressed payload
     * @param receivedPayload Compressed payload
     * @return Decompressed payload in byte array.
     */
    private byte[] decompressPayload(byte[] receivedPayload){
        final int BUFFER_SIZE = Math.min(32, receivedPayload.length);
        ByteArrayInputStream is = new ByteArrayInputStream(receivedPayload);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try{
            GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
            byte[] data = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = gis.read(data)) != -1) {
                bout.write(data, 0, bytesRead);
            }
            bout.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            UMIOUtils.closeQuietly(bout);
        }
        return bout.toByteArray();
    }



    private int calculateNumPackets(int payloadLength, int mtu) {
        return (int) Math.ceil((payloadLength + HEADER_SIZE) / (double)(mtu - 1));
    }

    /**
     * Internal method for constructing payload packets from the message payload
     * @param payload Message payload to be packetized
     * @return Constructed payload packets in byte arrays
     */
    private byte[][] packetizePayload(byte [] payload){
        if(payload.length == 0){
            throw new IllegalArgumentException();
        }else{
            int numPackets = calculateNumPackets(payload.length, mtu);
            ByteBuffer headerBuffer = ByteBuffer.allocate(HEADER_SIZE);
            byte[] header = headerBuffer
                    .put(requestType)
                    .putShort((short) mtu)
                    .putInt(payload.length).array();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                outputStream.write(header);
                outputStream.write(payload);
            } catch (IOException e) {
                e.printStackTrace();
                return new byte[][]{};
            }finally {
                UMIOUtils.closeQuietly(outputStream);
            }
            byte[] totalPayLoad = outputStream.toByteArray();
            byte[][] packets = new byte[numPackets][mtu];
            for(int i = 0; i < packets.length; i++) {
                packets[i][0] = messageId;
                int payloadPos = (i * (mtu-1));
                System.arraycopy(totalPayLoad, payloadPos, packets[i], 1,
                        Math.min(mtu - 1, totalPayLoad.length - payloadPos));
            }

            return packets;
        }
    }

    /**
     * Internal method for reconstructing the payload from packets. Strips out the mesage id
     *
     * @param packets Payload packets to be depacketized
     *
     * @return Constructed payload in byte array
     */
    //TODO: Throw an exception if any packet has a different message id
    private byte[] depacketizePayload(byte[][] packets){
        if(packets.length == 0){
            throw new NullPointerException();
        }else {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            for(byte [] packetContent : packets){
                try {
                    outputStream.write(packetContent, 1, packetContent.length - 1);
                } finally {
                    UMIOUtils.closeQuietly(outputStream);
                }
            }
            return outputStream.toByteArray();
        }
    }

    /**
     * Get request type from the received message payload.
     * @return Request status as byte
     */
    public byte getRequestType(){
        return requestType;
    }

    /**
     * Get received length from message payload
     * @return Length in integer
     */
    public int getLength(){
        return length;
    }

    /**
     * Get Maximum Transfer Unit from message packets
     * @return MTU value
     */
    public int getMtu(){
        return mtu;
    }



    /**
     * Called when packet is received from the other peer for assembling
     * @param packet packet received from the other peer
     * @return True if the packets are all received else False.
     */
    public boolean onPackageReceived(byte [] packet){
        if(onPacketReceivedCount == 0){
            assignHeaderValuesFromFirstPacket(packet);
            packetReceiveBuffer = new byte[calculateNumPackets(length, mtu)][mtu];
        }

        if(onPacketReceivedCount < packetReceiveBuffer.length) {
            packetReceiveBuffer[onPacketReceivedCount++] = packet;
        }

        if(onPacketReceivedCount == packetReceiveBuffer.length) {
            constructFromPackets(packetReceiveBuffer);
            return true;
        }else {
            return false;
        }
    }


    /**
     * Reset a message
     */
    public void reset(){
        requestType = 0;
        length = 0;
        mtu = DEFAULT_MTU_SIZE;
        payload = new byte[]{};
    }


    public byte getMessageId() {
        return messageId;
    }
}
