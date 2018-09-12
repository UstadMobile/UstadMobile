package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Represents the download status of an entry and all it's known descendants. This is used to show
 * the user the status of any given entry and it's subsections as they browse, without requiring a
 * large query.
 *
 * It needs recalculated only in the rare condition that a descendant is discovered that links to
 * another set of descendants that are already in the database.
 *
 * When an OPDS entry is first discovered, a recursive query determines all the ancestors for a
 * given entry. When status information changes (e.g. when a download starts, completes, etc) update
 * queries incrementing the appropriate values (e.g. total size, bytes downloaded, etc) are executed
 * for the entry being downloaded itself, and all it's ancestors. This results in each
 * OpdsEntryStatusCache object having up to date totals for the total size of all descendent entries,
 * download progress on descendent entries, and the size of all descendent entries downloaded or
 * discovered on disk.
 *
 * @see OpdsEntryStatusCacheAncestor
 */
@UmEntity
public class OpdsEntryStatusCache {

    @UmPrimaryKey(autoIncrement = true)
    private int statusCacheUid;

    @UmIndexField
    private String statusEntryId;

    private long pendingDownloadBytesSoFarIncDescendants;

    private long entryPendingDownloadBytesSoFar;

    private long containersDownloadedSizeIncDescendants;

    private long entryContainerDownloadedSize;

    private long sizeIncDescendants;

    private long entrySize;

    private int entriesWithContainerIncDescendants;

    private boolean entryHasContainer;

    private int containersDownloadedIncDescendants;

    private boolean entryContainerDownloaded;

    private int containersDownloadPendingIncAncestors;

    private boolean entryContainerDownloadPending;

    private long entryAcquisitionLinkLength;

    private int activeDownloadsIncAncestors;

    private boolean entryActiveDownload;

    private int pausedDownloadsIncAncestors;

    private boolean entryPausedDownload;

    public OpdsEntryStatusCache() {

    }

    public OpdsEntryStatusCache(String statusEntryId) {
        this.statusEntryId = statusEntryId;
    }

    public OpdsEntryStatusCache(String statusEntryId, long entryAcquisitionLinkLength) {
        this.statusEntryId = statusEntryId;
        this.entryAcquisitionLinkLength = entryAcquisitionLinkLength;
    }

    /**
     * StatusCacheUid is an artificial auto-increment primary key.
     *
     * @return The primary key
     */
    public int getStatusCacheUid() {
        return statusCacheUid;
    }

    /**
     * Should be used only by the ORM.
     *
     * @param statusCacheUid
     */
    public void setStatusCacheUid(int statusCacheUid) {
        this.statusCacheUid = statusCacheUid;
    }

    /**
     * The associated OPDS Entry ID (e.g. the ID of the OPDS feed, EPUB book, SCORM unique ID, etc)
     *
     * @return OPDS Entry ID
     */
    public String getStatusEntryId() {
        return statusEntryId;
    }

    /**
     *  Should be used only by the ORM
     *
     * @param statusEntryId
     */
    public void setStatusEntryId(String statusEntryId) {
        this.statusEntryId = statusEntryId;
    }

    /**
     * The total size (in bytes) of this entry and all its known descendents. This is calculated as
     * follows:
     * <ul>
     *  <li>
     *    If entry is not acquired and not yet being downloaded, then the length attribute of the
     *    first acquisition link is counted as the size of the entry.
     *  </li>
     *  <li>
     *      If the entry is in the process of being downloaded, the download size of the download job
     *      item is the counted as the entry size.
     *  </li>
     *  <li>
     *      If the entry is downloaded, then the size of the container file downloaded is the counted
     *      as the size of the entry.
     *  </li>
     * </ul>
     *
     * @return The total size (in bytes) of this entry and all its known descendents.
     */
    public long getSizeIncDescendants() {
        return sizeIncDescendants;
    }

    /**
     * To be used only by the ORM
     * @param sizeIncDescendants
     */
    public void setSizeIncDescendants(long sizeIncDescendants) {
        this.sizeIncDescendants = sizeIncDescendants;
    }

    public long getEntrySize() {
        return entrySize;
    }

    public void setEntrySize(long entrySize) {
        this.entrySize = entrySize;
    }

    /**
     * The total number of entries that have a container for acquisition, including all known
     * descendent entries.
     *
     * @return Total number of entries that have a container
     */
    public int getEntriesWithContainerIncDescendants() {
        return entriesWithContainerIncDescendants;
    }

    /**
     * To be used only by the ORM
     *
     * @param entriesWithContainerIncDescendants
     */
    public void setEntriesWithContainerIncDescendants(int entriesWithContainerIncDescendants) {
        this.entriesWithContainerIncDescendants = entriesWithContainerIncDescendants;
    }

    /**
     * The total number of containers that have been downloaded, including all known descendents
     *
     * @return The total number of containers that have been downloaded, including all known descendents
     */
    public int getContainersDownloadedIncDescendants() {
        return containersDownloadedIncDescendants;
    }

