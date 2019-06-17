package com.ustadmobile.sharedse.network

import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.sharedse.io.ByteBufferSe
import kotlinx.io.ByteBuffer
import kotlinx.serialization.stringFromUtf8Bytes
import kotlinx.serialization.toUtf8Bytes

/**
 * Class which defines WiFi Direct group in a cross platform way.
 *
 * @author kileha3
 */

open class WiFiDirectGroupBle {
    /**
     * Create Wi-Fi Direct group
     * @param ssid Group SSID
     * @param passphrase Group passphrase
     */
    constructor(
            /**
             * @return WiFi direct group SSID
             */
            ssid: String,
            /**@return Wifi direct group passphrase
             */
            passphrase: String) {
        this.ssid = ssid
        this.passphrase = passphrase
    }

    constructor(byteArr: ByteArray) {
        val buffer = ByteBufferSe.wrap(byteArr)
        var originalLength = byteArr.size
        val ip = buffer.getInt()
        val port = buffer.getChar().toInt()
        println("position: " + buffer.position())
        println("remain: " + buffer.remaining())
        val byteArray = ByteArray(buffer.remaining())
        buffer.get(byteArray, buffer.position(), buffer.remaining())
        println("buffer size" + byteArray.size)
        val splitString = stringFromUtf8Bytes(byteArray).split("|")
        val group = WiFiDirectGroupBle(splitString[0], splitString[1])
        group.ipAddress = NetworkManagerBleCommon.convertIpAddressToString(ip)
        group.port = port
        UMLog.l(UMLog.INFO, 699,
                "Group information received with ssid = " + group.ssid)
    }

    fun toBytes(): ByteArray {
        val string = ("$ssid|$passphrase")
        val buffer = ByteBuffer.allocate(string.toUtf8Bytes().size + 4 + 2)
                .putInt(NetworkManagerBleCommon.convertIpAddressToInteger(ipAddress!!))
                .putChar(port!!.toChar())
                .put(string.toUtf8Bytes())

        return buffer.array()
    }

    var ssid: String = ""

    var passphrase: String = ""

    /**
     * @return Node listening port
     */
    var port: Int? = 0

    /**
     * @return Node ip address
     */
    var ipAddress: String? = ""

}
