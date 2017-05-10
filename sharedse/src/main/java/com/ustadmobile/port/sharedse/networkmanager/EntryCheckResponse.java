package com.ustadmobile.port.sharedse.networkmanager;

import java.util.Map;

/**
 * Created by kileha3 on 09/05/2017.
 */

public class EntryCheckResponse {

    private long lastChecked;
    private boolean isFileAvailable;
    private NetworkNode networkNode;

    public EntryCheckResponse(NetworkNode networkNode){
        this.networkNode=networkNode;
    }

    public long getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(long lastChecked) {
        this.lastChecked = lastChecked;
    }

    public boolean isFileAvailable() {
        return isFileAvailable;
    }

    public void setFileAvailable(boolean fileAvailable) {
        isFileAvailable = fileAvailable;
    }

    public NetworkNode getNetworkNode() {
        return networkNode;
    }

    public void setNetworkNode(NetworkNode networkNode) {
        this.networkNode = networkNode;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof EntryCheckResponse && getNetworkNode().equals(this.networkNode);
    }
}
