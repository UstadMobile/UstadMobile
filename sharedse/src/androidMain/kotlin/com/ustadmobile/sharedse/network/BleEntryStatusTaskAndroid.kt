package com.ustadmobile.sharedse.network

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.lib.db.entities.NetworkNode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * This class handles all android specific entry status check from a peer device also,
 * it is responsible for creating BLE GATT client callback.
 *
 * **Note: Operation Flow**
 *
 *
 * - Once [BleEntryStatusTaskAndroid.run] is called, it creates
 * [BleMessageGattClientCallback] and pass the list of entries to be checked
 * and peer device to be checked from. After entry status check
 * [BleEntryStatusTask.onResponseReceived] will be called to report back the results.
 *
 *
 *
 * Use [BleEntryStatusTaskAndroid.run] to start executing the task itself,
 * this method will be called in [NetworkManagerBle.startMonitoringAvailability]
 * when pending task to be executed is found.
 *
 *
 * @see BleMessageGattClientCallback
 *
 * @see BleEntryStatusTask
 *
 * @see NetworkManagerBle
 *
 *
 * @author kileha3
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
class BleEntryStatusTaskAndroid : BleEntryStatusTask {

    private val gattClientCallbackManager: GattClientCallbackManager

    private var bluetoothManager: BluetoothManager? = null

    private var mGattClient: BluetoothGatt? = null

    private var managerBle: NetworkManagerBle? = null

    /**
     * Constructor to be used when creating platform specific instance of BleEntryStatusTask
     * @param context Platform specific application context.
     * @param entryUidsToCheck List of Id's to be checked for availability from a peer device.
     * @param peerToCheck Peer device for those entries to be checked from.
     */
    constructor(gattClientCallbackManager: GattClientCallbackManager, context: Context, managerAndroidBle: NetworkManagerBle,
                entryUidsToCheck: List<Long>, peerToCheck: NetworkNode)
            : super(context, managerAndroidBle, entryUidsToCheck, peerToCheck) {
        this.gattClientCallbackManager = gattClientCallbackManager
        this.managerBle = managerAndroidBle
        this.context = context
        val messagePayload = BleMessageUtil.bleMessageLongToBytes(entryUidsToCheck)
        this.message = BleMessage(NetworkManagerBleCommon.ENTRY_STATUS_REQUEST,
                BleMessage.getNextMessageIdForReceiver(peerToCheck.bluetoothMacAddress!!),
                messagePayload)
    }

    /**
     * Constructor to be used when creating platform specific instance of BleEntryStatusTask
     * responsible for sending custom messages apart from entry status check.
     * @param context Platform specific application context.
     * @param message Message to be sent
     * @param peerToSendMessageTo peer to send message to
     * @param responseListener Message response listener object
     */
    constructor(gattClientCallbackManager: GattClientCallbackManager,
                context: Context, managerBle: NetworkManagerBle, message: BleMessage,
                peerToSendMessageTo: NetworkNode,
                responseListener: BleMessageResponseListener)
            : super(context, managerBle, message, peerToSendMessageTo, responseListener) {
        this.gattClientCallbackManager = gattClientCallbackManager
    }


    /**
     * Set bluetooth manager for BLE GATT communication
     * @param bluetoothManager BluetoothManager instance
     */
    @VisibleForTesting
    fun setBluetoothManager(bluetoothManager: BluetoothManager) {
        this.bluetoothManager = bluetoothManager
    }


    /**
     * Start entry status check task
     */
    override fun sendRequest() {
        try {
            //TODO: this changes to get the clientcallback from a pool
            val bluetoothAddr = networkNode.bluetoothMacAddress
            if(bluetoothAddr == null) {
                //Illegal Argument - should never get here
                throw IllegalArgumentException("SendRequest called with null bluetooth destination")
            }

            val gattClientCallback = gattClientCallbackManager.getGattClient(bluetoothAddr)
            GlobalScope.launch {
                try {
                    val messageReceived = gattClientCallback.sendMessage(message)
                    onResponseReceived(bluetoothAddr, messageReceived, null)
                }catch(e: Exception) {
                    onResponseReceived(bluetoothAddr, null, e)
                }
            }

        } catch (e: IllegalArgumentException) {
            UMLog.l(UMLog.ERROR, 698,
                    "Wrong address format provided", e)
        }

    }
}
