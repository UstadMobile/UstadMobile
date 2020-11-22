package com.ustadmobile.port.android.networkmanager

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.nhaarman.mockitokotlin2.mock
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.networkmanager.LocalAvailabilityManager
import com.ustadmobile.port.android.netwokmanager.LocalAvailabilityManagerAndroidImpl
import com.ustadmobile.port.android.util.ext.putExtraResultAsJson
import com.ustadmobile.port.android.view.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.android.di
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class TestLocalAvailabilityManagerAndroidImpl()  {

    @Rule
    @JvmField
    val permissionGrantRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN)

    /**
     *
     */
    //@Test
    fun givenDeviceDiscoverable_whenStartScanningSet_thenShouldBeDiscovered() {
        launchActivity<MainActivity>()
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val di: DI by di(appContext)
        val localAvailabilityManagerAndroidImpl = LocalAvailabilityManagerAndroidImpl(
                Endpoint("http://localhost/"), appContext, di)
        localAvailabilityManagerAndroidImpl.bluetoothScanningEnabled = true

        val nodeLiveData = localAvailabilityManagerAndroidImpl.networkNodesLiveData

        val lock = CountDownLatch(1)
        runBlocking(Dispatchers.Main) {
            nodeLiveData.observeForever {
                if(it.isNotEmpty())
                    lock.countDown()
            }
        }


        lock.await(120, TimeUnit.SECONDS)
        println("Found: ${nodeLiveData.value?.joinToString { it.bluetoothMacAddress ?:"" }}")
    }

    @Test
    fun givenIdleAvailabilityManagerImpl_whenDeviceDiscovered_thenShouldBeAddedToLiveData() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val di: DI by di(appContext)
        val accountManager = di.direct.instance<UstadAccountManager>()
        val localAvailabilityManager  = di.direct.on(accountManager.activeAccount)
                .instance<LocalAvailabilityManager>() as LocalAvailabilityManagerAndroidImpl

        val btDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("11:22:33:44:55:66")
        val foundIntent = Intent(BluetoothDevice.ACTION_FOUND).also {
            it.putExtra(BluetoothDevice.EXTRA_DEVICE, btDevice)
        }



        localAvailabilityManager.bluetoothFoundBroadcastReceiver.onReceive(appContext,
            foundIntent)

        val networkNodesLive = localAvailabilityManager.networkNodesLiveData
        val latch = CountDownLatch(1)
        runBlocking(Dispatchers.Main) {
            networkNodesLive.observeForever {nodeList ->
                latch.takeIf { nodeList.any { it.bluetoothMacAddress ==  "11:22:33:44:55:66"} }?.countDown()
            }
        }

        latch.await(5, TimeUnit.SECONDS)
        Assert.assertTrue("Found device in list after mock discovery",
            networkNodesLive?.value?.any { it.bluetoothMacAddress == "11:22:33:44:55:66" } == true)
    }




}