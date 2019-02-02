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
    private int djUid;

    private int djDsUid;

    private long timeCreated;

    private long timeRequested;

    private long timeCompleted;

    /**
     * Status as per flags on NetworkTask
     */
    private int djStatus;


    /**
     * Empty constructor
     */
    public DownloadJob(){

    }

    public int getDjUid() {
        return djUid;
    }

    public void setDjUid(int djUid) {
        this.djUid = djUid;
    }

    public int getDjDsUid() {
        return djDsUid;
    }

    public void setDjDsUid(int djDsUid) {
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
}
