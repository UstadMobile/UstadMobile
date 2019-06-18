package com.ustadmobile.sharedse.network

import org.junit.Assert
import org.junit.Test

class WifiDirectGroupBleTest {

    @Test
    fun test() {
        val wifi = WiFiDirectGroupBle("23455353", "helloworld")
        wifi.ipAddress = "126.0.0.0"
        wifi.port = 25

        val bytes = wifi.toBytes()
        val newWifi = WiFiDirectGroupBle(bytes)

        Assert.assertTrue("ssid match", wifi.ssid == newWifi.ssid)
        Assert.assertTrue("pass match", wifi.passphrase == newWifi.passphrase)
        Assert.assertTrue("port match", wifi.port == newWifi.port)
        Assert.assertTrue("ip match", wifi.ipAddress == newWifi.ipAddress)

    }

}