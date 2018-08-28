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
public abstract class BleGattServer implements WiFiDirectGroupListenerBle{

    /**
     * Instance of a network manager which used for platform specific operations.
     */
    public NetworkManagerBle networkManager;

    private final Object p2pGroupCreationLock = new Object();

    private String message = null;
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
                synchronized (p2pGroupCreationLock){
                    try{
                        p2pGroupCreationLock.wait();
                        networkManager.createWifiDirectGroup(this);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return new BleMessage(WIFI_GROUP_CREATION_RESPONSE,message.getBytes(),20);
                default: return null;
        }
    }

    @Override
    public void groupCreated(WiFiDirectGroupBle group, Exception err) {
        synchronized (p2pGroupCreationLock){
            this.message = group.getPassphrase()+","+group.getPassphrase();
            p2pGroupCreationLock.notify();
        }
    }

    @Override
    public void groupRemoved(boolean successful, Exception err) {

    }
}
