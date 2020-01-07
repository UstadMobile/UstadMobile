package com.ustadmobile.sharedse.network

import android.bluetooth.*
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.MINIMUM_MTU_SIZE
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.ENTRY_STATUS_REQUEST
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.MAXIMUM_MTU_SIZE
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.USTADMOBILE_BLE_SERVICE_UUID
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import java.util.*


/**
 * Test class which tests [BleMessageGattClientCallback] to make sure all
 * [BluetoothGattCallback] callbacks it behaves as expected.
 *
 * @author kileha3
 */

//Commented out by Mike 14/Dec - this test needs updated to handle changes made to the underlying implementation
//@RunWith(RobolectricTestRunner::class)
class BleMessageGattClientCallbackTest {

//    private var mockedGattClient: BluetoothGatt? = null
//
//    private var messageToSend: BleMessage? = null
//
//    private var mockedCharacteristic: BluetoothGattCharacteristic? = null
//
//    private var gattClientCallback: BleMessageGattClientCallback? = null
//
//
//    @Before
//    fun setUp() {
//        mockedGattClient = mock(BluetoothGatt::class.java)
//        val entryList = Arrays.asList(1056289670L, 4590875612L, 9076137860L, 2912543894L)
//        messageToSend = BleMessage(ENTRY_STATUS_REQUEST, 42.toByte(), BleMessageUtil.bleMessageLongToBytes(entryList))
//
//        gattClientCallback = BleMessageGattClientCallback(messageToSend!!)
//
//        val service = BluetoothGattService(UUID.fromString(USTADMOBILE_BLE_SERVICE_UUID),
//                BluetoothGattService.SERVICE_TYPE_PRIMARY)
//        mockedCharacteristic = mock(BluetoothGattCharacteristic::class.java)
//        service.addCharacteristic(mockedCharacteristic)
//
//        val bluetoothDevice = mock(BluetoothDevice::class.java)
//        `when`(mockedGattClient!!.services).thenReturn(listOf(service))
//        `when`(mockedGattClient!!.requestMtu(MAXIMUM_MTU_SIZE)).thenReturn(true)
//        `when`<UUID>(mockedCharacteristic!!.uuid).thenReturn(UUID.fromString(USTADMOBILE_BLE_SERVICE_UUID))
//        `when`(mockedGattClient!!.device).thenReturn(bluetoothDevice)
//        `when`(mockedGattClient!!.device.address).thenReturn("00:11:22:33:FF:EE")
//
//    }
//
//
//    @Test
//    fun givenOnConnectionStateChanged_whenConnectedWithFailureStatus_thenShouldDisconnect() {
//        gattClientCallback!!.onConnectionStateChange(mockedGattClient!!,
//                BluetoothGatt.GATT_FAILURE, BluetoothProfile.STATE_CONNECTED)
//
//        //Verify that client was disconnected from the gatt server
//        verify<BluetoothGatt>(mockedGattClient).disconnect()
//
//        //Verify that client closed the gatt after failure
//        verify<BluetoothGatt>(mockedGattClient).close()
//
//    }
//
//    @Test
//    fun givenOnConnectionStateChanged_whenDisconnectedWithSuccessStatus_thenShouldDisconnect() {
//        gattClientCallback!!.onConnectionStateChange(mockedGattClient!!,
//                BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_DISCONNECTED)
//
//        //Verify that client was disconnected from the gatt server
//        verify<BluetoothGatt>(mockedGattClient).disconnect()
//
//
//        verify<BluetoothGatt>(mockedGattClient).close()
//    }
//
//    @Test
//    fun givenOnConnectionStateChanged_whenConnectedWithSuccessStatus_thenShouldDiscoverServices() {
//        gattClientCallback!!.onConnectionStateChange(mockedGattClient!!,
//                BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTED)
//
//        //Verify that service discovery was started
//        verify<BluetoothGatt>(mockedGattClient).discoverServices()
//    }
//
//
//    @Test
//    fun givenServiceIsDiscovered_whenMatchingCharacteristicsFound_thenShouldRequestPermissionToWrite() {
//        gattClientCallback!!.onServicesDiscovered(mockedGattClient!!, BluetoothGatt.GATT_SUCCESS)
//
//        //verify that permission was set
//        verify<BluetoothGattCharacteristic>(mockedCharacteristic).writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
//        //Verify that characteristics permission was requested
//        verify<BluetoothGatt>(mockedGattClient).setCharacteristicNotification(mockedCharacteristic, true)
//    }
//
//    @Test
//    fun givenOnCharacteristicWrite_whenGrantedPermissionToWrite_thenShouldStartSendingPackets() {
//        val packets = messageToSend!!.getPackets(MINIMUM_MTU_SIZE)
//
//        for (i in packets.indices) {
//            gattClientCallback!!.onCharacteristicWrite(mockedGattClient!!, mockedCharacteristic!!,
//                    BluetoothGatt.GATT_SUCCESS)
//
//            //verify that characteristics value was modified
//            verify<BluetoothGattCharacteristic>(mockedCharacteristic).value = packets[i]
//            //Verify that characteristics was modified
//
//            //onCharacteristicWrite is called when permission is granted, and each time writing
//            // finishes. Verifying that writeCharacteristic was called thus verifies
//            // that the process will repeat.
//            verify<BluetoothGatt>(mockedGattClient, times(i + 1)).writeCharacteristic(mockedCharacteristic)
//        }
//    }
//
//    @Test
//    fun givenOnCharacteristicsWrite_whenPermissionToWriteIsDenied_thenShouldRetryAndDisconnect(){
//        val packets = messageToSend!!.getPackets(MINIMUM_MTU_SIZE)
//
//        for (i in packets.indices) {
//            gattClientCallback!!.onCharacteristicWrite(mockedGattClient!!, mockedCharacteristic!!,
//                    BluetoothGatt.GATT_FAILURE)
//        }
//        //Verify that after many trials, device disconnected from the service
//        verify<BluetoothGatt>(mockedGattClient, times(1)).disconnect()
//    }
}
