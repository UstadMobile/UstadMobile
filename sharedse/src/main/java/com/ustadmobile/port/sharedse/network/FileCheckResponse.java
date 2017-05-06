package com.ustadmobile.port.sharedse.network;

/**
 * Created by kileha3 on 03/05/2017.
 */

public class FileCheckResponse {
    private int lastChecked;
    private boolean isFileAvailable;
    private String nodeAddress;

    public FileCheckResponse(String nodeAddress){
        this.nodeAddress=nodeAddress;
    }

    public boolean isFileAvailable() {
        return isFileAvailable;
    }

    public void setFileAvailable(boolean fileAvailable) {
        isFileAvailable = fileAvailable;
    }

    public String getNodeAddress() {
        return nodeAddress;
    }

    public void setNodeAddress(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    public int getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(int lastChecked) {
        this.lastChecked = lastChecked;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof FileCheckResponse &&
                ((FileCheckResponse)object).getNodeAddress().equals(nodeAddress);
    }
}
