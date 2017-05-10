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
    private Object mContext;
    private final Object bluetoothLock=new Object();


    public Vector<NetworkNode> networkNodesMap=new Vector<>();

    public Vector<NetworkNode>  knownNetworkNodes=new Vector<>();

    public Vector<AcquisitionTask> acquisitionTaskQueue=new Vector<>();

    public Vector<EntryStatusTask> statusTaskQueue=new Vector<>();

    public Vector<NetworkManagerListener> networkManagerListeners=new Vector<>();

    public Map<String,List<EntryCheckResponse>> entryResponses =new HashMap<>();


    public EntryStatusTask currentEntryStatusTask;

    public AcquisitionTask currentEntryAcquisitionTask;


    public abstract void startSuperNode();

    public abstract void stopSuperNode();

    public abstract boolean isSuperNodeEnabled();

    public abstract void init(Object mContext,String serviceName);

    public  abstract boolean isBluetoothEnabled();

    public abstract void startBluetoothServer();

    public abstract void stopBluetoothServer();

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

    public String[] requestFileStatus(String [] entryIds,Object mContext){
        createFileStatusTask(entryIds,mContext);
        return entryIds;
    }


    public UstadJSOPDSFeed requestAcquisition(UstadJSOPDSFeed feed,Object mContext){
        createAcquisitionTask(feed,mContext);
        return feed;
    }


    public abstract NetworkTask createFileStatusTask(String entryIds[],Object mContext);

    public abstract NetworkTask createAcquisitionTask(UstadJSOPDSFeed feed,Object mContext);



    public NetworkTask queueTask(NetworkTask task){

        if(task.getTaskType()==QUEUE_ENTRY_ACQUISITION){

            AcquisitionTask acquisitionTask=(AcquisitionTask)task;
            if(!acquisitionTaskQueue.contains(acquisitionTask)){
                acquisitionTaskQueue.add(acquisitionTask);
            }

            if(currentEntryAcquisitionTask==null || acquisitionTaskQueue.size()==1){
                checkTaskQueue(acquisitionTask.getTaskType());
            }

        }else if(task.getTaskType()==QUEUE_ENTRY_STATUS){
            EntryStatusTask entryStatusTask=(EntryStatusTask)task;
            statusTaskQueue.add(entryStatusTask);
        }

        return task;
    }

    public synchronized void checkTaskQueue(int queueType){

        if(queueType==QUEUE_ENTRY_ACQUISITION){

            if(!acquisitionTaskQueue.isEmpty()){
                currentEntryAcquisitionTask =acquisitionTaskQueue.remove(0);
                currentEntryAcquisitionTask.setNetworkManager(this);
                currentEntryAcquisitionTask.setNetworkTaskListener(this);
                currentEntryAcquisitionTask.start();
            }
        }else if(queueType==QUEUE_ENTRY_STATUS){

           if(!statusTaskQueue.isEmpty()){
               currentEntryStatusTask =statusTaskQueue.remove(0);
               currentEntryStatusTask.setNetworkManager(this);
               currentEntryStatusTask.setNetworkTaskListener(this);
               currentEntryStatusTask.start();
           }
        }
    }

    public void handleNodeDiscovered(NetworkNode node){
        networkNodesMap.add(node);
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

        if(task.getTaskType()==QUEUE_ENTRY_ACQUISITION){

            if(task!=currentEntryAcquisitionTask){
                return;
            }
            currentEntryAcquisitionTask=null;

        }else if(task.getTaskType()==QUEUE_ENTRY_STATUS){

            if(task!=currentEntryStatusTask){
                return;
            }
        }

        checkTaskQueue(task.getTaskType());
    }



}
