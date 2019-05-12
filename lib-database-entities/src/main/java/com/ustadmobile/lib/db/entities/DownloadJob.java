package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * A DownloadJob represents a specific run of downloading a DownloadSet. The DownloadSet contains
 * the list of entries that are to be downloaded. One DownloadSet can have multiple DownloadJobs, e.g.
 * one DownloadJob that initially downloads it, and then further DownloadJobs when it is updated, when
 * new entries become available, etc.
 */
@UmEntity
public class DownloadJob {

    @UmPrimaryKey(autoIncrement = true)
    private long djUid;

    private long djDsUid;

    private long timeCreated;

    private long timeRequested;

    private long timeCompleted;

    private long totalBytesToDownload;

    private long bytesDownloadedSoFar;

    /**
     * Status as per flags on NetworkTask
     */
    private int djStatus;


    /**
     * Empty constructor
     */
    public DownloadJob(){

    }

    public DownloadJob(DownloadSet downloadSet) {
        this.djDsUid = downloadSet.getDsUid();
    }

    public long getDjUid() {
        return djUid;
    }

    public void setDjUid(long djUid) {
        this.djUid = djUid;
    }

    public long getDjDsUid() {
        return djDsUid;
    }

    public void setDjDsUid(long djDsUid) {
        this.djDsUid = djDsUid;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }

    public long getTimeRequested() {
        return timeRequested;
    }

    public void setTimeRequested(long timeRequested) {
        this.timeRequested = timeRequested;
    }

    public long getTimeCompleted() {
        return timeCompleted;
    }

    public void setTimeCompleted(long timeCompleted) {
        this.timeCompleted = timeCompleted;
    }

    public int getDjStatus() {
        return djStatus;
    }

    public void setDjStatus(int djStatus) {
        this.djStatus = djStatus;
    }

    public long getTotalBytesToDownload() {
        return totalBytesToDownload;
    }

    public void setTotalBytesToDownload(long totalBytesToDownload) {
        this.totalBytesToDownload = totalBytesToDownload;
    }

    public long getBytesDownloadedSoFar() {
        return bytesDownloadedSoFar;
    }

    public void setBytesDownloadedSoFar(long bytesDownloadedSoFar) {
        this.bytesDownloadedSoFar = bytesDownloadedSoFar;
    }
}
