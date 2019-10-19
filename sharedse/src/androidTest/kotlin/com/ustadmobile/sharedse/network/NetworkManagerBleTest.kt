package com.ustadmobile.sharedse.network

import androidx.test.platform.app.InstrumentationRegistry
import com.nhaarman.mockitokotlin2.spy
import org.junit.Test

class NetworkManagerBleTest {

    @Test
    fun givenWiFiNetworkExists_whenConnectToWifiCalled_shouldScanThenConnect() {
        val realNetworkManager = NetworkManagerBle(InstrumentationRegistry.getInstrumentation().targetContext)
        val networkManagerSpy = spy(realNetworkManager)
        networkManagerSpy.onCreate()



    }

}