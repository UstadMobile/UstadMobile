package com.ustadmobile.port.sharedse.networkmanager;

import java.util.List;

import static com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil.bleMessageBytesToLong;
import static com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil.bleMessageLongToBytes;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_REQUEST;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_RESPONSE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_CREATION_REQUEST;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_CREATION_RESPONSE;

/**
 * This is an abstract class which is used to implement platform specific BleGattServer
 *
 * @author kileha3
 */
public abstract class BleGattServer {

    /**
     * Handle request from peer device
     * @param requestReceived Message received from the peer device
     * @return Newly constructed message as a response to the peer device
     *
     * @see BleMessage
     */
    public BleMessage handleRequest(BleMessage requestReceived) {
        byte requestType = requestReceived.getRequestType();
        switch (requestType){
            case ENTRY_STATUS_REQUEST:
                List<Long> requestedEntries = bleMessageBytesToLong(requestReceived.getPayload());

                return new BleMessage(ENTRY_STATUS_RESPONSE,bleMessageLongToBytes(requestedEntries),20);

            case WIFI_GROUP_CREATION_REQUEST:
                return new BleMessage(WIFI_GROUP_CREATION_RESPONSE,"CreatedWiFiGroup".getBytes(),20);
                default: return null;
        }
    }
}
