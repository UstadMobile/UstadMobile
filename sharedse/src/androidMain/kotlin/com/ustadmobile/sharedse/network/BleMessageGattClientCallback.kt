package com.ustadmobile.sharedse.network


import android.bluetooth.*
import android.os.Build
import androidx.annotation.RequiresApi
import com.github.aakira.napier.Napier
import com.ustadmobile.sharedse.network.BleGattServer.Companion.ATT_HEADER_SIZE
import com.ustadmobile.sharedse.network.NetworkManagerBle.Companion.USTADMOBILE_BLE_SERVICE_UUID_UUID
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.MINIMUM_MTU_SIZE
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.MAXIMUM_MTU_SIZE
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.USTADMOBILE_BLE_SERVICE_UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.ceil

/**
 * This class handle all the GATT client Bluetooth Low Energy callback
 *
 *
 *
 * **Note: Operation Flow**
 *
 *
 * - When a device is connected to a BLE node, if it  has android version 5 and above
 * it will request for the MTU change and upon receiving a call back on
 * {[BleMessageGattClientCallback.onMtuChanged]} it will updateState MTU and request for
 * all available services from the GATT otherwise it will request for available services.
 * This will be achieved by calling [BluetoothGatt.discoverServices].
 * Once services are found, all characteristics in those services will be listed.
 *
 *
 * - When trying to send a message, it will need write permission from the BLE node,
 * and when requested response might be as discussed in [BleGattServer].
 *
 *
 * - If it will receive [BluetoothGatt.GATT_SUCCESS] response, then it will start data
 * transmission to the BLE node. Upon receiving response the
 * [BleMessageGattClientCallback.onCharacteristicChanged] method will be invoked.
 *
 * For more explanation about MTU and MTU throughput, below is an article you gan go through
 * @link https://interrupt.memfault.com/blog/ble-throughput-primer
 *
 * @author kileha3
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
class BleMessageGattClientCallback() : BluetoothGattCallback() {

    private val receivedMessage: BleMessage

    private var messageToSend: BleMessage? = null

    private var responseListener: BleMessageResponseListener? = null

    private var packetIteration = 0

    private val serviceDiscoveryRef = AtomicBoolean(false)

    private val mConnected = AtomicBoolean(true)

    private val mClosed = AtomicBoolean(false)

    @Volatile
    private var lastActive: Long

    private var packetTransferAttemptsRemaining = 3

    private val MAX_DELAY_TIME  = 1000L

    private val currentMtu = AtomicInteger(MINIMUM_MTU_SIZE)

    private val mtuCompletableDeferred = CompletableDeferred<Int>()

    private val gattReadyCompletableDeferred = CompletableDeferred<BluetoothGatt>()

    private val messageChannel = Channel<PendingMessage>(Channel.UNLIMITED)

    class PendingMessage(val messageId: Int, val outgoingMessage: BleMessage,
                         internal val incomingMessage: BleMessage = BleMessage(),
                         val messageReceived: CompletableDeferred<BleMessage> = CompletableDeferred(),
                         val responseListener: BleMessageResponseListener? = null) {

        //var mtu: Int = -1

        lateinit var outgoingPackets: Array<ByteArray>

        var outgoingPacketNum: Int = 0

        var incomingPacketNum: Int = 0
    }

    private val messageProcessors: MutableMap<UUID, PendingMessageProcessor> = ConcurrentHashMap()


    /**
     * This is a channel based request processor that will handle sending a BleMessage and receiving
     * the reply. The main class callback will call the onCharacteristicWrite and
     * readCharacteristic methods when they match our characteristic UUID
     */
    class PendingMessageProcessor(val messageChannel: Channel<PendingMessage>,
                                  var mGatt: BluetoothGatt,
                                  val mtu: Int,
                                  var characteristic: BluetoothGattCharacteristic){

        val currentPendingMessage = AtomicReference<PendingMessage?>()

        private val logPrefix
                get() = "BleMessageGattClientCallback: Request ID #" +
                        "${currentPendingMessage.get()?.outgoingMessage?.messageId} "

        suspend fun process() {
            for(message in messageChannel) {
                //send the packet itself
                currentPendingMessage.set(message)
                message.outgoingPackets = message.outgoingMessage.getPackets(mtu)
                Napier.d("$logPrefix processor received message in channel " +
                        "${message.outgoingMessage.payload?.size} bytes MTU=${mtu}")
                try {
                    characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    val notificationSet = mGatt.setCharacteristicNotification(characteristic, true)
                    if(notificationSet) {
                        Napier.d("$logPrefix notification set OK")
                    }else {
                        Napier.e("$logPrefix notificatoin NOT set")
                    }

                    sendNextPacket(mGatt, characteristic)
                    val messageReceived = message.messageReceived.await()
                    message.responseListener?.onResponseReceived("",
                            messageReceived, null)
                    Napier.d("$logPrefix message processing completed successfully")
                }catch(e: Exception) {
                    Napier.e("$logPrefix MessageProcessor exception", e)
                    message.responseListener?.onResponseReceived("", null,
                            e)
                }
                Napier.d("$logPrefix waiting for next message")
                delay(1000)
            }
        }


        fun sendNextPacket(gatt: BluetoothGatt, lastCharacteristic: BluetoothGattCharacteristic) {
            val pendingMessageVal = currentPendingMessage.get()
            if(pendingMessageVal != null) {
                val packetNum = pendingMessageVal.outgoingPacketNum
                lastCharacteristic.value = pendingMessageVal.outgoingPackets[packetNum]
                val submitted = gatt.writeCharacteristic(lastCharacteristic)
                Napier.d({"$logPrefix sent packet #$packetNum/${pendingMessageVal.outgoingPackets.size} accepted=$submitted"})
                if(!submitted) {
                    Napier.e("$logPrefix packet submission not accepted!")
                }
            }else {
                Napier.e("$logPrefix pendingmessageval = null")
            }
        }

        fun onCharacteristicWrite(gatt: BluetoothGatt,
                                  characteristic: BluetoothGattCharacteristic, status: Int) {
            Napier.d((if (status == BluetoothGatt.GATT_SUCCESS) "$logPrefix: Allowed to send packets"
            else "$logPrefix: Not allowed to send packets") + " to ${gatt.device.address}")

            val currentMessageVal = currentPendingMessage.get()
            if(currentMessageVal != null) {
                if (++currentMessageVal.outgoingPacketNum < currentMessageVal.outgoingPackets.size) {
                    sendNextPacket(gatt, characteristic)
                } else {
                    Napier.v("$logPrefix sent all " +
                            "${currentPendingMessage.get()?.outgoingPackets?.size} packets - " +
                            "${currentMessageVal.outgoingMessage.payload?.size}  bytes. Awaiting response.")
                    //we now expect the server to send a response
                }
            }

            mGatt = gatt
            this.characteristic = characteristic
//                if (packetIteration < packets.size) {
//                    val packetToSend = packets[packetIteration]
//                    UMLog.l(UMLog.DEBUG, 698, "$logPrefix: Transferring packet #${packetIteration + 1}  of size " +
//                            "${packetToSend.size} to ${gatt.device.address} with mtu $currentMtu")
//                    characteristic.value = packetToSend
//                    gatt.writeCharacteristic(characteristic)
//                    packetIteration++
//
//                } else {
//                    packetIteration = 0
//                    UMLog.l(UMLog.DEBUG, 698, "$logPrefix: Sent packet of " +
//                            packets.size.toString() + " packet(s) transferred successfully to " +
//                            "the remote device =" + gatt.device.address)
//                    //We now expect the server to send a response
//                }
//            } else if (packetTransferAttemptsRemaining > 0) {
//                packetTransferAttemptsRemaining--
//                UMLog.l(UMLog.DEBUG, 698, "$logPrefix: Failed to send packet to ${gatt.device.address}" +
//                        " remain $packetTransferAttemptsRemaining trial, trying now...")
//                GlobalScope.launch(Dispatchers.Main) {
//                    try {
//                        delay(MAX_DELAY_TIME)
//                        onCharacteristicWrite(gatt, characteristic, BluetoothGatt.GATT_SUCCESS)
//                    } catch (e: Exception) {
//                        UMLog.l(UMLog.ERROR, 698, "$logPrefix: Exception on retry packet write")
//                    }
//                }
//
//            } else {
//                UMLog.l(UMLog.DEBUG, 698,
//                        "$logPrefix: Failed many times to send packet #${packetIteration} to ${gatt.device.address} disconnecting now")
//                cleanup(gatt)
//            }
        }


        fun readCharacteristics(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            mGatt = gatt
            this.characteristic = characteristic

            val currentMessageVal = currentPendingMessage.get()
            if(currentMessageVal != null) {
                val complete = currentMessageVal.incomingMessage.onPackageReceived(characteristic.value)
                currentMessageVal.incomingPacketNum++
                val expectedPayload = currentMessageVal.incomingMessage.length
                val totalExpectedPackets  = ceil(expectedPayload.toFloat() / currentMessageVal.outgoingMessage.mtu )
                Napier.d("$logPrefix received packet " +
                        "#${currentMessageVal.incomingPacketNum}/$totalExpectedPackets expect " +
                        "MTU= ${currentMessageVal.incomingMessage.mtu} message length= $expectedPayload bytes")
                if(complete) {
                    Napier.v("$logPrefix received complete message " +
                            "${currentMessageVal.incomingMessage.payload?.size} bytes")
                    currentMessageVal.messageReceived.complete(currentMessageVal.incomingMessage)
                    currentMessageVal.responseListener?.onResponseReceived("",
                            currentMessageVal.incomingMessage, null)
                }
            }else {
                Napier.e("$logPrefix currentMessageVal = null")
            }
        }
    }


    private val logPrefix
        get() = "BleMessageGattClientCallback Request ID# ${messageToSend?.messageId ?: -1}"

    init {
        receivedMessage = BleMessage()
        lastActive = System.currentTimeMillis()
    }

    suspend fun sendMessage(outgoingMessage: BleMessage,
                            responseListener: BleMessageResponseListener? = null): BleMessage {
        val pendingMessage = PendingMessage(0, outgoingMessage,
                responseListener = responseListener)

        Napier.d( "BleMessageGattClientCallback: sendMessage #" +
                "${outgoingMessage.messageId} MTU=$currentMtu")
        messageChannel.send(pendingMessage)
        return pendingMessage.messageReceived.await()
    }

    /**
     * Set listener to report back results on the listening part.
     * @param responseListener BleMessageResponseListener listener
     */
