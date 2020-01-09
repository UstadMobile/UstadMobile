package com.ustadmobile.sharedse.network

import android.bluetooth.*
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import com.github.aakira.napier.Napier
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.port.sharedse.impl.http.BleHttpRequest
import com.ustadmobile.port.sharedse.impl.http.BleHttpResponse
import com.ustadmobile.port.sharedse.impl.http.asBleHttpResponse
import fi.iki.elonen.NanoHTTPD
import kotlinx.serialization.json.Json
import kotlinx.serialization.toUtf8Bytes
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

/**
 * This class handle all the GATT server device's Bluetooth Low Energy callback
 *
 * Operation flow:
 *
 * When a client wants to send a BleMessage it will make one characteristicWriteRequest for each
 * packet in the outgoing request. The server will store the packet. Once all packets are received
 * the server will prepare a response BleMessage and store it. The client will then make a
 * characteristicReadRequest for each packet in the response.
 *
 * @author kileha3
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
class BleGattServer
/**
 * Constructor which will be used when creating new instance of BleGattServer
 * @param context Application context
 * @param networkManager Instance of a NetworkManagerBle for getting
 * BluetoothManager instance.
 */
(context: Context, networkManager: NetworkManagerBle,
 sessionFactory: HttpSessionFactory) :
        BleGattServerCommon(context, networkManager, sessionFactory) {

    class PendingReplyMessage(val destAddr: String, val characteristicUuid: UUID,
                              val message: BleMessage, mtu: Int,
                              val currentPacket: AtomicInteger = AtomicInteger(0)) {
        val packetsToSend = message.getPackets(mtu - ATT_HEADER_SIZE)

        val packetNum = AtomicInteger(0)
    }

    val pendingReplies: MutableList<PendingReplyMessage> = CopyOnWriteArrayList()

    /**
     * Get instance of a BluetoothGattServer
     * @return Instance of BluetoothGattServer
     */
    @set:VisibleForTesting
    var gattServer: BluetoothGattServer? = null

    private val messageAssembler = BleMessageAssembler()

    val networkManagerAndroid = networkManager

    @get:VisibleForTesting
    /**
     * Grant permission a peer device to start reading characteristics values
     *//* Reject all direct characteristics read from unknown source
        (one of our characteristics has NO_RESPONSE set).*/
    /**
     * Start receiving message packets sent from peer device
     *///Grant permission to the peer device to write on this characteristics
    //start receiving packets from the client device
    //Send back response
    //Our service doesn't require confirmation, if it does then reject sending packets
    val gattServerCallback: BluetoothGattServerCallback = object : BluetoothGattServerCallback() {

        override fun onCharacteristicReadRequest(device: BluetoothDevice, requestId: Int, offset: Int,
                                                 characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

            Napier.d(" onCharacteristicReadRequest from ${device.address}")
            if(characteristic.uuid in CHARACTERISTIC_UUIDS) {
                val clientMessage = pendingReplies.firstOrNull { it.destAddr == device.address
                        && it.characteristicUuid == characteristic.uuid }
                Napier.d(" readRequest from ${device.address}")
                if(clientMessage != null) {
                    val packetToSend = clientMessage.packetsToSend[clientMessage.packetNum.get()]
                    val responseSent = gattServer!!.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
                            0, packetToSend)
                    if(responseSent) {
                        Napier.d("SendResponse #${clientMessage.packetNum.get()} to ${device.address}")
                        val packetSent = clientMessage.packetNum.incrementAndGet()
                        if(packetSent == clientMessage.packetsToSend.size) {
                            Napier.d("Response ${clientMessage.message.messageId} completely sent")
                            pendingReplies.remove(clientMessage)
                        }
                    }else {
                        Napier.e("SendResponse not accepted")
                        gattServer!!.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE,
                                0, null)
                    }
                }else {
                    Napier.e("Read request: no response to send")
                    gattServer!!.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE,
                            0, null)
                }
            }else {
                Napier.e("Read request: wrong characteristic")
                gattServer!!.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE,
                        0, null)
            }
        }


        override fun onCharacteristicWriteRequest(device: BluetoothDevice, requestId: Int,
                                                  characteristic: BluetoothGattCharacteristic,
                                                  preparedWrite: Boolean, responseNeeded: Boolean,
                                                  offset: Int, value: ByteArray) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite,
                    responseNeeded, offset, value)

            if (characteristic.uuid in CHARACTERISTIC_UUIDS) {
                Napier.d(" write permission requested by ${device.address}")
                if(responseNeeded) {
                    val granted = gattServer!!.sendResponse(device, requestId,
                            BluetoothGatt.GATT_SUCCESS, 0, null)
                    Napier.d("Sent response to ${device.address}. accepted=$granted")
                }else {
                    Napier.d("Response not required by ${device.address}")
                }

                val messageReceived = messageAssembler.handleIncomingPacket(
                        device.address, value)
                UMLog.l(UMLog.DEBUG, 691,
                        "BLEGattServer: Received all packets from " + device.address + " "
                                + (messageReceived != null))
                if (messageReceived != null) {
                    val currentMtuSize = messageReceived.mtu

                    UMLog.l(UMLog.DEBUG, 691,
                            "BLEGattServer: Request received with default MTU size of $currentMtuSize")
                    //remove any stuck or pending replies that might be there
                    pendingReplies.removeAll { it.destAddr == device.address
                            && it.characteristicUuid == characteristic.uuid }
                    val messageToSend = handleRequest(messageReceived, device.address)
                    pendingReplies.add(PendingReplyMessage(device.address, characteristic.uuid,
                            messageToSend!!, currentMtuSize))

                    UMLog.l(UMLog.DEBUG, 691,
                            "BLEGattServer: Prepare response to send back to " + device.address)
                }


            }else {
                UMLog.l(UMLog.DEBUG, 691,
                        "BLEGattServer: wrong clientToServerCharacteristic: ${characteristic.uuid}")
            }
        }
    }

    init {
        this.gattServer = networkManager.getBluetoothManager().openGattServer(context, gattServerCallback)
        UMLog.l(UMLog.DEBUG, 691, "BLEGattServer: Opened")
    }

    override fun handleHttpRequest(bleMessageReceived: BleMessage, clientDeviceAddr: String): BleMessage {
        val bleRequest = Json.parse(BleHttpRequest.serializer(), String(bleMessageReceived.payload!!))
        UMLog.l(UMLog.DEBUG, 691,
                "BLEGattServer: Request ID# ${bleMessageReceived.messageId} " +
                        "Received bleRequest ${bleRequest.reqUri} ")
        val response = networkManagerAndroid.httpd.serve(bleRequest)
                ?: NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "text/plain", "not found")
        val bleResponse = response.asBleHttpResponse()

        val bleResponseStr = Json.stringify(BleHttpResponse.serializer(), bleResponse)
        val payload = bleResponseStr.toUtf8Bytes()
        UMLog.l(UMLog.DEBUG, 691,
                "BLEGattServer: Sending response ID# ${bleMessageReceived.messageId} ${bleRequest.reqUri} " +
                        "(${bleResponse.statusCode}) \n==Content Body: Message payload.size = ${payload.size} bytes==${bleResponse.body}\n\n")

        return BleMessage(BleMessage.MESSAGE_TYPE_HTTP,
                BleMessage.getNextMessageIdForReceiver(clientDeviceAddr),
                payload)
    }

    companion object {

        /**
         * Each BLE clientToServerCharacteristic notification etc. packet has a 1 byte Op-Code and a 2 byte
         * Attribute Handle. When we send a clientToServerCharacteristic the maximum amount of data we can send
         * is the MTU minus this overhead (e.g. MTU - 3). See "ATT MTU" here:
         *  https://punchthrough.com/maximizing-ble-throughput-part-2-use-larger-att-mtu-2/
         */
        const val ATT_HEADER_SIZE = 3

        val CHARACTERISTIC_UUIDS = NetworkManagerBleCommon.BLE_CHARACTERISTICS.map { UUID.fromString(it) }

    }
}
