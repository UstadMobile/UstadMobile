package com.ustadmobile.port.sharedse.network;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.p2p.P2PManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ustadmobile.port.sharedse.network.DownloadTask.DOWNLOAD_STATUS_RUNNING;

/**
 * Created by kileha3 on 05/02/2017.
 */

public abstract class NetworkManagerSharedSE implements P2PManager, NetworkTaskListener {

    /**
     * List of nodes that we know about around us
     */
    public List<NetworkNode> knownNodes =new ArrayList<>();

    protected Vector<NetworkNodeListener> networkNodeListeners = new Vector<>();

    public Vector<BluetoothTask> bluetoothQueue=new Vector<>();
    public Vector<DownloadTask> downloadTaskQueue=new Vector<>();

    /**
     * Store all reference of all locally available files
     * and their respective nodes to get from.
     */
    public HashMap<String,HashMap<String,String>> availableFiles=new HashMap<>();

    public BluetoothTask currentBluetoothTask;
    public DownloadTask currentDownloadTask;

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
            currentBluetoothTask.setBluetoothNetworkListener(this);
            currentBluetoothTask.start();
        }
    }


    @Override
    public void bluetoothTaskEnded(BluetoothTask task) {
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
     * Start insecure bluetooth service (register services,
     * open socket and listen for the incoming communication)
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


    /**
     * Cross platform file download
     * @param feed - feed contains file entries
     * @param context - Application context
     */
    public void downloadFile(UstadJSOPDSFeed feed,Object context){
        queueDownloadTasks(feed);
    }

    /**
     * Cross platform method to queue the download tasks as per user selections.
     * @param feed - Feed to get entry files to download from.
     */
    private void queueDownloadTasks(UstadJSOPDSFeed feed){
        DownloadTask downloadTask=makeDownloadTask(feed);
        if(!downloadTaskQueue.contains(downloadTask)){
            downloadTask.setDownloadID((long) new AtomicInteger().incrementAndGet());
            downloadTaskQueue.add(downloadTask);

            if(currentDownloadTask==null || downloadTaskQueue.size()==1){
                checkDownloadQueue();
            }
        }

    }

    /**
     * Cross platform method to check the queue if has more task to process
     * (Download feed entries), otherwise finish it.
     */
    private synchronized void checkDownloadQueue(){
        if(!downloadTaskQueue.isEmpty()){

            if(currentBluetoothTask!=null && downloadTaskQueue.size()>0){
                return;
            }
            currentDownloadTask=downloadTaskQueue.remove(0);
            currentDownloadTask.setDownloadTaskListener(this);
            currentDownloadTask.start();
        }
    }

    /**
     * Cross platform method to be fired when feed download task has ended
     * @param task - current downloading tasking
     */
    @Override
    public void downloadTaskEnded(DownloadTask task) {
        if(task!=currentDownloadTask){
            return;
        }
        currentDownloadTask=null;
        checkDownloadQueue();
    }


    /**
     * Create download task and start download
     * @param feed feed to download
     * @return
     */
    protected abstract DownloadTask makeDownloadTask(UstadJSOPDSFeed feed);

}
