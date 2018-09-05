package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

/**
 * Represents an OpdsEntry joined to it's related OpdsEntryStatusCache (created using a JOIN query).
 *
 */
public class OpdsEntryWithStatusCache extends OpdsEntryWithRelations {

    public static final int DOWNLOAD_DISPLAY_STATUS_NOT_DOWNLOADED = 0;

    public static final int DOWNLOAD_DISPLAY_STATUS_QUEUED = 1;

    public static final int DOWNLOAD_DISPLAY_STATUS_IN_PROGRESS = 2;

    public static final int DOWNLOAD_DISPLAY_STATUS_PAUSED = 3;

    public static final int DOWNLOAD_DISPLAY_STATUS_DOWNLOADED = 4;

    public static final int DOWNLOAD_DISPLAY_STATUS_ERROR = 5;

    private static final int IN_PROGRESS_THRESHOLD = 90;

    @UmEmbedded
    OpdsEntryStatusCache statusCache;

    @UmEmbedded
    DownloadJobItem pendingDownloadJobItem;

    /**
     * Setter for status cache property.
     *
     * @return The associated OpdsEntryStatusCache object (
     */
    public OpdsEntryStatusCache getStatusCache() {
        return statusCache;
    }

    public void setStatusCache(OpdsEntryStatusCache statusCache) {
        this.statusCache = statusCache;
    }

    public DownloadJobItem getPendingDownloadJobItem() {
        return pendingDownloadJobItem;
    }

    public void setPendingDownloadJobItem(DownloadJobItem pendingDownloadJobItem) {
        this.pendingDownloadJobItem = pendingDownloadJobItem;
    }


    /**
     * Determine the download status that should be displayed for this entry
     *
     * @return Download display status flag as per DOWNLOAD_DISPLAY_STATE flags
     */
    public int getDownloadDisplayState() {
        long containersDownloadedSizeIncDescendants = statusCache.getContainersDownloadedSizeIncDescendants();

        if(containersDownloadedSizeIncDescendants > 0
                && containersDownloadedSizeIncDescendants == statusCache.getSizeIncDescendants()) {
            return DOWNLOAD_DISPLAY_STATUS_DOWNLOADED;
        }else if (statusCache.getPausedDownloadsIncAncestors() > 0) {
            return DOWNLOAD_DISPLAY_STATUS_PAUSED;
        }else if (statusCache.getActiveDownloadsIncAncestors() > 0){
            return DOWNLOAD_DISPLAY_STATUS_IN_PROGRESS;
        }

        int percentRequestedOrCompleted = (statusCache.getEntriesWithContainerIncDescendants()) > 0 ?
                ((statusCache.getContainersDownloadedIncDescendants()
                        + statusCache.getContainersDownloadPendingIncAncestors()) * 100)
                        / (statusCache.getEntriesWithContainerIncDescendants()) : 0;
        if(statusCache.getContainersDownloadPendingIncAncestors() > 0
                && statusCache.getSizeIncDescendants() > 0
                && percentRequestedOrCompleted > IN_PROGRESS_THRESHOLD) {
            return DOWNLOAD_DISPLAY_STATUS_QUEUED;
        }

        return DOWNLOAD_DISPLAY_STATUS_NOT_DOWNLOADED;
    }

    /**
     * Get the download percentage completed (inc ancestors)
     *
     * @return The download percentage completed (inc ancestors), between 0 and 100
     */
    public int getDownloadCompletePercentage(){
        if(statusCache == null)
            return 0;

        if(statusCache.getSizeIncDescendants() == 0)
            return 0;

        long totalDownloaded = statusCache.getContainersDownloadedSizeIncDescendants() +
                statusCache.getPendingDownloadBytesSoFarIncDescendants();
        return (int)((totalDownloaded * 100)/statusCache.getSizeIncDescendants());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OpdsEntryWithStatusCache)) return false;
        if (!super.equals(o)) return false;

        OpdsEntryWithStatusCache that = (OpdsEntryWithStatusCache) o;

        return statusCache != null ? statusCache.equals(that.statusCache) : that.statusCache == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (statusCache != null ? statusCache.hashCode() : 0);
        return result;
    }
}
