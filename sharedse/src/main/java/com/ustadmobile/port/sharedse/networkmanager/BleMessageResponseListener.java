package com.ustadmobile.port.sharedse.networkmanager;

/**
 * Interface to listen for the response from the server device.
 */
public interface BleMessageResponseListener {
    /**
     * Invoked when new response is received, after collecting the packages into a message.
     * @param sourceDeviceAddress Server device bluetooth MAC address
     * @param response Message received as a response from the server device (if successful)
     * @param error
     */
    void onResponseReceived(String sourceDeviceAddress, BleMessage response, Exception error);
}
