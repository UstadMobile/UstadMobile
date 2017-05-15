package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.p2p.P2PManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Created by kileha3 on 08/05/2017.
 */

public abstract class NetworkManager implements P2PManager,NetworkManagerTaskListener {

    public static final int QUEUE_ENTRY_STATUS=0;
    public static final int QUEUE_ENTRY_ACQUISITION=1;
    public static final int NOTIFICATION_TYPE_SERVER=0;
    public static final int NOTIFICATION_TYPE_ACQUISITION=1;
    public BluetoothServer bluetoothServer;

    public static final String SD_TXT_KEY_IP_ADDR = "a";

    public static final String SD_TXT_KEY_BT_MAC = "b";

    private Object mContext;

    private final Object bluetoothLock=new Object();



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

    public void init(Object mContext,String serviceName) {
        this.mContext = mContext;


    }

    public  abstract boolean isBluetoothEnabled();


    public abstract BluetoothServer getBluetoothServer();

    public Object acquireBluetoothLock(Object bluetoothLock){
        synchronized (bluetoothLock){

        }

        return bluetoothLock;
    }

    public void releaseBluetoothLock(Object bluetoothLock){
        if(bluetoothLock!=null){

        }
    }

    public abstract boolean isWiFiEnabled();

    public List<String> requestFileStatus(List<String> entryIds,Object mContext){
        EntryStatusTask task = new EntryStatusTask(entryIds);
        queueTask(task);
        return entryIds;
    }


    public UstadJSOPDSFeed requestAcquisition(UstadJSOPDSFeed feed,Object mContext){

        return feed;
    }


    public NetworkTask queueTask(NetworkTask task){
        tasksQueues[task.getQueueId()].add(task);
        checkTaskQueue(task.getQueueId());

        return task;
    }

    public synchronized void checkTaskQueue(int queueType){
        if(!tasksQueues[queueType].isEmpty() && currentTasks[queueType] == null) {
            currentTasks[queueType] = tasksQueues[queueType].get(0);
            currentTasks[queueType].setNetworkManager(this);
            currentTasks[queueType].setNetworkTaskListener(this);
            currentTasks[queueType].start();
        }
    }

    protected void handleWifiDirectSdTxtRecordsAvailable(String serviceName, String senderMacAddr, HashMap<String, String> txtRecords) {
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



    public void addNetworkManagerListener(NetworkManagerListener listener){
        networkManagerListeners.add(listener);
    }

    public void removeNetworkManagerListener(NetworkManagerListener listener){
        if(listener!=null){
            networkManagerListeners.remove(listener);
        }
    }

    public abstract void connectBluetooth(String deviceAddress,BluetoothConnectionHandler handler);

    public abstract void handleEntriesStatusUpdate(NetworkNode node, String fileIds[],boolean [] status);

    public abstract int addNotification(int notificationType,String title,String message);

    public abstract void updateNotification(int notificationId,int progress,String title,String message);

    public abstract void removeNotification(int notificationId);

    @Override
    public void handleTaskCompleted(NetworkTask task) {
        if(task == currentTasks[task.getQueueId()]) {
            //otherwise - that'd be weird...
            currentTasks[task.getQueueId()] = null;
            checkTaskQueue(task.getQueueId());
        }
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



}
