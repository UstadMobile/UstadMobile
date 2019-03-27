package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * DownloadJobItemHistory represents one attempt to download a given DownloadJobItem. It is used
 * to track the performance of different peers, and inform the selection of peers when attempting to
 * download items in the future.
 */
@UmEntity
public class DownloadJobItemHistory {

    public static final int MODE_CLOUD = 1;

    public static final int MODE_LOCAL = 2;

    @UmPrimaryKey(autoIncrement = true)
    private long id;

    private String url;

    //Foreign key for the networknode this is connected to
    private long networkNode;

    private long downloadJobItemId;

    private int mode;

    private long numBytes;

    private boolean successful;

    private long startTime;

    private long endTime;

    public DownloadJobItemHistory() {

    }

    public DownloadJobItemHistory(long networkNode, int mode, boolean successful, long startTime, long endTime){
        this.networkNode = networkNode;
        this.mode = mode;
        this.successful = successful;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public DownloadJobItemHistory(NetworkNode node, DownloadSetItem item, int mode, long startTime) {
        if(node != null)
            networkNode = node.getNodeId();

        if(item != null)
            downloadJobItemId = item.getDsiUid();

        this.mode = mode;
        this.startTime= startTime;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getNetworkNode() {
        return networkNode;
    }

    public void setNetworkNode(long networkNode) {
        this.networkNode = networkNode;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public long getNumBytes() {
        return numBytes;
    }

    public void setNumBytes(long numBytes) {
        this.numBytes = numBytes;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getDownloadJobItemId() {
        return downloadJobItemId;
    }

    public void setDownloadJobItemId(long downloadJobItemId) {
        this.downloadJobItemId = downloadJobItemId;
    }
}
