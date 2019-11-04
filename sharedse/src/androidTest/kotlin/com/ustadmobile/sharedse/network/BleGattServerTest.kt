package com.ustadmobile.sharedse.network

import android.bluetooth.*
import android.content.Context
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.MINIMUM_MTU_SIZE
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.ENTRY_STATUS_REQUEST
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.USTADMOBILE_BLE_SERVICE_UUID
import kotlinx.coroutines.Dispatchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import java.util.*

/**
 * Test class which tests [BleGattServer] to make sure it behaves as expected on all
 * [BluetoothGattServerCallback] callbacks
 *
 * @author kileha3
 */

@RunWith(RobolectricTestRunner::class)
class BleGattServerTest {

    private var mockedCharacteristics: BluetoothGattCharacteristic? = null

    private var mockedGattServer: BluetoothGattServer? = null

    private var mGattServer: BleGattServer? = null

    private var mockedBluetoothDevice: BluetoothDevice? = null

    private var bleMessage: BleMessage? = null

    @Before
    fun setUp() {

        val context = mock(Context::class.java)

        mockedGattServer = mock(BluetoothGattServer::class.java)

        mockedBluetoothDevice = mock(BluetoothDevice::class.java)

        `when`(mockedBluetoothDevice!!.address).thenReturn("00:11:22:33:FF:EE")

        mockedCharacteristics = mock(BluetoothGattCharacteristic::class.java)

        val mockBluetoothManager = mock(BluetoothManager::class.java)

        val httpd = EmbeddedHTTPD(0, context)
        try {
            httpd.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val networkManager = NetworkManagerBle(context, Dispatchers.Default, httpd)
        networkManager.setBluetoothManager(mockBluetoothManager)

        mGattServer = BleGattServer(context, networkManager)
        mGattServer!!.gattServer = mockedGattServer

        `when`(mockBluetoothManager.openGattServer(any(Context::class.java),
                any(BluetoothGattServerCallback::class.java))).thenReturn(mockedGattServer)

        val entryList = Arrays.asList(1056289670L, 4590875612L, 9076137860L, 2912543894L)
        bleMessage = BleMessage(ENTRY_STATUS_REQUEST, 42.toByte(), BleMessageUtil.bleMessageLongToBytes(entryList))
        `when`(mockedCharacteristics!!.uuid).thenReturn(UUID.fromString(USTADMOBILE_BLE_SERVICE_UUID))
    }

    @Test
    fun givenOnCharacteristicReadRequestFromKnownSource_whenIsCharacteristicsWithSameUUID_thenShouldGrantPermission() {
        mGattServer!!.gattServerCallback.onCharacteristicReadRequest(mockedBluetoothDevice, 0, 0, mockedCharacteristics)
        //Verify that permission to read on the characteristics was granted
        verify<BluetoothGattServer>(mockedGattServer).sendResponse(mockedBluetoothDevice, 0, BluetoothGatt.GATT_SUCCESS, 0, null)
    }

    @Test
    fun givenOnCharacteristicReadRequestFromUnknownSource_whenIsCharacteristicsWithSameUUID_thenShouldGrantPermission() {
        mockedCharacteristics!!.writeType = BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
        mGattServer!!.gattServerCallback.onCharacteristicReadRequest(mockedBluetoothDevice, 0, 0, mockedCharacteristics)

        //Verify that permission to read on the characteristics was granted
        verify<BluetoothGattServer>(mockedGattServer).sendResponse(mockedBluetoothDevice, 0, BluetoothGatt.GATT_SUCCESS, 0, null)
    }


    @Test
    fun givenOnCharacteristicWriteRequest_whenIsCharacteristicsWithSameUUID_thenShouldGrantPermission() {
        mGattServer!!.gattServerCallback.onCharacteristicWriteRequest(mockedBluetoothDevice, 0, mockedCharacteristics,
                true, true, 0, bleMessage!!.getPackets(MINIMUM_MTU_SIZE)[0])

        //Verify that permission to write on the characteristics was granted
        verify<BluetoothGattServer>(mockedGattServer).sendResponse(mockedBluetoothDevice, 0, BluetoothGatt.GATT_SUCCESS,
                0, null)
    }

    @Test
    fun givenOnCharacteristicWriteRequest_whenIsCharacteristicsWithSameUUID_thenShouldStartReceivingPackets() {

        //TODO: Make simulation possible for handling this test case

    }
}
