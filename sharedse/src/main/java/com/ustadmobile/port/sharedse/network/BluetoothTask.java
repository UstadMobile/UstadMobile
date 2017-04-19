package com.ustadmobile.port.sharedse.network;

/**
 * Created by kileha3 on 17/04/2017.
 */

public abstract class BluetoothTask {

    private NetworkNode node;
    protected NetworkTaskListener listener;

    public BluetoothTask(NetworkNode networkNode){
        this.node= networkNode;
    }


    public void setNetworkTaskListener(NetworkTaskListener listener) {
        this.listener = listener;
    }


    public void fireTaskEnded() {
        if(listener != null)
            listener.taskEnded(this);
    }

    public synchronized void start() {

    }

    public synchronized void stop() {

    }

    public NetworkNode getNode() {
        return node;
    }

    public void setNode(NetworkNode node) {
        this.node = node;
    }



}
