package com.ustadmobile.core.impl;

/**
 * Represents an acquisition status event
 *
 * Created by mike on 4/19/17.
 */

public class AcquisitionStatusEvent {

    private int status;

    private long totalBytes;

    private long bytesDownloadedSoFar;

    String entryId;

    public AcquisitionStatusEvent(int status, long totalBytes, long bytesDownloadedSoFar, String entryId) {
        this.status  = status;
        this.totalBytes = totalBytes;
        this.bytesDownloadedSoFar = bytesDownloadedSoFar;
        this.entryId = entryId;
    }


    /**
     * The status of the acquisition: As per UstadMobileSystemImpl.DLSTATUS_ flags
     * @return
     */
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Total bytes to be downloaded on this acquisition:
     *
     * @return Total bytes to be downloaded or -1 if unknown
     */
    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public long getBytesDownloadedSoFar() {
        return bytesDownloadedSoFar;
    }

    public void setBytesDownloadedSoFar(long bytesDownloadedSoFar) {
        this.bytesDownloadedSoFar = bytesDownloadedSoFar;
    }

    public String getEntryId() {
        return entryId;
    }
}
