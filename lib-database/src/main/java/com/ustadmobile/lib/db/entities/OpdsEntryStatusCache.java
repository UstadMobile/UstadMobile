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

    @UmPrimaryKey
    private Integer statusCacheUid;

    @UmIndexField
    private String statusEntryId;


    private long sumActiveDownloadsBytesSoFar;

    private long sumContainersDownloadedSize;

    private long acquisitionLinkLength;

    private long totalSize;

    private int entriesWithContainer;

    private int containersDownloaded;

    private int containersDownloadPending;

    private int acquisitionStatus;

    public static final int ACQUISITION_STATUS_UNACQUIRED = 0;

    public static final int ACQUISITION_STATUS_IN_PROGRESS = 1;

    public static final int ACQUISITION_STATUS_ACQUIRED = 2;

    public OpdsEntryStatusCache() {

    }

    public OpdsEntryStatusCache(String statusEntryId) {
        this.statusEntryId = statusEntryId;
    }

    public OpdsEntryStatusCache(String statusEntryId, long acquisitionLinkLength) {
        this.statusEntryId = statusEntryId;
        this.acquisitionLinkLength = acquisitionLinkLength;
    }

    /**
     * StatusCacheUid is an artificial auto-increment primary key.
     *
     * @return The primary key
     */
    public Integer getStatusCacheUid() {
        return statusCacheUid;
    }

    /**
     * Should be used only by the ORM.
     *
     * @param statusCacheUid
     */
    public void setStatusCacheUid(Integer statusCacheUid) {
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
    public long getTotalSize() {
        return totalSize;
    }

    /**
     * To be used only by the ORM
     * @param totalSize
     */
    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    /**
     * The total number of entries that have a container for acquisition, including all known
     * descendent entries.
     *
     * @return Total number of entries that have a container
     */
    public int getEntriesWithContainer() {
        return entriesWithContainer;
    }

    /**
     * To be used only by the ORM
     *
     * @param entriesWithContainer
     */
    public void setEntriesWithContainer(int entriesWithContainer) {
        this.entriesWithContainer = entriesWithContainer;
    }

    /**
     * The total number of containers that have been downloaded, including all known descendents
     *
     * @return The total number of containers that have been downloaded, including all known descendents
     */
    public int getContainersDownloaded() {
        return containersDownloaded;
    }

    /**
     * To be used only by the ORM
     *
     * @param containersDownloaded
     */
    public void setContainersDownloaded(int containersDownloaded) {
        this.containersDownloaded = containersDownloaded;
    }

    /**
     * The total number of containers for which a download is pending (queued, but not yet complete)
     * @return
     */
    public int getContainersDownloadPending() {
        return containersDownloadPending;
    }

    /**
     *
     * @param containersDownloadPending
     */
    public void setContainersDownloadPending(int containersDownloadPending) {
        this.containersDownloadPending = containersDownloadPending;
    }

    /**
     * The total bytes downloaded for all currently active downloads for this entry and all known
     * descendent entries (recursive).
     *
     * @return The sum (in bytes) of bytes downlaoded so far for this and all known descendent entries
     */
    public long getSumActiveDownloadsBytesSoFar() {
        return sumActiveDownloadsBytesSoFar;
    }

    /**
     * Set the sum of bytes downloaded for this and all known currently active descendent entries.
     * Should be used only by the ORM.
     *
     * @param sumActiveDownloadsBytesSoFar
     */
    public void setSumActiveDownloadsBytesSoFar(long sumActiveDownloadsBytesSoFar) {
        this.sumActiveDownloadsBytesSoFar = sumActiveDownloadsBytesSoFar;
    }

    /**
     * The total size (in bytes) of all containers that have been downloaded so far for this entry
     * and all known descendent entries.
     *
     * @return Total size (in bytes) of all containers that have been downloaded for this entry and
     * all known descendent entries (recursive).
     */
    public long getSumContainersDownloadedSize() {
        return sumContainersDownloadedSize;
    }

    /**
     * For use only by the ORM
     *
     * @param sumContainersDownloadedSize
     */
    public void setSumContainersDownloadedSize(long sumContainersDownloadedSize) {
        this.sumContainersDownloadedSize = sumContainersDownloadedSize;
    }

    /**
     * The length attribute of the OPDS acquisition link that has been used for size calculation
     * purposes
     *
     * @return Length of the OPDS acquisition link that has been used for size calculation
     */
    public long getAcquisitionLinkLength() {
        return acquisitionLinkLength;
    }

    public void setAcquisitionLinkLength(long acquisitionLinkLength) {
        this.acquisitionLinkLength = acquisitionLinkLength;
    }

    /**
     * The acquisition status of this entry as per the ACQUISITION_STATUS_ flags
     *
     * @see #ACQUISITION_STATUS_UNACQUIRED
     * @see #ACQUISITION_STATUS_IN_PROGRESS
     * @see #ACQUISITION_STATUS_ACQUIRED
     *
     * @return the acquisition status of this entry
     */
    public int getAcquisitionStatus() {
        return acquisitionStatus;
    }

    /**
     * For use only by the ORM
     *
     * @param acquisitionStatus
     */
    public void setAcquisitionStatus(int acquisitionStatus) {
        this.acquisitionStatus = acquisitionStatus;
    }
}
