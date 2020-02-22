package com.ustadmobile.sharedse.network

/**
 * Interface to listen for the response from the server device.
 */
interface BleMessageResponseListener {
    /**
     * Invoked when new response is received, after collecting the packages into a message.
     * @param sourceDeviceAddress Server device bluetooth MAC address
     * @param response Message received as a response from the server device (if successful)
     * @param error
     */
    fun onResponseReceived(sourceDeviceAddress: String, response: BleMessage?, error: Exception?)
}
