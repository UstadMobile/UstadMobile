package com.ustadmobile.port.sharedse.networkmanager;

import java.util.List;

import static com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil.bleMessageBytesToLong;
import static com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil.bleMessageLongToBytes;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_REQUEST;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_RESPONSE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_CREATION_REQUEST;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_CREATION_RESPONSE;

/**
 * <h1>BleGattServer</h1>
 *
 */
public abstract class BleGattServer {

    public BleMessage handleRequest(BleMessage requestReceived) {
        byte requestType = requestReceived.getRequestType();
        switch (requestType){
            case ENTRY_STATUS_REQUEST:
                List<Long> requestedEntries = bleMessageBytesToLong(requestReceived.getPayload());

                return new BleMessage(ENTRY_STATUS_RESPONSE,bleMessageLongToBytes(requestedEntries),20);

            case WIFI_GROUP_CREATION_REQUEST:
                return new BleMessage(WIFI_GROUP_CREATION_RESPONSE,"".getBytes(),20);
                default: return null;
        }
    }
}
