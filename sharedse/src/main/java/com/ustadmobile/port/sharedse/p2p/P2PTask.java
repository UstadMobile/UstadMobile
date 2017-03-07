package com.ustadmobile.port.sharedse.p2p;

/**
 * Created by kileha3 on 05/03/2017.
 */

public abstract class P2PTask {

    private P2PNode node;

    private String downloadUri;

    protected P2PTaskListener listener;




    public P2PTask(P2PNode node, String downloadUri) {
        this.node = node;
        this.downloadUri = downloadUri;
    }

    public void setP2PTaskListener(P2PTaskListener listener) {
        this.listener = listener;
    }

    /**
     * Runs the task
     */
    public void start() {

    }


    public P2PNode getNode() {
        return node;
    }

    public void setNode(P2PNode node) {
        this.node = node;
    }

    public String getDownloadUri() {
        return downloadUri;
    }

    public void setDownloadUri(String downloadUri) {
        this.downloadUri = downloadUri;
    }

    /**
     * Return the current status of the task
     *
     * @return
     */
    public abstract int getStatus();

    protected void fireTaskEnded() {
        if(listener != null)
            listener.taskEnded(this);
    }


}
