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
    private int downloadJobId;

    private int downloadSetId;

    private long timeCreated;

    private long timeRequested;

    private long timeCompleted;

    private boolean allowMeteredDataUsage;

    /**
     * Status as per flags on NetworkTask
     */
    private int status;


    /**
     * Empty constructor
     */
    public DownloadJob(){

    }

    /**
     * Constructor
     *
     * @param downloadSet The DownloadSet that this DownloadJob is related to
     * @param timeRequested The time to mark as the timeRequested (in ms)
     */
    public DownloadJob(DownloadSet downloadSet, long timeRequested){
        this.downloadSetId = downloadSet.getId();
        this.timeRequested = timeRequested;
        this.timeCreated = System.currentTimeMillis();
    }


    /**
     * Getter for downloadJobId property
     *
     * @return The DownloadJobId (primary key)
     */
    public int getDownloadJobId() {
        return downloadJobId;
    }

    /**
     * Setter for downloadJobId property
     *
     * @param downloadJobId The DownloadJobId (primary key)
     */
    public void setDownloadJobId(int downloadJobId) {
        this.downloadJobId = downloadJobId;
    }

    /**
     * Getter for the downloadSetId proeprty
     *
     * @return The id (primary key) of the related DownloadSet
     */
    public int getDownloadSetId() {
        return downloadSetId;
    }

    /**
     * Setter for the downloadSetId property
     *
     * @param downloadSetId The id (primary key) of the related DownloadSet
     */
    public void setDownloadSetId(int downloadSetId) {
        this.downloadSetId = downloadSetId;
    }

    /**
     * Getter for the timeCreated property
     *
     * @return The time this downloadJob was created (in ms)
     */
    public long getTimeCreated() {
        return timeCreated;
    }

    /**
     * Setter for the timeCreated property
     *
     * @param timeCreated The time this downloadJob was created (in ms)
     */
    public void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }

    /**
     * Getter for the timeRequested property
     *
     * @return The time this download was requested (e.g. queued)
     */
    public long getTimeRequested() {
        return timeRequested;
    }

    /**
     * Setter for the timeRequested property
     *
     * @param timeRequested The time this download was requested (e.g. queued)
     */
    public void setTimeRequested(long timeRequested) {
        this.timeRequested = timeRequested;
    }

    /**
     * Getter for the timeCompleted property
     *
     * @return The time this DownloadJob was completed (in ms)
     */
    public long getTimeCompleted() {
        return timeCompleted;
    }

    /**
     * Setter for the timeCompleted property
     *
     * @param timeCompleted The time this DownloadJob was completed (in ms)
     */
    public void setTimeCompleted(long timeCompleted) {
        this.timeCompleted = timeCompleted;
    }

    /**
     * Getter for the status property
     *
     * @return The status of this DownloadJob as per the NetworkTask status flags
     */
    public int getStatus() {
        return status;
    }

    /**
     * Setter for the status property
     *
     * @param status The status of this DownloadJob as per the NetworkTask status flags
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Get if the usage of metered data is enabled for this download job.
     *
     * @return true if the usage of metered data is enabled for this download job.
     */
    public boolean isAllowMeteredDataUsage() {
        return allowMeteredDataUsage;
    }

    /**
     * Set if the usage of metered data is enabled for this download job.
     *
     * @param allowMeteredDataUsage if the usage of metered data is enabled for this download job.
     */
    public void setAllowMeteredDataUsage(boolean allowMeteredDataUsage) {
        this.allowMeteredDataUsage = allowMeteredDataUsage;
    }
}
