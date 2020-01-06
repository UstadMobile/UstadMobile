package com.ustadmobile.sharedse.network

import android.bluetooth.*
import android.content.Context
import com.ustadmobile.lib.db.entities.NetworkNode
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.*

/**
 * Test class which tests [BleEntryStatusTaskAndroid] to make sure it behaves as expected
 * under different circumstances.
 *
 * @author kileha3
 */
class BleEntryStatusTaskAndroidTest {

//    private var entries: List<Long>? = null
//
//
//    private lateinit var statusTask: BleEntryStatusTaskAndroid
//
//
//    @Before
//    fun setUp() {
//        entries = Arrays.asList(1056289670L, 4590875612L, 9076137860L, 2912543894L)
//        val mockedBluetoothAdapter: BluetoothAdapter = mock(BluetoothAdapter::class.java)
//        val mockedBluetoothManager: BluetoothManager = mock(BluetoothManager::class.java)
//        val mockedGatt: BluetoothGatt = mock(BluetoothGatt::class.java)
//        val mockedDevice = mock(BluetoothDevice::class.java)
//        val mockContext = mock(Context::class.java)
//
//        val manager = mock(NetworkManagerBle::class.java)
//        val networkNode = NetworkNode()
//        networkNode.bluetoothMacAddress = "00:11:22:33:FF:EE"
//        statusTask = BleEntryStatusTaskAndroid(mockContext, manager, entries!!, networkNode)
//        statusTask.setBluetoothManager(mockedBluetoothManager)
//
//        //whenever(mockedBluetoothManager.adapter).thenReturn(mockedBluetoothAdapter)
//
//        `when`(mockedBluetoothManager.adapter)
//                .thenReturn(mockedBluetoothAdapter)
//
//        `when`(mockedBluetoothAdapter.getRemoteDevice(networkNode.bluetoothMacAddress))
//                .thenReturn(mockedDevice)
//
//        //whenever(mockedDevice.connectGatt(mockContext,
//        //        eq<Boolean>(java.lang.Boolean.FALSE), eq(mockCallback))).thenReturn(mockedGatt)
//
//        `when`(mockedDevice.address).thenReturn("00:11:22:33:FF:EE")
//
//        `when`(mockedDevice.connectGatt(ArgumentMatchers.any(Context::class.java),
//                eq(false), ArgumentMatchers.any(BluetoothGattCallback::class.java))).thenReturn(mockedGatt)
//
//    }
//
//    @Test
//    fun givenEntryStatusIsCreated_whenStarted_thenShouldCreateBleClientCallback() {
//
//        statusTask.sendRequest()
//
//        assertNotNull("BleClientCallback should not be null ",
//                statusTask.gattClientCallback)
//    }
//
//    @Test
//    fun givenEntryStatusIsCreated_whenStartedAndBleClientCallbackIsCreated_thenItShouldHaveRightMessage() {
//
//        statusTask.sendRequest()
//
//        assertNotNull("BleClientCallback should not be null ", statusTask.gattClientCallback)
//
//        val receivedEntries = BleMessageUtil.bleMessageBytesToLong(statusTask.message!!.payload!!)
//
//        assertEquals("Should have the same message", receivedEntries, entries)
//    }

}
