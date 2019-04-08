package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.db.dao.ContainerDao;
import com.ustadmobile.core.impl.UmAccountManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil.bleMessageBytesToLong;
import static com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil.bleMessageLongToBytes;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_REQUEST;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_RESPONSE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_CREATION_RESPONSE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_REQUEST;

/**
 * This is an abstract class which is used to implement platform specific BleGattServer.
 * It is responsible for processing the message received from peer devices and return
 * the response to the respective peer device.
 *
 * <p>
 * <b>Note: Operation Flow</b>
 * When server device receives a message, it calls {@link BleGattServer#handleRequest}
 * and handle it according to the request type. If the Request type will be about
 * checking entry statuses, it will check the status from the database otherwise
 * it will be for Wifi direct group creation.
 *
 * @author kileha3
 */
public abstract class BleGattServer {

    private NetworkManagerBle networkManager;

    private Object context;

    public BleGattServer (Object context){
        this.context = context;
    }

    /**
     * Set NetworkManagerBle instance
     * @param networkManager Instance of NetworkManagerBle
     */
    public void setNetworkManager(NetworkManagerBle networkManager) {
        this.networkManager = networkManager;
    }

    public void setContext(Object context) {
        this.context = context;
    }

    /**
     * Default constructor used by Mockito when spying this class
     */
    protected BleGattServer(){}

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
                List<Long> entryStatusResponse = new ArrayList<>();

                ContainerDao containerDao = UmAccountManager.INSTANCE.getRepositoryForActiveAccount(context)
                        .getContainerDao();
                for(long containerUid: bleMessageBytesToLong(requestReceived.getPayload())){

                    Long foundLocalContainerUid =
                            containerDao.findLocalAvailabilityByUid(containerUid);
                    entryStatusResponse.add(foundLocalContainerUid != null
                            && foundLocalContainerUid != 0 ? 1L: 0L);
                }
                return new BleMessage(ENTRY_STATUS_RESPONSE, (byte)42,
                        bleMessageLongToBytes(entryStatusResponse));

            case WIFI_GROUP_REQUEST:
                WiFiDirectGroupBle group = networkManager.awaitWifiDirectGroupReady(5000,
                        TimeUnit.MILLISECONDS);
                return new BleMessage(WIFI_GROUP_CREATION_RESPONSE, (byte)42,
                        networkManager.getWifiGroupInfoAsBytes(group));
            default: return null;
        }
    }

}