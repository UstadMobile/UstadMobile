package com.ustadmobile.port.sharedse.network;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.p2p.P2PManager;

import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by kileha3 on 05/02/2017.
 */

public abstract class NetworkManagerSharedSE implements P2PManager, NetworkTaskListener {


    /**
     * Map of available supernodes mapped as node to index file
     */
    protected HashMap<NetworkNode, UstadJSOPDSFeed> availableIndexes=new HashMap<>();


    /**
     * List of super nodes that we know about around us
     */
    public List<NetworkNode> knownNodes =new ArrayList<>();

    protected Vector<NetworkNodeListener> networkNodeListeners = new Vector<>();
    /**
     * Store the tasks of which will be executed in a FIFO way
     */
    public Vector<P2PTask> taskQueue = new Vector<>();
    public Vector<BluetoothTask> bluetoothQueue=new Vector<>();

    /**
     * Store the download requests of which will be executed in a FIFO way
     */
    public HashMap<Integer,P2PTask> downloadRequests=new HashMap<>();

    public HashMap<String,HashMap<String,String>> availableFiles=new HashMap<>();


    private P2PTask currentTask;
    public BluetoothTask currentBluetoothTask;

    /**
     * TAG to set where to get file from (Cloud,P2P or Local network)
     */
    public int currentDownloadSource =-1;
    public static final int DOWNLOAD_SOURCE_CLOUD =1;
    public static final int DOWNLOAD_SOURCE_P2P =2;
    public static final int DOWNLOAD_SOURCE_LOCAL_NETWORK =3;

    /**
     * Store information of the previously connected WiFi hotspot before connecting to
     * the no prompt network.
     */
    protected String [] currentConnectedNetwork=new String[3];

    /**
     * store current network SSID
     */
    public static final int CURRENT_NETWORK_SSID=0;

    /**
     * Store current network NETID
     */
    public static final int CURRENT_NETWORK_NETID=1;

    /**
     * Sore the current network MAC address
     */
    public static final int CURRENT_NETWORK_GATWAY_ADDRESS=2;
    private int requestDownloadId = 0;
    public static final String  CURRENT_NETWORK_EMPTY_STATE = "empty";
    private static final int START_TASK_INTERVAL=0;

    /**
     * Set if supernode mode is enabled or not (by default this is disabled)
     *
     * @param enabled
     */
    public abstract void setSuperNodeEnabled(Object context, boolean enabled);

    /**
     *  Set if normal client mode is enabled (enabled by default on platforms that support it)
     *
     *  @param enabled
     */
    public abstract void setClientEnabled(Object context, boolean enabled);

    /**
     * check if there is super node around
     * */
    public abstract boolean isSuperNodeAvailable(Object context);

    /**
     * check if files are available locally
     * */
    public abstract String [] areFilesAvailable(Object context, String [] fileIds);

    /**
     * stop the download once has been started
     * */
    public abstract void stopDownload(Object context, int requestId, boolean delete);

    /**
     * request for the download status
     * */
    public abstract int[] getRequestStatus(Object context, int requestId);

    /**
     * request to status of the peer to peer environment
     * */
    public abstract int getStatus(Object context);


    public void addNodeListener(NetworkNodeListener listener) {
        networkNodeListeners.add(listener);
    }

    public void removeNodeListener(NetworkNodeListener listener) {
        networkNodeListeners.remove(listener);
    }

    /**
     * Cross platform logic to execute when a new node is discovered
     *
     * @param node
     */
    public void handleNodeDiscovered(NetworkNode node) {
        fireNodeDiscovered(node);
        BluetoothTask bluetoothTask=makeBlueToothTask(node);
        queueBluetoothTask(bluetoothTask);
    }
    /**
     * Cross platform logic to execute when updating node indexes
     *
     * @param node
     */

    protected void handleNodeIndexUpdated(NetworkNode node, String fileUri) {
        try{
            UstadJSOPDSFeed feed = UstadJSOPDSFeed.loadFromXML(new FileInputStream(fileUri), "UTF-8");//params needed: input stream
            availableIndexes.put(node, feed);
            UstadMobileSystemImpl.l(UMLog.DEBUG, 2, "Available Index "+availableIndexes.size());


        }catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cross platform listener method to execute when a new node is discovered
     *
     * @param node
     */
    protected void fireNodeDiscovered(NetworkNode node) {
        for(NetworkNodeListener listener: networkNodeListeners) {
            listener.nodeDiscovered(node);
        }
    }

    /**
     * Cross platform listener method to execute when a new node is no longer active
     *
     * @param node
     */
    protected void fireNodeGone(NetworkNode node) {
        for(NetworkNodeListener listener: networkNodeListeners) {
            listener.nodeGone(node);
        }
    }

    /**
     * Cross platform queuing logic, it check if there are task to execute and if any assign to the
     * current task and process it by calling currentTask.start() which handles all connection logic
     *
     */
    public synchronized void checkDownloadQueue(){
        if (currentTask == null && !taskQueue.isEmpty()) {
            currentTask = taskQueue.remove(0);
            currentTask.start();
        }
    }


    /**
     * Cross platform queuing logic, it check if there are task to execute and if any assign to the
     * current task and process it by calling bluetoothTask.start() which handles all connection logic
     *
     */

    public synchronized void checkBluetoothQueue(){
        if(!bluetoothQueue.isEmpty()){
            currentBluetoothTask=bluetoothQueue.remove(0);
            currentBluetoothTask.setNetworkTaskListener(this);
            currentBluetoothTask.start();
        }
    }

    /**
     * Cross platform method to execute when download (Task) is completed,
     * it checks for the task type and if the task is of type P2PTask.TYPE_INDEX that means
     * the connection will be terminated and connecting to the next node.
     *
     * @param task
     */

    @Override
    public void taskEnded(P2PTask task) {

        if(task != currentTask) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 0, "Task ended != current task");
            return;
        }

        if(task.getTaskType() == P2PTask.TYPE_INDEX) {
            handleNodeIndexUpdated(task.getNode(),task.getDestinationPath());
        }


        currentTask = null;
        checkDownloadQueue();

    }

    @Override
    public void taskEnded(BluetoothTask task) {
        if(task!=currentBluetoothTask){
            return;
        }
        currentBluetoothTask=null;
        checkBluetoothQueue();
    }

    /**
     * Cross platform method add tasks to the queue
     *
     * @param task
     */

    protected void queueDownloadTask(P2PTask task) {
        taskQueue.add(task);
         if(taskQueue.size()==1 && taskQueue.get(0).getDownloadStatus()!=P2PTask.DOWNLOAD_STATUS_RUNNING){
             checkDownloadQueue();
         }

    }


    protected void queueBluetoothTask(BluetoothTask bluetoothTask){
        bluetoothQueue.add(bluetoothTask);
        if(bluetoothQueue.size()==1){
            checkBluetoothQueue();
        }
    }


    protected abstract P2PTask makeDownloadTask(NetworkNode node, String downloadUri, long startAfterTime);

    protected abstract BluetoothTask makeBlueToothTask(NetworkNode node);

}
