package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.lib.db.entities.ContentEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil.bleMessageBytesToLong;
import static com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil.bleMessageLongToBytes;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_REQUEST;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_RESPONSE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_INFO_SEPARATOR;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_REQUEST;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_CREATION_RESPONSE;

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
public abstract class BleGattServer implements WiFiDirectGroupListenerBle{

    private NetworkManagerBle networkManager;

    private final Object p2pGroupCreationLock = new Object();

    private long GROUP_CREATION_TIMEOUT = TimeUnit.SECONDS.toMillis(2);

    private String message = null;

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

    /**
     * Default constructor used by Mockito when spying this class
     */
    public BleGattServer(){}

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
                UmAppDatabase database = UmAppDatabase.getInstance(context);
                ContentEntryDao contentEntryDao = database.getContentEntryDao();
                List<Long> entryStatusResponse = new ArrayList<>();
                for(long entryUuid: bleMessageBytesToLong(requestReceived.getPayload())){
                    ContentEntry contentEntry = contentEntryDao.findByEntryId(entryUuid);
                    entryStatusResponse.add(contentEntry == null ?
                            0L: contentEntry.getLastUpdateTime());
                }
                return new BleMessage(ENTRY_STATUS_RESPONSE,
                        bleMessageLongToBytes(entryStatusResponse));

            case WIFI_GROUP_REQUEST:
                synchronized (p2pGroupCreationLock){
                    try{
                        networkManager.handleWiFiDirectGroupChangeRequest(this);
                        networkManager.createWifiDirectGroup();
                        p2pGroupCreationLock.wait(GROUP_CREATION_TIMEOUT);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        p2pGroupCreationLock.notify();
                    }
                }
                return new BleMessage(WIFI_GROUP_CREATION_RESPONSE,message.getBytes());
                default: return null;
        }
    }

    @Override
    public void groupCreated(WiFiDirectGroupBle group, Exception err) {
        synchronized (p2pGroupCreationLock){
            this.message = group.getSsid()+WIFI_GROUP_INFO_SEPARATOR+group.getPassphrase();
            p2pGroupCreationLock.notify();
        }
    }

    @Override
    public void groupRemoved(boolean successful, Exception err) {

    }
}
