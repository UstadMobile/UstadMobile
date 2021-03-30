package com.ustadmobile.sharedse.network

import com.ustadmobile.sharedse.io.ByteBufferSe

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
    constructor(ssid: String,passphrase: String) {
        this.ssid = ssid
        this.passphrase = passphrase
    }

    /**
     * Constructor used to create WiFiDirectGroupBle from BleMessage payload
     * @param byteArr message payload
     */
    constructor(byteArr: ByteArray) {
        val buffer = ByteBufferSe.wrap(byteArr)
        val ipInt = buffer.getInt()
        port = buffer.getChar().toInt()
        ipAddress = NetworkManagerBleCommon.convertIpAddressToString(ipInt)
        val ssidAndPassphraseArr = ByteArray(buffer.remaining())
        buffer.get(ssidAndPassphraseArr, 0, buffer.remaining())
        val ssidAndPassphrase = ssidAndPassphraseArr.decodeToString().split("|")
        ssid = ssidAndPassphrase[0]
        passphrase = ssidAndPassphrase[1]
    }

    fun toBytes(): ByteArray {
        val string = ("$ssid|$passphrase")
        val buffer = ByteBufferSe.allocate(string.encodeToByteArray().size + 4 + 2)
                .putInt(NetworkManagerBleCommon.convertIpAddressToInteger(ipAddress!!))
                .putChar(port!!.toChar())
                .put(string.encodeToByteArray())

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

    override fun toString(): String {
        return "WiFiDirectGroupBle ssid='$ssid' passphrase='$passphrase' ownerIp=$ipAddress ownerPort=$port"
    }
}
