package com.ustadmobile.core.networkmanager;

/**
 * <h1>EntryCheckResponse</h1>
 *
 * This is the class which represent a single response received during Entry status check task execution.
 *
 * @author kileha3
 */
public class EntryCheckResponse {

    private long lastChecked;
    private boolean isFileAvailable;
    private NetworkNode networkNode;

    public EntryCheckResponse(NetworkNode networkNode){
        this.networkNode=networkNode;
    }

    /**
     * Get last time when entry status was checked.
     * @return long: Time in milliseconds, when status check task finished
     */
    public long getLastChecked() {
        return lastChecked;
    }

    /**
     * Set last time when entry status was checked
     * @param lastChecked: Time in milliseconds, when status check task finished
     */
    public void setLastChecked(long lastChecked) {
        this.lastChecked = lastChecked;
    }

    /**
     * Method to check if the status indicates that the file can be downloaded locally
     * @return boolean: Local file availability
     */
    public boolean isFileAvailable() {
        return isFileAvailable;
    }

    /**
     * Set local file availability
     * @param fileAvailable : TRUE, if file is available locally otherwise FALSE.
     */
    public void setFileAvailable(boolean fileAvailable) {
        isFileAvailable = fileAvailable;
    }

    /**
     * Get node on which entry status check task was executed on.
     * @return NetworkNode
     */
    public NetworkNode getNetworkNode() {
        return networkNode;
    }

    /**
     * Set the node on which entry status check task will be executed on.
     * @param networkNode NetworkNode object.
     */
    public void setNetworkNode(NetworkNode networkNode) {
        this.networkNode = networkNode;
    }

    public boolean equals(Object object) {
        return object instanceof EntryCheckResponse && getNetworkNode().equals(this.networkNode);
    }
}
