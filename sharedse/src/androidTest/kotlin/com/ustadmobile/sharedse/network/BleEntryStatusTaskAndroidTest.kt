package com.ustadmobile.sharedse.network

import android.bluetooth.*
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.ustadmobile.lib.db.entities.NetworkNode
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.util.*

/**
 * Test class which tests [BleEntryStatusTaskAndroid] to make sure it behaves as expected
 * under different circumstances.
 *
 * @author kileha3
 */
@RunWith(RobolectricTestRunner::class)
class BleEntryStatusTaskAndroidTest {

    private var entries: List<Long>? = null


    private var statusTask: BleEntryStatusTaskAndroid? = null


    @Before
    fun setUp() {
        entries = Arrays.asList(1056289670L, 4590875612L, 9076137860L, 2912543894L)
        val mockedBluetoothManager = mock(BluetoothManager::class.java)
        val mockedBluetoothAdapter = mock(BluetoothAdapter::class.java)
        val mockedDevice = mock(BluetoothDevice::class.java)
        val mockedGatt = mock(BluetoothGatt::class.java)
        val context = InstrumentationRegistry.getInstrumentation().context

        val manager = NetworkManagerBle(context)
        Mockito.spy(manager)
        val networkNode = NetworkNode()
        networkNode.bluetoothMacAddress = "00:11:22:33:FF:EE"
        statusTask = BleEntryStatusTaskAndroid(context, manager, entries!!, networkNode)
        statusTask!!.setBluetoothManager(mockedBluetoothManager)

        `when`(mockedBluetoothManager.adapter)
                .thenReturn(mockedBluetoothAdapter)

        `when`(mockedBluetoothAdapter.getRemoteDevice(networkNode.bluetoothMacAddress))
                .thenReturn(mockedDevice)

        `when`(mockedDevice.connectGatt(any(Context::class.java),
                eq<Boolean>(java.lang.Boolean.FALSE), any(BluetoothGattCallback::class.java))).thenReturn(mockedGatt)

    }

    @Test
    fun givenEntryStatusIsCreated_whenStarted_thenShouldCreateBleClientCallback() {

        statusTask!!.run()

        assertNotNull("BleClientCallback should not be null ",
                statusTask!!.gattClientCallback)
    }

    @Test
    fun givenEntryStatusIsCreated_whenStartedAndBleClientCallbackIsCreated_thenItShouldHaveRightMessage() {

        statusTask!!.run()

        assertNotNull("BleClientCallback should not be null ", statusTask!!.gattClientCallback)

        val receivedEntries = BleMessageUtil.bleMessageBytesToLong(statusTask!!.message!!.payload!!)

        assertEquals("Should have the same message", receivedEntries, entries)
    }

}
