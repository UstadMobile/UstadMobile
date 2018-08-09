package com.ustadmobile.port.android.netwokmanager;

import com.ustadmobile.port.sharedse.networkmanager.BleMessage;

/**
 * <h1>BleMessageResponseListener</h1>
 *
 * Interface to listen for the response from the server device.
 */
public interface BleMessageResponseListener {
    /**
     * Invoked when new response is received, after collecting the packages into a message.
     * @param response Message received as a response from the server device.
     */
    void onResponseReceived(BleMessage response);
}