    /**
     * To be used only by the ORM
     *
     * @param containersDownloadedIncDescendants
     */
    public void setContainersDownloadedIncDescendants(int containersDownloadedIncDescendants) {
        this.containersDownloadedIncDescendants = containersDownloadedIncDescendants;
    }

    public boolean isEntryContainerDownloaded() {
        return entryContainerDownloaded;
    }

    public void setEntryContainerDownloaded(boolean entryContainerDownloaded) {
        this.entryContainerDownloaded = entryContainerDownloaded;
    }

    /**
     * The total number of containers for which a download is pending (queued, but not yet complete)
     * @return
     */
    public int getContainersDownloadPendingIncAncestors() {
        return containersDownloadPendingIncAncestors;
    }

    /**
     *
     * @param containersDownloadPendingIncAncestors
     */
    public void setContainersDownloadPendingIncAncestors(int containersDownloadPendingIncAncestors) {
        this.containersDownloadPendingIncAncestors = containersDownloadPendingIncAncestors;
    }

    public boolean isEntryContainerDownloadPending() {
        return entryContainerDownloadPending;
    }

    public void setEntryContainerDownloadPending(boolean entryContainerDownloadPending) {
        this.entryContainerDownloadPending = entryContainerDownloadPending;
    }

    /**
     * The total bytes downloaded for all currently active downloads for this entry and all known
     * descendent entries (recursive).
     *
     * @return The sum (in bytes) of bytes downlaoded so far for this and all known descendent entries
     */
    public long getPendingDownloadBytesSoFarIncDescendants() {
        return pendingDownloadBytesSoFarIncDescendants;
    }

    /**
     * Set the sum of bytes downloaded for this and all known currently active descendent entries.
     * Should be used only by the ORM.
     *
     * @param pendingDownloadBytesSoFarIncDescendants
     */
    public void setPendingDownloadBytesSoFarIncDescendants(long pendingDownloadBytesSoFarIncDescendants) {
        this.pendingDownloadBytesSoFarIncDescendants = pendingDownloadBytesSoFarIncDescendants;
    }

    public long getEntryPendingDownloadBytesSoFar() {
        return entryPendingDownloadBytesSoFar;
    }

    public void setEntryPendingDownloadBytesSoFar(long entryPendingDownloadBytesSoFar) {
        this.entryPendingDownloadBytesSoFar = entryPendingDownloadBytesSoFar;
    }

    /**
     * The total size (in bytes) of all containers that have been downloaded so far for this entry
     * and all known descendent entries.
     *
     * @return Total size (in bytes) of all containers that have been downloaded for this entry and
     * all known descendent entries (recursive).
     */
    public long getContainersDownloadedSizeIncDescendants() {
        return containersDownloadedSizeIncDescendants;
    }

    /**
     * For use only by the ORM
     *
     * @param containersDownloadedSizeIncDescendants
     */
    public void setContainersDownloadedSizeIncDescendants(long containersDownloadedSizeIncDescendants) {
        this.containersDownloadedSizeIncDescendants = containersDownloadedSizeIncDescendants;
    }

    public long getEntryContainerDownloadedSize() {
        return entryContainerDownloadedSize;
    }

    public void setEntryContainerDownloadedSize(long entryContainerDownloadedSize) {
        this.entryContainerDownloadedSize = entryContainerDownloadedSize;
    }

    /**
     * The length attribute of the OPDS acquisition link that has been used for size calculation
     * purposes
     *
     * @return Length of the OPDS acquisition link that has been used for size calculation
     */
    public long getEntryAcquisitionLinkLength() {
        return entryAcquisitionLinkLength;
    }

    public void setEntryAcquisitionLinkLength(long entryAcquisitionLinkLength) {
        this.entryAcquisitionLinkLength = entryAcquisitionLinkLength;
    }

    public boolean isEntryHasContainer() {
        return entryHasContainer;
    }

    public void setEntryHasContainer(boolean entryHasContainer) {
        this.entryHasContainer = entryHasContainer;
    }


    /**
     * Get the total number of downloads currently ongoing including this entry and all its ancestors
     *
     * @return The total number of downloads currently ongoing including this entry and all its ancestors
     */
    public int getActiveDownloadsIncAncestors() {
        return activeDownloadsIncAncestors;
    }

    /**
     * Set the total number of downloads currently ongoing including this entry and all its ancestors
     * @param activeDownloadsIncAncestors
     */
    public void setActiveDownloadsIncAncestors(int activeDownloadsIncAncestors) {
        this.activeDownloadsIncAncestors = activeDownloadsIncAncestors;
    }

    /**
     * True if the download for this entry (e.g. DownloadJobItem) is currently running, false otherwise
     *
     * @return True if the download for this entry (e.g. DownloadJobItem) is currently running, false otherwise
     */
    public boolean isEntryActiveDownload() {
        return entryActiveDownload;
    }

