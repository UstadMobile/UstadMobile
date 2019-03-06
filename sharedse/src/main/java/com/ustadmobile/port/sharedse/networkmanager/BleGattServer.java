package com.ustadmobile.port.sharedse.networkmanager;

import com.google.gson.Gson;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContainerDao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
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
public abstract class BleGattServer implements WiFiDirectGroupListenerBle{

    private NetworkManagerBle networkManager;

    private CountDownLatch mLatch = new CountDownLatch(1);

    private String message = null;

    private Object context;

    static final int GROUP_CREATION_TIMEOUT = 5;

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
                ContainerDao containerDao =
                        UmAppDatabase.getInstance(context).getContainerDao();
                List<Long> entryStatusResponse = new ArrayList<>();

                for(long containerUid: bleMessageBytesToLong(requestReceived.getPayload())){

                    long foundLocalContainerUid =
                            containerDao.findLocalAvailabilityByUid(containerUid);
                    entryStatusResponse.add(foundLocalContainerUid != 0 ? 1L: 0L);
                }
                return new BleMessage(ENTRY_STATUS_RESPONSE,
                        bleMessageLongToBytes(entryStatusResponse));

            case WIFI_GROUP_REQUEST:
                networkManager.handleWiFiDirectGroupChangeRequest(this);
                networkManager.createWifiDirectGroup();
                try { mLatch.await(GROUP_CREATION_TIMEOUT, TimeUnit.SECONDS); }
                catch(InterruptedException e) {
                    mLatch.countDown();
                    e.printStackTrace();
                }
                return new BleMessage(WIFI_GROUP_CREATION_RESPONSE,message.getBytes());
            default: return null;
        }
    }

    @Override
    public void groupCreated(WiFiDirectGroupBle group, Exception err) {
        group.setEndpoint("http://192.168.49.1:"+ networkManager.getHttpd().getListeningPort()+"/");
        this.message = new Gson().toJson(group);
        mLatch.countDown();
    }

    @Override
    public void groupRemoved(boolean successful, Exception err) {

    }
}