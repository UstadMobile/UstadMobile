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


    public void setBluetoothNetworkListener(NetworkTaskListener listener) {
        this.listener = listener;
    }


    public void fireTaskEnded() {
        if(listener != null)
            listener.bluetoothTaskEnded(this);
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
