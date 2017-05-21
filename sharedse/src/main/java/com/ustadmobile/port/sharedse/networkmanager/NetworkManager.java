package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.p2p.P2PManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static com.ustadmobile.core.buildconfig.CoreBuildConfig.NETWORK_SERVICE_NAME;

/**
 * Created by kileha3 on 08/05/2017.
 */

public abstract class NetworkManager implements P2PManager,NetworkManagerTaskListener {

    public static final int QUEUE_ENTRY_STATUS=0;
    public static final int QUEUE_ENTRY_ACQUISITION=1;
    public static final int NOTIFICATION_TYPE_SERVER=0;
    public static final int NOTIFICATION_TYPE_ACQUISITION=1;
    public static final int DOWNLOAD_SOURCE_CLOUD=1;
    public static final int DOWNLOAD_SOURCE_PEER_SAME_NETWORK=2;
    public static final int DOWNLOAD_SOURCE_PEER_DIFFERENT_NETWORK=3;
    public BluetoothServer bluetoothServer;

    public static final String SD_TXT_KEY_IP_ADDR = "a";

    public static final String SD_TXT_KEY_BT_MAC = "b";

    private Object mContext;

    private Vector<NetworkNode> knownNetworkNodes=new Vector<>();

    private Vector<NetworkTask>[] tasksQueues = new Vector[] {
        new Vector<>(), new Vector<>()
    };


    private Vector<NetworkManagerListener> networkManagerListeners = new Vector<>();

    private Map<String,List<EntryCheckResponse>> entryResponses =new HashMap<>();

    private NetworkTask[] currentTasks = new NetworkTask[2];

    public NetworkManager() {
    }

    public abstract void startSuperNode();

    public abstract void stopSuperNode();

    public abstract boolean isSuperNodeEnabled();

    public void init(Object mContext) {
        this.mContext = mContext;
    }

    public  abstract boolean isBluetoothEnabled();


    public abstract BluetoothServer getBluetoothServer();

    public abstract boolean isWiFiEnabled();

    public boolean isFileAvailable(String entryId){
        for(EntryCheckResponse response:entryResponses.get(entryId)){
            if(response.isFileAvailable()){
                return true;
            }
        }
        return false;
    }

    public List<String> requestFileStatus(List<String> entryIds,Object mContext,List<NetworkNode> nodeList){
        EntryStatusTask task = new EntryStatusTask(entryIds,nodeList,this);
        task.setTaskType(QUEUE_ENTRY_STATUS);
        queueTask(task);
        return entryIds;
    }


    public UstadJSOPDSFeed requestAcquisition(UstadJSOPDSFeed feed,Object mContext){
        AcquisitionTask task=new AcquisitionTask(feed,this);
        task.setTaskType(QUEUE_ENTRY_ACQUISITION);
        queueTask(task);
        return feed;
    }


    public NetworkTask queueTask(NetworkTask task){
        tasksQueues[task.getTaskType()].add(task);
        checkTaskQueue(task.getTaskType());

        return task;
    }

    public synchronized void checkTaskQueue(int queueType){
        if(!tasksQueues[queueType].isEmpty() && currentTasks[queueType] == null) {
            currentTasks[queueType] = tasksQueues[queueType].remove(0);
            currentTasks[queueType].setNetworkManager(this);
            currentTasks[queueType].setNetworkTaskListener(this);
            currentTasks[queueType].start();
        }
    }

    public void handleWifiDirectSdTxtRecordsAvailable(String serviceFullDomain,String senderMacAddr, HashMap<String, String> txtRecords) {
        if(serviceFullDomain.contains(NETWORK_SERVICE_NAME.toLowerCase())){
            String ipAddr = txtRecords.get(SD_TXT_KEY_IP_ADDR);
            String btAddr = txtRecords.get(SD_TXT_KEY_BT_MAC);

            NetworkNode node = null;
            boolean newNode = true;
            if(ipAddr != null) {
                node = getNodeByIpAddress(ipAddr);
                newNode = (node == null);
            }


            if(node == null) {
                node = new NetworkNode(senderMacAddr);
                node.setDeviceIpAddress(ipAddr);
            }

            node.setDeviceBluetoothMacAddress(btAddr);
            node.setDeviceWifiDirectMacAddress(senderMacAddr);

            if(newNode)
                knownNetworkNodes.add(node);
                fireNetworkNodeDiscovered(node);
        }
    }