//    internal fun setOnResponseReceived(responseListener: BleMessageResponseListener) {
//        this.responseListener = responseListener
//    }

    /**
     * Receive MTU change event when server device changed it's MTU to the requested value.
     */
    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        super.onMtuChanged(gatt, mtu, status)
        Napier.d("$logPrefix: MTU changed from $currentMtu to $mtu")
        currentMtu.set(mtu)
        mtuCompletableDeferred.complete(mtu)
    }

    /**
     * Start discovering GATT services when peer device is connected or disconnects from GATT
     * when connection failed.
     */
    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)

        val remoteDeviceAddress = gatt.device.address

        if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
            Napier.d("$logPrefix: Device connected to $remoteDeviceAddress")

            if (!serviceDiscoveryRef.get()) {
                Napier.d("$logPrefix: Discovering services offered by $remoteDeviceAddress")
                serviceDiscoveryRef.set(true)
                gatt.discoverServices()
            }
        } else if(status != BluetoothGatt.GATT_SUCCESS && !mClosed.get()){
            Napier.e("$logPrefix: onConnectionChange not successful and connection is not " +
                    "closed $status from $remoteDeviceAddress")
            cleanup(gatt)
            responseListener?.onResponseReceived(remoteDeviceAddress, null,
                        IOException("BLE onConnectionStateChange not successful." +
                                "Status = " + status))
        }
    }


    /**
     * Enable notification to be sen't back when characteristics are modified
     * from the GATT server's side.
     */
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        super.onServicesDiscovered(gatt, status)
        val service = gatt.services.firstOrNull { it.uuid.toString() == USTADMOBILE_BLE_SERVICE_UUID }  //findMatchingService(gatt.services)
        if (service == null) {
            Napier.e("$logPrefix: ERROR Ustadmobile Service not found on " +
                    gatt.device.address)
            responseListener?.onResponseReceived(gatt.device.address, null,
                    IOException("UstadMobile service not found on device"))
            cleanup(gatt)
            return
        }

        Napier.d("$logPrefix: Ustadmobile Service found on ${gatt.device.address}")
        val characteristics = service.characteristics

        val characteristic = characteristics[0]
        if (characteristic.uuid == USTADMOBILE_BLE_SERVICE_UUID_UUID) {
            //characteristics.add(characteristic)
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            gatt.setCharacteristicNotification(characteristic, true)
            GlobalScope.launch(Dispatchers.Main) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Napier.d("$logPrefix: Requesting MTU changed from $currentMtu to " +
                            "$MAXIMUM_MTU_SIZE")
                    gatt.requestMtu(MAXIMUM_MTU_SIZE)
                    var changedMtu = -1
                    withTimeoutOrNull(MAX_DELAY_TIME) {
                        changedMtu = mtuCompletableDeferred.await()
                    }
                    Napier.d("$logPrefix: Deferrable MTU change: got $changedMtu")
                }
                //TODO: We are now ready to process messages
                //onCharacteristicWrite(gatt, characteristic, BluetoothGatt.GATT_SUCCESS)

                val processor0 = PendingMessageProcessor(messageChannel, gatt, currentMtu.get(),
                        characteristic)
                messageProcessors.put(characteristic.uuid, processor0)

                GlobalScope.launch(Dispatchers.Main) {
                    processor0.process()
                }
            }
        }
    }


    /**
     * Start transmitting message packets to the peer device once given permission
     * to write on the characteristic
     */
    override fun onCharacteristicWrite(gatt: BluetoothGatt,
                                       characteristic: BluetoothGattCharacteristic, status: Int) {
        super.onCharacteristicWrite(gatt, characteristic, status)

        val messageProcessor = messageProcessors[characteristic.uuid]
        if(messageProcessor != null) {
            messageProcessor.onCharacteristicWrite(gatt, characteristic, status)
        }else {
            //log it
            Napier.v("onCharacteristicWrite no message processor " +
                    "for characteristic UUID ${characteristic.uuid}")
        }

//        UMLog.l(UMLog.DEBUG, 698, (if(status == BluetoothGatt.GATT_SUCCESS) "$logPrefix: Allowed to send packets"
//        else "$logPrefix: Not allowed to send packets" ) + " to ${gatt.device.address}")
//
//        val packets = messageToSend.getPackets(currentMtu)
//        if (status == BluetoothGatt.GATT_SUCCESS) {
//            if (packetIteration < packets.size) {
//                val packetToSend = packets[packetIteration]
//                UMLog.l(UMLog.DEBUG, 698, "$logPrefix: Transferring packet #${packetIteration + 1}  of size " +
//                        "${packetToSend.size} to ${gatt.device.address} with mtu $currentMtu")
//                characteristic.value = packetToSend
//                gatt.writeCharacteristic(characteristic)
//                packetIteration++
//
//            } else {
//                packetIteration = 0
//                UMLog.l(UMLog.DEBUG, 698,"$logPrefix: Sent packet of " +
//                        packets.size.toString() + " packet(s) transferred successfully to " +
//                                "the remote device =" + gatt.device.address)
//                //We now expect the server to send a response
//            }
//        }else if(packetTransferAttemptsRemaining > 0) {
//            packetTransferAttemptsRemaining--
//            UMLog.l(UMLog.DEBUG, 698, "$logPrefix: Failed to send packet to ${gatt.device.address}" +
//                    " remain $packetTransferAttemptsRemaining trial, trying now...")
//            GlobalScope.launch(Dispatchers.Main) {
//                try {
//                    delay(MAX_DELAY_TIME)
//                    onCharacteristicWrite(gatt, characteristic, BluetoothGatt.GATT_SUCCESS)
//                }catch(e: Exception) {
//                    UMLog.l(UMLog.ERROR, 698, "$logPrefix: Exception on retry packet write")
//                }
//            }
//
//        }else {
//            UMLog.l(UMLog.DEBUG, 698,
//                    "$logPrefix: Failed many times to send packet #${packetIteration} to ${gatt.device.address} disconnecting now")
//            cleanup(gatt)
//        }
    }

    /**
     * Read modified valued from the characteristics when changed from GATT server's end.
     */
    override fun onCharacteristicRead(gatt: BluetoothGatt,
                                      characteristic: BluetoothGattCharacteristic, status: Int) {
        super.onCharacteristicRead(gatt, characteristic, status)
        Napier.d("onCharacteristicRead")
    }

    /**
     * Receive notification when characteristics value has been changed from GATT server's side.
     */
    override fun onCharacteristicChanged(gatt: BluetoothGatt,
                                         characteristic: BluetoothGattCharacteristic) {
        super.onCharacteristicChanged(gatt, characteristic)
        Napier.d("onCharacteristicChanged")
        readCharacteristics(gatt, characteristic)
    }

    /**
     * Read values from the service characteristic
     * @param gatt Bluetooth Gatt object
     * @param characteristic Modified service characteristic to read that value from
     */
    private fun readCharacteristics(gatt: BluetoothGatt,
                                    characteristic: BluetoothGattCharacteristic) {
        val processor = messageProcessors[characteristic.uuid]
        if(processor != null) {
            processor.readCharacteristics(gatt, characteristic)
        }else {
            Napier.v("readCharacteristic no message processor " +
                    "for characteristic UUID ${characteristic.uuid}")
            //log it
        }
        val messageComplete = receivedMessage.onPackageReceived(characteristic.value)
//        if (messageComplete) {
//            UMLog.l(UMLog.DEBUG, 698,"$logPrefix: Message received successfully")
//            //Before we waited for the server to disconnect us. Actually we should disconnect ourselves
//            // as per https://developer.android.com/guide/topics/connectivity/bluetooth-le "Close the client app"
//            cleanup(gatt)
//
//            responseListener?.onResponseReceived(gatt.device.address, receivedMessage, null)
//        }
    }


