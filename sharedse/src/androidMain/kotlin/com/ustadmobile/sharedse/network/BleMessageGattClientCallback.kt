package com.ustadmobile.sharedse.network


import android.bluetooth.*
import android.os.Build
import androidx.annotation.RequiresApi
import io.github.aakira.napier.Napier
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.MINIMUM_MTU_SIZE
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.MAXIMUM_MTU_SIZE
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.USTADMOBILE_BLE_SERVICE_UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.IOException
import java.lang.IllegalArgumentException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.ceil

/**
 * This class handle all the GATT client Bluetooth Low Energy callback
 *
 * One instance of this callback is created per server that a client wants to communicate with.
 * When a connection is the client will discover services and then initiate an MTU change request.
 *
 * The message will put onto a channel. Channel processing begins after the mtu has been changed.
 *
 * Messages must be sent one at a time per characteristic used
 * to avoid corruption on the server or client. The client will make a characteristic write request
 * for each packet in the outgoing request. Once all request packets have been sent the client will
 * make a characteristic read request for each packet in the response.
 *
 * For more explanation about MTU and MTU throughput, below is an article you gan go through
 * @link https://interrupt.memfault.com/blog/ble-throughput-primer
 *
 * @author kileha3
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
class BleMessageGattClientCallback(val deviceAddr: String,
                                   val clientCallbackManager: GattClientCallbackManager,
                                   val nodeHistoryHandler: NodeHistoryHandler) : BluetoothGattCallback() {

    private val receivedMessage: BleMessage

    private var messageToSend: BleMessage? = null

    private var responseListener: BleMessageResponseListener? = null

    private val serviceDiscoveryRef = AtomicBoolean(false)

    private val mConnected = AtomicBoolean(true)

    private val mClosed = AtomicBoolean(false)

    private val disconnectRequested = AtomicBoolean(false)

    private var disconnecting = false

    private val lastActive = AtomicLong(System.currentTimeMillis())

    private val MAX_DELAY_TIME  = 1000L

    private val currentMtu = AtomicInteger(MINIMUM_MTU_SIZE)

    private val mtuCompletableDeferred = CompletableDeferred<Int>()

    private val messageChannel = Channel<PendingMessage>(Channel.UNLIMITED)

    private val operationChannel = Channel<GattOperation>(Channel.UNLIMITED)

    private val callbackId = CALLBACK_ID_ATOMICINT.getAndIncrement()

    private val activeCharacteristics: MutableList<BluetoothGattCharacteristic> = CopyOnWriteArrayList()

    class GattOperation(val opType: Int, val characteristicUUID: UUID?,
                        val characteristicValue: ByteArray?,
                        val pendingMesage: PendingMessage?)

    class PendingMessage(val messageId: Int, val outgoingMessage: BleMessage,
                         internal val incomingMessage: BleMessage = BleMessage(),
                         val messageReceived: CompletableDeferred<BleMessage> = CompletableDeferred(),
                         val responseListener: BleMessageResponseListener? = null) {

        lateinit var outgoingPackets: Array<ByteArray>

        var outgoingPacketNum: Int = 0

        var incomingPacketNum: Int = 0
    }

    //Map of characteristic UUID -> PendingMessageProcessor
    private val processorMap: MutableMap<UUID, PendingMessageProcessor> = ConcurrentHashMap()

    private fun scheduleCheckTimeout(gatt: BluetoothGatt, interval: Long) {
        GlobalScope.launch(Dispatchers.Main) {
            delay(interval)
            if(System.currentTimeMillis() - lastActive.get() > 15000) {
                Napier.v("GattClient connection wtih ${gatt.device} is inactive. " +
                        "Requesting disconnect")
                requestDisconnect(gatt)
            }else {
                scheduleCheckTimeout(gatt, interval)
            }
        }
    }


    /**
     * This is a channel based request processor that will handle sending a BleMessage and receiving
     * the reply. The main class callback will call the onCharacteristicWrite and
     * readCharacteristic methods when they match our clientToServerCharacteristic UUID
     */
    class PendingMessageProcessor(val messageChannel: Channel<PendingMessage>,
                                  private val operationChannel: Channel<GattOperation>,
                                  var mGatt: BluetoothGatt,
                                  val mtu: Int,
                                  val clientToServerCharacteristic: BluetoothGattCharacteristic,
                                  val clientCallbackId: Int){

        val currentPendingMessage = AtomicReference<PendingMessage?>()

        private val logPrefix
                get() = "BleMessageGattClientCallback($clientCallbackId): Request ID #" +
                        "${currentPendingMessage.get()?.outgoingMessage?.messageId} "

        private var lastSendStartTime = 0L

        private var lastReceiveStartTime = 0L

        suspend fun process() {
            for(message in messageChannel) {
                if(message.messageReceived.isCancelled) {
                    Napier.d({"$logPrefix Message ${message.outgoingMessage.messageId} has been " +
                            "canceled - not processing"})
                    continue
                }

                //send the packet itself
                currentPendingMessage.set(message)
                message.outgoingPackets = message.outgoingMessage.getPackets(mtu - BleGattServer.ATT_HEADER_SIZE)
                Napier.d("$logPrefix processor received message in channel " +
                        "${message.outgoingMessage.payload?.size} bytes MTU=${mtu}")
                val startTime = System.currentTimeMillis()
                lastSendStartTime = startTime
                try {
                    clientToServerCharacteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    sendNextPacket(mGatt, clientToServerCharacteristic)
                    val messageReceived = message.messageReceived.await()
                    val duration = System.currentTimeMillis() - startTime
                    Napier.d({
                        val bytesSent = message.outgoingMessage.payload?.size ?: -1
                        val bytesReceived = messageReceived.payload?.size ?: -1
                        val speedKBps = (bytesSent + bytesReceived).toFloat() / ((duration.toFloat() * 1024) / 1000)
                        """$logPrefix SUCCESS sent $bytesSent bytes received 
                        |$bytesReceived bytes in ${duration}ms (${bytesSent + bytesReceived}
                        |@ ${String.format("%.2f", speedKBps)} KB/s
                    """.trimMargin()})
                    message.responseListener?.onResponseReceived("",
                            messageReceived, null)

                }catch(e: Exception) {
                    Napier.e("$logPrefix MessageProcessor exception", e)
                    message.responseListener?.onResponseReceived("", null,
                            e)
                }
                Napier.d("$logPrefix waiting for next message")
            }
        }


        fun sendNextPacket(gatt: BluetoothGatt, lastCharacteristic: BluetoothGattCharacteristic) {
            val pendingMessageVal = currentPendingMessage.get()
            if(pendingMessageVal != null) {
                val packetNum = pendingMessageVal.outgoingPacketNum
                GlobalScope.launch {
                    operationChannel.send(GattOperation(OP_WRITE, lastCharacteristic.uuid,
                            pendingMessageVal.outgoingPackets[packetNum], pendingMessageVal))
                    Napier.d({"$logPrefix request write: send to channel"})
                }
            }else {
                Napier.e("$logPrefix pendingmessageval = null")
            }
        }

        fun requestReadNextPacket(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            GlobalScope.launch {
                val pendingMessageVal = currentPendingMessage.get()
                if(pendingMessageVal != null){
                    operationChannel.send(GattOperation(OP_READ, characteristic.uuid, null,
                            pendingMessageVal))
                    Napier.d("${logPrefix }Request read : sent to channel")
                }else {
                    Napier.e("$logPrefix pendingmessageval = null")
                }

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
                    val sendDuration = System.currentTimeMillis() - lastSendStartTime
                    Napier.v("$logPrefix sent all " +
                            "${currentPendingMessage.get()?.outgoingPackets?.size} packets - " +
                            "${currentMessageVal.outgoingMessage.payload?.size}  bytes in " +
                            "$sendDuration ms. Starting read.")

                    lastReceiveStartTime = System.currentTimeMillis()
                    //now read the server's reply
                    requestReadNextPacket(gatt, characteristic)
                }
            }
        }


        fun readCharacteristics(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
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
                    val duration = System.currentTimeMillis() - lastReceiveStartTime
                    val payloadSize = currentMessageVal.incomingMessage.payload?.size ?: 1
                    val speedKbps = payloadSize.toFloat() / ((duration.toFloat() * 1024)/1000)
                    Napier.v("$logPrefix SUCCESS received complete message " +
                            "${currentMessageVal.incomingMessage.payload?.size} bytes in " +
                            "$duration ms @ ${String.format("%.2f", speedKbps)} KB/s")
                    currentMessageVal.messageReceived.complete(currentMessageVal.incomingMessage)
                    currentMessageVal.responseListener?.onResponseReceived("",
                            currentMessageVal.incomingMessage, null)
                }else {
                    requestReadNextPacket(gatt, characteristic)
                }
            }else {
                Napier.e("$logPrefix currentMessageVal = null")
            }
        }
    }


    private val logPrefix
        get() = "BleMessageGattClientCallback($callbackId) Request ID# ${messageToSend?.messageId ?: -1}"

    init {
        receivedMessage = BleMessage()
    }

    suspend fun sendMessage(outgoingMessage: BleMessage,
                            responseListener: BleMessageResponseListener? = null): BleMessage {
        val pendingMessage = PendingMessage(0, outgoingMessage,
                responseListener = responseListener)

        Napier.d( "$logPrefix sendMessage #" +
                "${outgoingMessage.messageId} MTU=$currentMtu")
        messageChannel.send(pendingMessage)
        try {
            return pendingMessage.messageReceived.await()
        }catch(e: CancellationException) {
            withContext(NonCancellable) {
                pendingMessage.messageReceived.cancel()
            }
            throw e
        }
    }

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

        Napier.d({"$logPrefix CONNECTIONCHANGE newState=$newState status=$status"})
        if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
            Napier.d("$logPrefix: Device connected to $remoteDeviceAddress")
            scheduleCheckTimeout(gatt, 5000)

            if (!serviceDiscoveryRef.get()) {
                Napier.d("$logPrefix: Discovering services offered by $remoteDeviceAddress")
                serviceDiscoveryRef.set(true)
                gatt.discoverServices()
            }
        } else if(newState == BluetoothProfile.STATE_DISCONNECTED && !mClosed.get()){
            Napier.v({"$logPrefix: onConnectionChange : now DISCONNECTED, but not yet closed." +
                    "Requesting cleanup/close"})
            //cleanup(gatt)
            cleanup(gatt)
            responseListener?.onResponseReceived(remoteDeviceAddress, null,
                        IOException("BLE onConnectionStateChange not successful." +
                                "Status = " + status))
        }

        if(status != BluetoothGatt.GATT_SUCCESS) {
            nodeHistoryHandler(gatt.device.address, NODE_EVT_TYPE_FAIL)
        }
    }


    /**
     * Enable notification to be sent back when characteristics are modified
     * from the GATT server's side.
     *
     *
     */
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        super.onServicesDiscovered(gatt, status)
        val service = gatt.services.firstOrNull { it.uuid.toString() == USTADMOBILE_BLE_SERVICE_UUID }  //findMatchingService(gatt.services)
        if (activeCharacteristics.isEmpty() && service == null) {
            Napier.e("$logPrefix: ERROR Ustadmobile Service not found on " +
                    gatt.device.address)
            responseListener?.onResponseReceived(gatt.device.address, null,
                    IOException("UstadMobile service not found on device"))
            nodeHistoryHandler(gatt.device.address, NODE_EVT_TYPE_FAIL)
            //cleanup(gatt)
            requestDisconnect(gatt)
            return
        }

        if(service == null) {
            Napier.wtf({"$logPrefix: Should not happen! characteristics were empty and service is null"})
            nodeHistoryHandler(gatt.device.address, NODE_EVT_TYPE_FAIL)
            return
        }

        Napier.d("$logPrefix: Ustadmobile SERVICE DISCOVERED found on ${gatt.device.address}")
        val characteristics = service.characteristics

        val ustadUuids = NetworkManagerBleCommon.BLE_CHARACTERISTICS.map { UUID.fromString(it) }
        val ustadCharacteristics = characteristics.filter { it.uuid in ustadUuids }

        if(ustadCharacteristics.isEmpty()) {
            Napier.e("Service discovered on ${gatt.device.address} does not have ANY " +
                    "ustad characteristics - disconnecting")
            requestDisconnect(gatt)
            nodeHistoryHandler(gatt.device.address, NODE_EVT_TYPE_FAIL)
            return
        }

        this.activeCharacteristics.addAll(ustadCharacteristics)
        if(Build.VERSION.SDK_INT >= 21) {
            val initiated = gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
            Napier.v({"${logPrefix }Requesting connection priority initiated=$initiated"})
        }

        GlobalScope.launch(Dispatchers.Main) {
            delay(100)//wait for connection priority to take effect
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

            ustadCharacteristics.forEach { characteristic ->
                GlobalScope.launch {
                    val processor = PendingMessageProcessor(messageChannel, operationChannel,
                            gatt, currentMtu.get(), characteristic, callbackId)
                    processorMap[characteristic.uuid] = processor
                    processor.process()
                }
            }
        }

        GlobalScope.launch(Dispatchers.Main) {
            runNextOperation(gatt)
        }
    }



    /**
     * Start transmitting message packets to the peer device once given permission
     * to write on the clientToServerCharacteristic
     */
    override fun onCharacteristicWrite(gatt: BluetoothGatt,
                                       characteristic: BluetoothGattCharacteristic, status: Int) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        lastActive.set(System.currentTimeMillis())

        val messageProcessor = processorMap[characteristic.uuid]
        if(messageProcessor != null) {
            messageProcessor.onCharacteristicWrite(gatt, characteristic, status)
        }else {
            //log it
            Napier.v("onCharacteristicWrite no message processor " +
                    "for clientToServerCharacteristic UUID ${characteristic.uuid}")
        }

        runBlocking { runNextOperation(gatt) }
    }

    /**
     * Read modified valued from the characteristics when changed from GATT server's end.
     */
    override fun onCharacteristicRead(gatt: BluetoothGatt,
                                      characteristic: BluetoothGattCharacteristic, status: Int) {
        val processor = processorMap[characteristic.uuid]
        lastActive.set(System.currentTimeMillis())

        if(processor != null) {
            processor.readCharacteristics(gatt, characteristic)
        }else {
            Napier.v("readCharacteristic no message processor " +
                    "for clientToServerCharacteristic UUID ${characteristic.uuid}")
            //log it
        }

        runBlocking { runNextOperation(gatt) }
    }



    suspend fun runNextOperation(gatt: BluetoothGatt) {
        var opSent = false
        do {
            val nextOp = operationChannel.receive()
            if(nextOp.pendingMesage?.messageReceived?.isCancelled ?: false
                    || disconnecting) {
                if(!disconnecting) {
                    Napier.d("nextOp was for canceled message : skipping operation")
                }else {
                    Napier.d("This client is actually disconnecting : skipping operation")
                }

                continue
            }

            val characteristic = activeCharacteristics.firstOrNull { it.uuid == nextOp.characteristicUUID }

            when(nextOp.opType) {
                OP_READ ->  {
                    val initiated = gatt.readCharacteristic(characteristic!!)
                    Napier.d("$logPrefix run: readCharacteristic: ${characteristic.uuid} initiated=$initiated")
                }

                OP_WRITE -> {
                    val characteristicVal = nextOp.characteristicValue
                    if(characteristicVal == null) {
                        Napier.wtf("OP_WRITE request with null value.")
                        throw IllegalArgumentException("OP_WRITE requested with null value")
                    }
                    characteristic!!.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    characteristic.value = characteristicVal
                    val initiated = gatt.writeCharacteristic(characteristic)
                    Napier.d("$logPrefix run: writeCharacteristic: ${characteristic.uuid} initiated=$initiated")
                }

                OP_DISCONNECT -> {
                    gatt.disconnect()
                    Napier.d("$logPrefix run: disconnect")
                    disconnecting = true
                    GlobalScope.launch(Dispatchers.Main) {
                        delay(CLOSE_TIMEOUT)
                        cleanup(gatt)
                    }

                    //TODO
//                    messageChannel.close()
//                    messageChannel.cancel()
                }
            }
            opSent = true
        }while(!opSent)

    }

    private fun requestDisconnect(gatt: BluetoothGatt) {
        if(!disconnectRequested.getAndSet(true)) {
            GlobalScope.launch {
                operationChannel.send(GattOperation(OP_DISCONNECT, null, null, null))
            }
        }
    }

    private fun cleanup(gatt: BluetoothGatt) {
        var clientCallbackHandleClosed = false
        try {
            if (!mClosed.getAndSet(true)) {
                clientCallbackHandleClosed = true
                Napier.i("$logPrefix: closing")
                gatt.close()
            }
        } catch (e: Exception) {
            Napier.e("$logPrefix: ERROR closing gatt")
        } finally {
            clientCallbackManager.takeIf { clientCallbackHandleClosed }
                    ?.handleGattClientClosed(this)
            Napier.i("$logPrefix: cleanup done")
        }
    }

    companion object {
        val OP_WRITE = 1

        val OP_READ = 2

        val OP_DISCONNECT = 3

        val CALLBACK_ID_ATOMICINT = AtomicInteger()

        //The time between calling disconnect and then calling close if we did not receive a callback
        // to onConnectionStateChange confirming disconnection
        val CLOSE_TIMEOUT = 2000L
    }
}