    /**
     * Get a known network node by IP address
     *
     * @param ipAddr
     * @return
     */
    public NetworkNode getNodeByIpAddress(String ipAddr) {
        synchronized (knownNetworkNodes) {
            String nodeIp;
            for(NetworkNode node : knownNetworkNodes) {
                nodeIp = node.getDeviceIpAddress();
                if(nodeIp != null && nodeIp.equals(ipAddr))
                    return node;
            }
        }

        return null;
    }

    public NetworkNode getNodeByBluetoothAddr(String bluetoothAddr) {
        synchronized (knownNetworkNodes) {
            String nodeBtAddr;
            for(NetworkNode node : knownNetworkNodes) {
                nodeBtAddr = node.getDeviceBluetoothMacAddress();
                if(nodeBtAddr != null && nodeBtAddr.equals(bluetoothAddr))
                    return node;
            }
        }

        return null;
    }

    public void addNetworkManagerListener(NetworkManagerListener listener){
        networkManagerListeners.add(listener);
    }

    public void removeNetworkManagerListener(NetworkManagerListener listener){
        if(listener!=null){
            networkManagerListeners.remove(listener);
        }
    }

    public abstract void connectBluetooth(String deviceAddress,BluetoothConnectionHandler handler);

    public void handleEntriesStatusUpdate(NetworkNode node, List<String> fileIds,List<Boolean> status) {
        List<EntryCheckResponse> responseList;
        EntryCheckResponse checkResponse;
        long timeNow = Calendar.getInstance().getTimeInMillis();
        for (int position=0;position<fileIds.size();position++){
            checkResponse = getEntryResponse(fileIds.get(position), node);

            responseList=getEntryResponses().get(fileIds.get(position));
            if(responseList==null){
                responseList=new ArrayList<>();
                entryResponses.put(fileIds.get(position),responseList);
            }

            if(checkResponse == null) {
                checkResponse = new EntryCheckResponse(node);
                responseList.add(checkResponse);
            }

            checkResponse.setFileAvailable(status.get(position));
            checkResponse.setLastChecked(timeNow);
        }

        fireFileStatusCheckInformationAvailable(fileIds);
    }

    public EntryCheckResponse getEntryResponse(String fileId, NetworkNode node) {
        List<EntryCheckResponse> responseList = getEntryResponses().get(fileId);
        if(responseList == null)
            return null;

        for(int responseNum = 0; responseNum < responseList.size(); responseNum++) {
            if(responseList.get(responseNum).getNetworkNode().equals(node)) {
                return responseList.get(responseNum);
            }
        }

        return null;
    }

    public void handleFileAcquisitionInformationAvailable(String entryId,long downloadId,int downloadSource){
        fireFileAcquisitionInformationAvailable(entryId,downloadId,downloadSource);
    }


    protected void fireFileStatusCheckInformationAvailable(List<String> fileIds) {
        synchronized (networkManagerListeners) {
            for(NetworkManagerListener listener : networkManagerListeners){
                listener.fileStatusCheckInformationAvailable(fileIds);
            }
        }
    }


    protected void fireFileAcquisitionInformationAvailable(String entryId,long downloadId,int downloadSource) {
        synchronized (networkManagerListeners) {
            for(NetworkManagerListener listener : networkManagerListeners){
                listener.fileAcquisitionInformationAvailable(entryId,downloadId,downloadSource);
            }
        }
    }

    protected void fireNetworkNodeDiscovered(NetworkNode node) {
        synchronized (networkManagerListeners) {
            for(NetworkManagerListener listener : networkManagerListeners){
                listener.networkNodeDiscovered(node);
            }
        }
    }

    protected void fireEntryStatusCheckCompleted(NetworkTask task){
        synchronized (networkManagerListeners) {
            for(NetworkManagerListener listener : networkManagerListeners){
                listener.entryStatusCheckCompleted(task);
            }
        }
    }
    public abstract int addNotification(int notificationType,String title,String message);

    public abstract void updateNotification(int notificationId,int progress,String title,String message);

    public abstract void removeNotification(int notificationId);

    @Override
    public void handleTaskCompleted(NetworkTask task) {
        if(task == currentTasks[task.getTaskType()]) {
            currentTasks[task.getTaskType()] = null;
            checkTaskQueue(task.getTaskType());
        }

        fireEntryStatusCheckCompleted(task);
    }

    public List<NetworkNode> getKnownNodes() {
        return knownNetworkNodes;
    }

    public Map<String,List<EntryCheckResponse>> getEntryResponses(){
        return entryResponses;
    }

    public Object getContext() {
        return mContext;
    }

    public abstract String getDeviceIPAddress();



}
