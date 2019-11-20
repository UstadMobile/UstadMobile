package com.ustadmobile.sharedse.network

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.os.Build
import java.util.concurrent.ConcurrentHashMap
import com.ustadmobile.core.impl.UMLog

class GattClientCallbackManager(val context: Context,
                                val adapter: BluetoothAdapter) {

    private val gattClientCallbacks: ConcurrentHashMap<String, BleMessageGattClientCallback> = ConcurrentHashMap()

    fun getGattClient(deviceAddress: String): BleMessageGattClientCallback {
        var currentCallback = gattClientCallbacks[deviceAddress]
        if(currentCallback == null){
            UMLog.l(UMLog.VERBOSE, 0, "GattClientCallbackManager: initiating " +
                    "connection connection to $deviceAddress")
            currentCallback = BleMessageGattClientCallback(deviceAddress, this)
            gattClientCallbacks[deviceAddress] = currentCallback
            val remoteDevice = adapter.getRemoteDevice(deviceAddress)


            //For device below lollipop they require autoConnect flag to be
            // TRUE otherwise they will always throw error 133.
            val useAutoConnect = Build.VERSION.SDK_INT < 21

            val remoteGatt = remoteDevice.connectGatt(context, false, currentCallback)

            if (Build.VERSION.SDK_INT >= 21) {
                remoteGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
            }
        }

        return currentCallback
    }

    fun handleGattDisconnected(clientCallback: BleMessageGattClientCallback){
        gattClientCallbacks.remove(clientCallback.deviceAddr)
    }


}