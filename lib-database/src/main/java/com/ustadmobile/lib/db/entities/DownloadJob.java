package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Created by mike on 1/26/18.
 */

@UmEntity
public class DownloadJob {

    @UmPrimaryKey(autoIncrement = true)
    private int id;

    /**
     * Status as per flags on NetworkTask
     */
    private int status;

    private long timeRequested;

    private long timeCompleted;

    private String destinationDir;

    private boolean wifiDirectDownloadEnabled;

    private boolean lanDownloadEnabled;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public String getDestinationDir() {
        return destinationDir;
    }

    public void setDestinationDir(String destinationDir) {
        this.destinationDir = destinationDir;
    }

    /**
     * If enabled the task will attempt to acquire the requested entries from another node using
     * wifi direct. The node will be contacted using bluetooth and then a wifi group connection
     * will be created.
     *
     * @return boolean: True if enabled, false otherwise
     */
    public boolean isWifiDirectDownloadEnabled() {
        return wifiDirectDownloadEnabled;
    }

    public void setWifiDirectDownloadEnabled(boolean wifiDirectDownloadEnabled) {
        this.wifiDirectDownloadEnabled = wifiDirectDownloadEnabled;
    }

    /**
     * If enabled the task will attempt to acquire the requested entries from another node on the same
     * wifi network directly (nodes discovered using Network Service Discovery - NSD).
     *
     * @return boolean: True if enabled, false otherwise
     */
    public boolean isLanDownloadEnabled() {
        return lanDownloadEnabled;
    }

    public void setLanDownloadEnabled(boolean lanDownloadEnabled) {
        this.lanDownloadEnabled = lanDownloadEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DownloadJob)) return false;

        DownloadJob that = (DownloadJob) o;

        if (id != that.id) return false;
        if (status != that.status) return false;
        if (timeRequested != that.timeRequested) return false;
        if (timeCompleted != that.timeCompleted) return false;
        return destinationDir.equals(that.destinationDir);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + status;
        result = 31 * result + (int) (timeRequested ^ (timeRequested >>> 32));
        result = 31 * result + (int) (timeCompleted ^ (timeCompleted >>> 32));
        result = 31 * result + destinationDir.hashCode();
        return result;
    }
}
