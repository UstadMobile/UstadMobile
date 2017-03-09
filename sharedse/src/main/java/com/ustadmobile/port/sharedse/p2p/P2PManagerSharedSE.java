package com.ustadmobile.port.sharedse.p2p;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.p2p.P2PManager;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

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
    protected List<P2PNode> knownSupernodes=new ArrayList<>();

    protected Vector<P2PNodeListener> nodeListeners = new Vector<>();

    private Vector<P2PTask> taskQueue = new Vector<>();

    private P2PTask currentTask;

    protected HashMap<String,String> previousConnectedNetwork=new HashMap<>();
    protected boolean isConnectedSameNetwork =false;

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

        for (P2PNode node: knownSupernodes) {
            UstadJSOPDSFeed nodeFeed=availableIndexes.get(node);
            if(nodeFeed.id.equals(fileId)){
                return true;
            }
        }

        return false;
    }

    /**
     * request to download a file from super node
     *
     */
    public abstract int requestDownload(Object context, DownloadRequest request);

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

    protected void handleNodeIndexUpdated(P2PNode node, String fileUri) {
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

    protected void fireNodeDiscovered(P2PNode node) {
        for(P2PNodeListener listener: nodeListeners) {
            listener.nodeDiscovered(node);
        }
    }

    protected void fireNodeGone(P2PNode node) {
        for(P2PNodeListener listener: nodeListeners) {
            listener.nodeGone(node);
        }
    }

    public synchronized void checkQueue(){
        if (currentTask == null && !taskQueue.isEmpty()) {
            currentTask = taskQueue.remove(0);
            currentTask.start();
        }

        //do nothing - a task is already running
    }

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



    protected void queueP2PTask(P2PTask task) {
        taskQueue.add(task);
        checkQueue();
    }

    protected abstract P2PTask makeDownloadTask(P2PNode node, String downloadUri);


}
