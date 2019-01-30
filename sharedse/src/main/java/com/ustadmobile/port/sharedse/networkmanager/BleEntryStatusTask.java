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
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_CREATION_RESPONSE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_INFO_SEPARATOR;

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
    public BleMessage message;

    private NetworkNode networkNode;

    private Object context;

    private List<Long> entryUidsToCheck;

    private NetworkManagerBle networkManagerBle;

    private static final int ssidInfoIndex = 0;

    private static final int passphraseInfoIndex = 1;

    private BleMessageResponseListener responseListener;

    /**
     * Constructor which will be used when creating new instance of a task
     * @param context Application context.
     * @param entryUidsToCheck List of Id's to be checked for availability from a peer device.
     * @param peerToCheck Peer device for those entries to be checked from.
     */
    public BleEntryStatusTask(Object context,List<Long> entryUidsToCheck, NetworkNode peerToCheck) {
        this.networkNode = peerToCheck;
        this.context = context;
        this.entryUidsToCheck = entryUidsToCheck;
    }

    /**
     * Constructor which will be used when creaating new instance for WiFi direct group creation request
     * @param context Application context
     * @param message Message to be sent to the peer device (Carried WiFi group creation request)
     * @param peerToSendMessageTo Peer to send message to
     * @param responseListener Message response listener object
     */
    public BleEntryStatusTask(Object context , BleMessage message, NetworkNode peerToSendMessageTo,
                              BleMessageResponseListener responseListener){
        this.networkNode = peerToSendMessageTo;
        this.context = context;
        this.message = message;
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

                int networkNodeId = networkNodeDao.findNodeByBluetoothAddress(sourceDeviceAddress).getNodeId();
                long currentTimeStamp = Calendar.getInstance().getTimeInMillis();
                List<EntryStatusResponse> entryStatusResponses = new ArrayList<>();
                List<Long> statusCheckResponse = bleMessageBytesToLong(response.getPayload());

                for(int entryCounter = 0 ; entryCounter < entryUidsToCheck.size(); entryCounter++){
                    long entryUuid = entryUidsToCheck.get(entryCounter);
                    long entryResponse = statusCheckResponse.get(entryCounter);
                    EntryStatusResponse nodeResponse =
                            entryStatusResponseDao.findByEntryIdAndNetworkNode(entryUuid,networkNodeId);
                    EntryStatusResponse statusResponse = new EntryStatusResponse(entryUuid, networkNodeId,
                            nodeResponse == null ? currentTimeStamp:nodeResponse.getResponseTime()
                            ,entryResponse, entryResponse != 0);
                    if(nodeResponse!= null){
                        statusResponse.setId(statusResponse.getId());
                    }
                    entryStatusResponses.add(statusResponse);

                }
                Long [] rowCount = entryStatusResponseDao.insert(entryStatusResponses);
                if(rowCount.length == entryStatusResponses.size()){
                    UstadMobileSystemImpl.l(UMLog.DEBUG,697,
                            rowCount.length+" responses saved to the db");
                }

                break;

            case WIFI_GROUP_CREATION_RESPONSE:

                String [] groupInfo = new String(response.getPayload())
                        .split(WIFI_GROUP_INFO_SEPARATOR);
                networkManagerBle.connectToWiFi(groupInfo[ssidInfoIndex],
                        groupInfo[passphraseInfoIndex]);
                break;
        }

        if(responseListener != null){
            responseListener.onResponseReceived(sourceDeviceAddress,response);
        }

    }

    public void setNetworkManagerBle(NetworkManagerBle networkManagerBle){
        this.networkManagerBle = networkManagerBle;
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
