package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.EntryStatusResponseDao;
import com.ustadmobile.core.db.dao.NetworkNodeDao;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.lib.db.entities.EntryStatusResponse;
import com.ustadmobile.lib.db.entities.NetworkNode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil.bleMessageBytesToLong;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_RESPONSE;

/**
 * This is an abstract class which is used to implement platform specific BleEntryStatus
 *
 * @see BleMessageResponseListener
 * @see Runnable
 * @author kileha3
 */
public abstract class BleEntryStatusTask implements Runnable,BleMessageResponseListener {

    /**
     * Message object which carries list of entry Ids to be checked for availability.
     */
    protected BleMessage message;

    protected NetworkNode networkNode;

    protected Object context;

    private List<Long> entryUidsToCheck;

    private BleMessageResponseListener responseListener;

    private NetworkManagerBle managerBle;

    /**
     * Constructor which will be used when creating new instance of a task
     * @param context Application context.
     * @param entryUidsToCheck List of Id's to be checked for availability from a peer device.
     * @param peerToCheck Peer device for those entries to be checked from.
     *
     */
    public BleEntryStatusTask(Object context, NetworkManagerBle managerBle, List<Long> entryUidsToCheck,
                              NetworkNode peerToCheck) {
        this.networkNode = peerToCheck;
        this.context = context;
        this.entryUidsToCheck = entryUidsToCheck;
        this.managerBle = managerBle;
    }

    /**
     * Constructor which will be used when creaating new instance for WiFi direct group creation request
     * @param context Application context
     * @param message Message to be sent to the peer device (Carried WiFi group creation request)
     * @param peerToSendMessageTo Peer to send message to
     * @param responseListener Message response listener object
     */
    public BleEntryStatusTask(Object context , NetworkManagerBle managerBle, BleMessage message, NetworkNode peerToSendMessageTo,
                              BleMessageResponseListener responseListener){
        this.networkNode = peerToSendMessageTo;
        this.context = context;
        this.message = message;
        this.managerBle = managerBle;
        this.responseListener = responseListener;
    }

    /**
     * Default constructor for Mockito to spy on this class
     */
    protected BleEntryStatusTask(){}

    /**
     * Set content, for test purpose
     * @param context Mocked context
     */
    protected void setContext(Object context){
        this.context = context;
    }

    /**
     * Set networkManagerBle for testing purpose.
     * @param managerBle NetworkManagerBle object
     */
    void setManagerBle(NetworkManagerBle managerBle){
        this.managerBle = managerBle;
    }

    /**
     * Set list of entry uuids , for test purpose
     * @param entryUidsToCheck List of uuids
     */
    void setEntryUidsToCheck(List<Long> entryUidsToCheck){
        this.entryUidsToCheck = entryUidsToCheck;
    }

    /**
     * Handle response from the entry status task
     * @param sourceDeviceAddress Server device bluetooth MAC address
     * @param response Message received as a response from the server device.
     */
    @Override
    public void onResponseReceived(String sourceDeviceAddress,BleMessage response) {

        byte responseRequestType = response.getRequestType();

        switch (responseRequestType){

            case  ENTRY_STATUS_RESPONSE:
                UmAppDatabase umAppDatabase = UmAppDatabase.getInstance(context);
                EntryStatusResponseDao entryStatusResponseDao = umAppDatabase.getEntryStatusResponseDao();
                NetworkNodeDao networkNodeDao = umAppDatabase.getNetworkNodeDao();

                long networkNodeId = networkNodeDao.findNodeByBluetoothAddress(sourceDeviceAddress).getNodeId();
                List<EntryStatusResponse> entryFileStatusResponseList = new ArrayList<>();
                List<Long> statusCheckResponse = bleMessageBytesToLong(response.getPayload());

                long time = System.currentTimeMillis();
                for(int entryCounter = 0 ; entryCounter < entryUidsToCheck.size(); entryCounter++){
                    long fileEntryUid = entryUidsToCheck.get(entryCounter);
                    boolean isAvailable = statusCheckResponse.get(entryCounter) != 0;

                    EntryStatusResponse entryResponse = new EntryStatusResponse();
                    entryResponse.setErNodeId(networkNodeId);
                    entryResponse.setResponseTime(time);
                    entryResponse.setAvailable(isAvailable);
                    entryResponse.setErContentEntryFileUid(fileEntryUid);

                    entryFileStatusResponseList.add(entryResponse);

                }
                Long [] rowCount = entryStatusResponseDao.insert(entryFileStatusResponseList);
                if(rowCount.length == entryFileStatusResponseList.size()){
                    UstadMobileSystemImpl.l(UMLog.DEBUG,697,
                            rowCount.length+" responses saved to the db from "+ sourceDeviceAddress);
                }

                managerBle.handleLocalAvailabilityResponsesReceived(entryFileStatusResponseList);
                break;
        }

        if(responseListener != null){
            responseListener.onResponseReceived(sourceDeviceAddress,response);
        }

    }

    /**
     * Get BleMessage instance
     * @return Created BleMessage
     */
    public BleMessage getMessage() {
        return message;
    }

    /**
     * Get NetworkNode instance
     * @return Created NetworkNode
     */
    public NetworkNode getNetworkNode() {
        return networkNode;
    }
}
