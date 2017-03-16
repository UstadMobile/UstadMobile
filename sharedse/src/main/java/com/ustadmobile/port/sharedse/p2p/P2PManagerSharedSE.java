package com.ustadmobile.port.sharedse.p2p;

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.p2p.P2PManager;
import com.ustadmobile.core.util.UMFileUtil;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by kileha3 on 05/02/2017.
 */

public abstract class P2PManagerSharedSE implements P2PManager, P2PTaskListener {


    /**
     * Map of available supernodes mapped as node to index file
     */
    protected HashMap<P2PNode, UstadJSOPDSFeed> availableIndexes=new HashMap<>();

    /**
     * List of supernodes that we know about around us
     */
    protected List<P2PNode> knownSuperNodes =new ArrayList<>();

    protected Vector<P2PNodeListener> nodeListeners = new Vector<>();
    /**
     * Store the tasks of which will be executed in a FIFO way
     */
    public Vector<P2PTask> taskQueue = new Vector<>();

    /**
     * Store the download requests of which will be executed in a FIFO way
     */
    public HashMap<Integer,P2PTask> downloadRequests=new HashMap<>();

    private P2PTask currentTask;

    /**
     * Store information of the previously connected WiFi hotspot before connecting to
     * the no prompt network.
     */
    protected String [] currentConnectedNetwork=new String[3];

    public static final int CURRENT_NETWORK_SSID=0;
    public static final int CURRENT_NETWORK_NETID=1;
    public static final int CURRENT_NETWORK_GATWAY_ADDRESS=2;

    /**
     * Used to check if the previous and the current network are the same, so that
     * we can skip the logic of connecting to no prompt network.
     * i.e. Download task will be made if they are same otherwise device will be
     * connected to no prompt network.
     */

    /**
     * Set if supernode mode is enabled or not (by default this is disabled)
     *
     * @param enabled
     */
    public abstract void setSuperNodeEnabled(Object context, boolean enabled);

    public abstract List<P2PNode> getSuperNodes(Object context);

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
     * check if the file is available locally
     * */
    public boolean isFileAvailable(Object context, String fileId) {
        for (P2PNode node: knownSuperNodes) {
            UstadJSOPDSFeed nodeFeed=availableIndexes.get(node);
            if(nodeFeed.getEntryById(fileId) != null){
                return true;
            }
        }

        return false;
    }

    /**
     * request to download a file from super node
     *
     */
    public int requestDownload(Object context, DownloadRequest request) {
        int requestDownloadId;
        String fileName=null;
        P2PTask downloadIndexTask = null;
        String storageDir = UstadMobileSystemImpl.getInstance().getStorageDirs(
                CatalogController.SHARED_RESOURCE, context)[0].getDirURI();

        boolean isAvailableLocally=isFileAvailable(context,request.fileId);

        if(isAvailableLocally){
            P2PNode nodeWithFile = null;
            UstadJSOPDSFeed nodeFeed;
            UstadJSOPDSEntry fileEntry = null;
            for (P2PNode node: knownSuperNodes) {
                nodeFeed=availableIndexes.get(node);
                fileEntry = nodeFeed.getEntryById(request.getFileId());
                if(fileEntry != null){
                    nodeWithFile = node;
                    break;
                }
            }

            Vector acquisitionLinks = fileEntry.getAcquisitionLinks();

            if(acquisitionLinks.size() > 0) {

                String[] acquisitionAttrs = (String[])acquisitionLinks.get(0);
                String url = acquisitionAttrs[UstadJSOPDSItem.ATTR_HREF];
                fileName = UMFileUtil.getFilename(url);
                downloadIndexTask = makeDownloadTask(nodeWithFile,url);
            }
        }else{
            P2PNode node=new P2PNode(currentConnectedNetwork[CURRENT_NETWORK_GATWAY_ADDRESS]);
            node.setNetworkSSID(currentConnectedNetwork[CURRENT_NETWORK_SSID]);
            downloadIndexTask = makeDownloadTask(node,request.fileSource);
            fileName=UMFileUtil.getFilename(request.fileSource);
        }

        File fileDir = new File(storageDir+"/"+fileName);
        downloadIndexTask.setDestinationPath(fileDir.getAbsolutePath());
        downloadIndexTask.setTaskType(P2PTask.TYPE_COURSE);
        downloadIndexTask.setP2PTaskListener(this);

        requestDownloadId = new Random().nextInt(100-1)+1;
        downloadRequests.put(requestDownloadId,downloadIndexTask);
        queueP2PTask(downloadIndexTask);

        return requestDownloadId;
    }

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


    public void addNodeListener(P2PNodeListener listener) {
        nodeListeners.add(listener);
    }

    public void removeNodeListener(P2PNodeListener listener) {
        nodeListeners.remove(listener);
    }

    /**
     * Cross platform logic to execute when a new node is discovered
     *
     * @param node
     */
    protected void handleNodeDiscovered(P2PNode node) {
        fireNodeDiscovered(node);

        P2PTask downloadIndexTask = makeDownloadTask(node, "/catalog/acquire.opds");
        try{
            File tmpFile = File.createTempFile("acquire", ".opds");
            downloadIndexTask.setDestinationPath(tmpFile.getAbsolutePath());
            downloadIndexTask.setTaskType(P2PTask.TYPE_INDEX);
            downloadIndexTask.setP2PTaskListener(this);
            queueP2PTask(downloadIndexTask);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Cross platform logic to execute when updating node indexes
     *
     * @param node
     */

    protected void handleNodeIndexUpdated(P2PNode node, String fileUri) {
        try{
            UstadJSOPDSFeed feed = UstadJSOPDSFeed.loadFromXML(new FileInputStream(fileUri), "UTF-8");//params needed: input stream
            availableIndexes.put(node, feed);
            UstadMobileSystemImpl.l(UMLog.DEBUG, 2, "Available Index "+availableIndexes.size());
            //feed.entries[0].id


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
    protected void fireNodeDiscovered(P2PNode node) {
        for(P2PNodeListener listener: nodeListeners) {
            listener.nodeDiscovered(node);
        }
    }

    /**
     * Cross platform listener method to execute when a new node is no longer active
     *
     * @param node
     */
    protected void fireNodeGone(P2PNode node) {
        for(P2PNodeListener listener: nodeListeners) {
            listener.nodeGone(node);
        }
    }

    /**
     * Cross platform queuing logic, it check if there are task to execute and if any assign to the
     * current task and process it by calling currentTask.start() which handles all connection logic
     *
     */
    public synchronized void checkQueue(){
        if (currentTask == null && !taskQueue.isEmpty()) {
            currentTask = taskQueue.remove(0);
            currentTask.start();
        }

        //do nothing - a task is already running
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
            //something is very wrong
            UstadMobileSystemImpl.l(UMLog.ERROR, 0, "Task ended != current task");
            return;
        }


        if(task.getTaskType() == P2PTask.TYPE_INDEX) {
            handleNodeIndexUpdated(task.getNode(),task.getDestinationPath());
        }


        currentTask = null;
        checkQueue();

    }


    /**
     * Cross platform method add tasks to the queue
     *
     * @param task
     */

    protected void queueP2PTask(P2PTask task) {
        taskQueue.add(task);
        checkQueue();
    }


    /**
     * Cross platform method which will create a new download task of which
     *
     * @param node - This refer to the node that will be exchanging content/index from.
     * @param downloadUri - This refer to the file URI to download.
     */
    protected abstract P2PTask makeDownloadTask(P2PNode node, String downloadUri);


}
