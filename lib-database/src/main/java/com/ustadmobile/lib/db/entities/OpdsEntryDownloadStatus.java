package com.ustadmobile.lib.db.entities;

/**
 * Represents the download status of an entry and all it's known children.
 */
public class OpdsEntryDownloadStatus {

    private long totalBytesDownloaded;

    private long totalSize;

    /**
     * Number of entries completely downloaded
     */
    private int entriesWithContainer;

    private int containersDownloaded;

    private int containersDownloadPending;

    public long getTotalBytesDownloaded() {
        return totalBytesDownloaded;
    }

    public void setTotalBytesDownloaded(long totalBytesDownloaded) {
        this.totalBytesDownloaded = totalBytesDownloaded;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public int getEntriesWithContainer() {
        return entriesWithContainer;
    }

    public void setEntriesWithContainer(int entriesWithContainer) {
        this.entriesWithContainer = entriesWithContainer;
    }

    public int getContainersDownloaded() {
        return containersDownloaded;
    }

    public void setContainersDownloaded(int containersDownloaded) {
        this.containersDownloaded = containersDownloaded;
    }

    public int getContainersDownloadPending() {
        return containersDownloadPending;
    }

    public void setContainersDownloadPending(int containersDownloadPending) {
        this.containersDownloadPending = containersDownloadPending;
    }
}
