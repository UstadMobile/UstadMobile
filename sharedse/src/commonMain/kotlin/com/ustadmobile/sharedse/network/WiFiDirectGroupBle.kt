package com.ustadmobile.sharedse.network

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
        val ipInt = buffer.getInt()
        port = buffer.getChar().toInt()
        ipAddress = NetworkManagerBleCommon.convertIpAddressToString(ipInt)
        val ssidAndPassphraseArr = ByteArray(buffer.remaining())
        buffer.get(ssidAndPassphraseArr, 0, buffer.remaining())
        val ssidAndPassphrase = stringFromUtf8Bytes(ssidAndPassphraseArr).split("|")
        ssid = ssidAndPassphrase[0]
        passphrase = ssidAndPassphrase[1]
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
