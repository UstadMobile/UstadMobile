package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.util.UMIOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_REQUEST;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_RESPONSE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_REQUEST;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_CREATION_RESPONSE;

/**
 * Bluetooth Low Energy can use a packet size between 20 and 512 bytes. BleMessage will packetize
 * a payload accordingly, and use Gzip compression if that reduces the size. Messages are delivered
 * as follows:
 * <p>
 * Byte 0: Request code (8 bit integer value between 0 and 255)
 * Byte 1: Maximum Transfer Unit
 * Byte 2-6: payload length
 * Byte 7-onwards: Payload
 *
 * @author kileha3
 */
public class BleMessage {

    private byte[] payload;

    private byte requestType;

    private int mtu;

    private int length;

    private ByteArrayOutputStream outputStream;


    /**
     * Constructor which will be used when receiving packets
     */
    public BleMessage(){ }


    /**
     * Constructor which will be used when sending the message
     * @param requestType Type of the request that will be contained.
     * @param payload The actual payload to be sent
     */
    public BleMessage(byte requestType, byte[] payload){
        this.requestType = requestType;
        this.payload = payload;
        this.length = payload.length;
    }


    /**
     * Constructor which will be used when receiving the message
     * @param payload Received packets
     */
    public BleMessage(byte[][] payload){
        byte [] packets = depacketizePayload(payload);
        byte [] receivedPayload = ByteBuffer.wrap(Arrays.copyOfRange(packets, 6,
                packets.length)).array();
        requestType = ByteBuffer.wrap(Arrays.copyOfRange(packets, 0, 1)).get();
        mtu = ByteBuffer.wrap(Arrays.copyOfRange(packets, 1, 2)).get();
        length = ByteBuffer.wrap(Arrays.copyOfRange(packets, 2, 6)).getInt();

        boolean isCompressed = receivedPayload.length > 0 &&
                (receivedPayload[0] == (byte) (GZIPInputStream.GZIP_MAGIC))
                && (receivedPayload[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
        this.payload = receivedPayload;
        if(isCompressed){
            this.payload = decompressPayload(receivedPayload);
        }
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
     * @return Compressed payload in byte array.
     */
    private byte[] compressPayload(byte[] payload){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gos = null;
        try{
            gos = new GZIPOutputStream(bos);
            gos.write(payload);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            UMIOUtils.closeQuietly(gos);
        }
        return bos.toByteArray();
    }

    /**
     * Internal method for decompressing compressed payload
     * @param receivedPayload Compressed payload
     * @return Decompressed payload in byte array.
     */
    private byte[] decompressPayload(byte[] receivedPayload){
        final int BUFFER_SIZE = 32;
        ByteArrayInputStream is = new ByteArrayInputStream(receivedPayload);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try{
            GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
            byte[] data = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = gis.read(data)) != -1) {
                bout.write(data, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            UMIOUtils.closeQuietly(bout);
        }
        return bout.toByteArray();
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
            int packetSize = (int) Math.ceil(payload.length / (double) mtu);
            ByteBuffer headerBuffer = ByteBuffer.allocate(6);
            byte[] header = headerBuffer.put(requestType).put((byte) mtu)
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
            byte [] totalPayLoad = outputStream.toByteArray();
            byte[][] packets = new byte[packetSize][mtu];
            int start = 0;
            for(int position = 0; position < packets.length; position++) {
                int end = start + mtu;
                if(end > totalPayLoad.length){end = totalPayLoad.length;}
                packets[position] = Arrays.copyOfRange(totalPayLoad,start, end);
                start += mtu;
            }
            return packets;
        }
    }


    /**
     * Internal method for reconstructing the payload from packets.
     * @param packets Payload packets to be depacketized
     * @return Constructed payload in byte array
     */
    private byte[] depacketizePayload(byte[][] packets){
        if(packets.length == 0){
            throw new NullPointerException();
        }else{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            for(byte [] payLoad : packets){
                try {
                    outputStream.write(payLoad);
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
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
     * @param packets packet received from the other peer
     * @return True if the packets are all received else False.
     */
    public boolean onPackageReceived(byte [] packets){
        byte mRequestType = ByteBuffer.wrap(Arrays.copyOfRange(packets, 0, 1)).get();
        if(outputStream == null && isValidRequestType(mRequestType)){
            outputStream = new ByteArrayOutputStream();
            mtu = ByteBuffer.wrap(Arrays.copyOfRange(packets, 1, 2)).get();
            length = ByteBuffer.wrap(Arrays.copyOfRange(packets, 2, 6)).getInt();
            requestType = mRequestType;
        }

        if(outputStream != null){
            try{
                outputStream.write(packets);
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte [] receivedPayload = ByteBuffer.wrap(Arrays.copyOfRange(outputStream.toByteArray(), 6,
                    outputStream.toByteArray().length)).array();

            if(receivedPayload.length == length){
                try{
                    boolean isCompressed = receivedPayload.length > 0 &&
                            (receivedPayload[0] == (byte) (GZIPInputStream.GZIP_MAGIC))
                            && (receivedPayload[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
                    this.payload = receivedPayload;
                    if(isCompressed){
                        this.payload = decompressPayload(receivedPayload);
                    }
                    outputStream.close();
                }catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    UMIOUtils.closeQuietly(outputStream);
                    outputStream = null;
                }
                return true;
            }
        }
        return false;
    }


    /**
     * Check if received request type is one of the pre defined request types.
     */
    private boolean isValidRequestType(byte requestType){
        return ENTRY_STATUS_REQUEST == requestType || ENTRY_STATUS_RESPONSE == requestType ||
                WIFI_GROUP_REQUEST == requestType ||
                WIFI_GROUP_CREATION_RESPONSE == requestType;
    }

}