    /**
     * True if the download for this entry (e.g. DownloadJobItem) is currently running, false otherwise
     *
     * @param entryActiveDownload True if the download for this entry (e.g. DownloadJobItem) is currently running, false otherwise
     */
    public void setEntryActiveDownload(boolean entryActiveDownload) {
        this.entryActiveDownload = entryActiveDownload;
    }


    /**
     * Get the total number of paused downloads, including any descendants
     *
     * @return The total number of paused downloads, including any descendants
     */
    public int getPausedDownloadsIncAncestors() {
        return pausedDownloadsIncAncestors;
    }

    /**
     * Set the total number of paused downloads, including any descendants
     * @param pausedDownloadsIncAncestors the total number of paused downloads, including any descendants
     */
    public void setPausedDownloadsIncAncestors(int pausedDownloadsIncAncestors) {
        this.pausedDownloadsIncAncestors = pausedDownloadsIncAncestors;
    }

    /**
     * Get if the download for this entry is currently paused.
     *
     * @return True if the download for this entry is currently paused, false otherwise
     */
    public boolean isEntryPausedDownload() {
        return entryPausedDownload;
    }

    /**
     * Set if the download for this entry is currently paused.
     *
     * @param entryPausedDownload True if the download for this entry is currently paused, false otherwise
     */
    public void setEntryPausedDownload(boolean entryPausedDownload) {
        this.entryPausedDownload = entryPausedDownload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OpdsEntryStatusCache)) return false;

        OpdsEntryStatusCache that = (OpdsEntryStatusCache) o;

        if (pendingDownloadBytesSoFarIncDescendants != that.pendingDownloadBytesSoFarIncDescendants)
            return false;
        if (entryPendingDownloadBytesSoFar != that.entryPendingDownloadBytesSoFar) return false;
        if (containersDownloadedSizeIncDescendants != that.containersDownloadedSizeIncDescendants)
            return false;
        if (entryContainerDownloadedSize != that.entryContainerDownloadedSize) return false;
        if (sizeIncDescendants != that.sizeIncDescendants) return false;
        if (entrySize != that.entrySize) return false;
        if (entriesWithContainerIncDescendants != that.entriesWithContainerIncDescendants)
            return false;
        if (entryHasContainer != that.entryHasContainer) return false;
        if (containersDownloadedIncDescendants != that.containersDownloadedIncDescendants)
            return false;
        if (entryContainerDownloaded != that.entryContainerDownloaded) return false;
        if (containersDownloadPendingIncAncestors != that.containersDownloadPendingIncAncestors)
            return false;
        if (entryContainerDownloadPending != that.entryContainerDownloadPending) return false;
        if (entryAcquisitionLinkLength != that.entryAcquisitionLinkLength) return false;
        if (statusCacheUid != that.statusCacheUid) return false;
        if (entryActiveDownload != that.entryActiveDownload) return false;
        if (activeDownloadsIncAncestors != that.activeDownloadsIncAncestors) return false;
        if(!statusEntryId.equals(that.statusEntryId)) return false;
        if(entryPausedDownload != that.entryPausedDownload) return false;
        return pausedDownloadsIncAncestors == that.pausedDownloadsIncAncestors;
    }

    @Override
    public int hashCode() {
        int result = statusCacheUid;
        result = 31 * result + statusEntryId.hashCode();
        result = 31 * result + (int) (pendingDownloadBytesSoFarIncDescendants ^ (pendingDownloadBytesSoFarIncDescendants >>> 32));
        result = 31 * result + (int) (entryPendingDownloadBytesSoFar ^ (entryPendingDownloadBytesSoFar >>> 32));
        result = 31 * result + (int) (containersDownloadedSizeIncDescendants ^ (containersDownloadedSizeIncDescendants >>> 32));
        result = 31 * result + (int) (entryContainerDownloadedSize ^ (entryContainerDownloadedSize >>> 32));
        result = 31 * result + (int) (sizeIncDescendants ^ (sizeIncDescendants >>> 32));
        result = 31 * result + (int) (entrySize ^ (entrySize >>> 32));
        result = 31 * result + entriesWithContainerIncDescendants;
        result = 31 * result + (entryHasContainer ? 1 : 0);
        result = 31 * result + containersDownloadedIncDescendants;
        result = 31 * result + (entryContainerDownloaded ? 1 : 0);
        result = 31 * result + containersDownloadPendingIncAncestors;
        result = 31 * result + (entryContainerDownloadPending ? 1 : 0);
        result = 31 * result + (int) (entryAcquisitionLinkLength ^ (entryAcquisitionLinkLength >>> 32));
        result = 31 * result + (entryActiveDownload ? 1 : 0);
        result = 31 * result + activeDownloadsIncAncestors;
        return result;
    }
}
