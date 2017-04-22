package com.ustadmobile.port.sharedse.network;

import com.ustadmobile.core.p2p.P2PManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by kileha3 on 05/02/2017.
 */

public abstract class NetworkManagerSharedSE implements P2PManager, NetworkTaskListener {

    /**
     * List of nodes that we know about around us
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
    /**
     * Store all reference of all locally available files
     * and their respective nodes to get from.
     */
    public HashMap<String,HashMap<String,String>> availableFiles=new HashMap<>();

    private P2PTask currentTask;
    public BluetoothTask currentBluetoothTask;

    /**
     * Flags to set where to get file from (Cloud,P2P or Local network)
     */
    /**
     * This will be set if the file will be downloaded from the cloud
     */
    public static final int DOWNLOAD_SOURCE_CLOUD =1;
    /**
     * This willl be set if the file will be downloaded from peer device
     */
    public static final int DOWNLOAD_SOURCE_P2P =2;

    /**
     * The current download source holder
     */
    public int currentDownloadSource =-1;
    public int currentTaskIndex=0;

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
     * Process locally file availability
     * */
    public abstract void checkLocalFilesAvailability(Object context, List<String> files);

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
     * This method is used to handle all the connection between two devices,
     * it will take acre if we have more than one device to communicate to
     * @param bluetoothTask - created bluetooth communication task
     */
    protected void queueBluetoothTask(BluetoothTask bluetoothTask){
        bluetoothQueue.add(bluetoothTask);
    }



    /**
     * Cross platform queuing logic, it check if there are task to execute and if any assign to the
     * current task and process it by calling bluetoothTask.start() which handles all connection logic
     *
     */
    public synchronized void checkBluetoothQueue(){
        if(!bluetoothQueue.isEmpty() && currentTaskIndex <=(bluetoothQueue.size()-1)){
            currentBluetoothTask=bluetoothQueue.get(currentTaskIndex);
            currentBluetoothTask.setNetworkTaskListener(this);
            currentBluetoothTask.start();
        }
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


    /**
     * Start insecure bluetooth service (register services,open socket and listen for the incoming communication)
     */
    public abstract void startInsecureBluetoothService();

    /**
     * Stop insecure bluetooth service which was started
     */
    public abstract void stopInsecureBluetoothService();

    /**
     * Create bluetooth task and start communication between two devices.
     * @param node Network node in this case it will be Bluetooth node
     * @return
     */
    protected abstract BluetoothTask makeBlueToothTask(NetworkNode node);

}
