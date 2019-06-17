package com.ustadmobile.sharedse.network

/**
 * Class which defines WiFi Direct group in a cross platform way.
 *
 * @author kileha3
 */

open class WiFiDirectGroupBle
/**
 * Create Wi-Fi Direct group
 * @param ssid Group SSID
 * @param passphrase Group passphrase
 */
(
        /**
         * @return WiFi direct group SSID
         */
        val ssid: String,
        /**@return Wifi direct group passphrase
         */
        val passphrase: String) {

    /**
     * @return Node listening port
     */
    var port: Int? = 0

    /**
     * @return Node ip address
     */
    var ipAddress: String? = ""

}