//    /**
//     * Find the matching service among services found by peer devices
//     * @param serviceList List of all found services
//     * @return Matching service
//     */
//    private fun findMatchingService(serviceList: List<BluetoothGattService>): BluetoothGattService? {
//        for (service in serviceList) {
//            val serviceIdString = service.uuid.toString()
//            if (matchesServiceUuidString(serviceIdString)) {
//                return service
//            }
//        }
//        return null
//    }

    private fun matchesServiceUuidString(serviceIdString: String): Boolean {
        return uuidMatches(serviceIdString, USTADMOBILE_BLE_SERVICE_UUID)
    }


    private fun uuidMatches(uuidString: String, vararg matches: String): Boolean {
        for (match in matches) {
            if (uuidString.equals(match, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    private fun cleanup(gatt: BluetoothGatt) {
        try {
            if (mConnected.get()) {
                gatt.disconnect()
                mConnected.set(false)
                Napier.i("$logPrefix: disconnected")
            }

            if (!mClosed.get()) {
                gatt.close()
                mClosed.set(true)
                Napier.i("$logPrefix: closed")
            }
        } catch (e: Exception) {
            Napier.e("$logPrefix: ERROR disconnecting")
        } finally {
            Napier.i("$logPrefix: cleanup done")
        }
    }
}
