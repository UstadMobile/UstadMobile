package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Represents a CrawlJob - where the app needs to walk through all the child entries of a given
 * OPDSEntry. This can be used for indexing and to prepare for a download.
 */
@UmEntity
public class CrawlJob {

    @UmPrimaryKey(autoIncrement = true)
    private Integer crawlJobId;

    private String rootEntryUri;

    private String rootEntryUuid;

    private int status;

    private int containersDownloadJobId = -1;

    private boolean recursive = true;

    private boolean queueDownloadJobOnDone = false;

    /**
     * Getter for the crawlJobId property
     *
     * @return The crawlJobId (primary key)
     */
    public Integer getCrawlJobId() {
        return crawlJobId;
    }

    /**
     * Setter for the crawlJobId property
     *
     * @param crawlJobId The crawlJobId (primary key)
     */
    public void setCrawlJobId(Integer crawlJobId) {
        this.crawlJobId = crawlJobId;
    }

    /**
     * Get the status of the CrawlJob
     *
     * @return The status of the CrawlJob, as an integer flag as per NetworkTask.STATUS flags
     */
    public int getStatus() {
        return status;
    }

    /**
     * Set the status of the CrawlJob
     *
     * @param status The status of the CrawlJob, as an integer flag as per NetworkTask.STATUS flags
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * If >= 0, containers that are found on this crawl will be added as DownloadJobItems to the
     * given download job
     *
     * @return The id of the download job that containers, discovered on this crawl, should be added to
     */
    public int getContainersDownloadJobId() {
        return containersDownloadJobId;
    }

    /**
     * If >= 0, containers that are found on this crawl will be added as DownloadJobItems to the
     * given download job
     *
     * @param containersDownloadJobId
     */
    public void setContainersDownloadJobId(int containersDownloadJobId) {
        this.containersDownloadJobId = containersDownloadJobId;
    }

    /**
     * Getter for the recursive property
     *
     * @return true if this Crawl should be recursive and iterate over child entries and OPDS subsection links
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * Setter for the recursive property
     *
     * @param recursive true if this Crawl should be recursive and iterate over child entries and OPDS subsection links
     */
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    /**
     * Setter for the queue download job on done property
     *
     * @return true if this download should be automatically queued when the CrawlTask is done
     */
    public boolean isQueueDownloadJobOnDone() {
        return queueDownloadJobOnDone;
    }

    /**
     * Getter for the queue download job on done property
     *
     * @param queueDownloadJobOnDone true if this download should be automatically queued when the CrawlTask is done
     */
    public void setQueueDownloadJobOnDone(boolean queueDownloadJobOnDone) {
        this.queueDownloadJobOnDone = queueDownloadJobOnDone;
    }

    /**
     * Get the root entry URI from which the crawl should begin. Can be null if the root entry UUID
     * is specified.
     *
     * @return The root entry URI from which the crawl should begin
     */
    public String getRootEntryUri() {
        return rootEntryUri;
    }

    /**
     * Set the root entry URI from which the crawl should begin. Can be null if the root entry UUID
     * is specified.
     *
     * @param rootEntryUri The root entry URI from which the crawl should begin
     */
    public void setRootEntryUri(String rootEntryUri) {
        this.rootEntryUri = rootEntryUri;
    }

    /**
     * Get the root entry OPDS uuid from which the crawl should begin. If null, the rootEntryUri
     * must not be null, that uri will be loaded, and the URI will be set accordingly.
     *
     * @return The root entry OPDS uuid
     */
    public String getRootEntryUuid() {
        return rootEntryUuid;
    }

    /**
     * Set the root entry OPDS uuid from which the crawl should begin. If null, the rootEntryUri
     * must not be null, that uri will be loaded, and the URI will be set accordingly.
     *
     * @param rootEntryUuid The root entry OPDS uuid
     */
    public void setRootEntryUuid(String rootEntryUuid) {
        this.rootEntryUuid = rootEntryUuid;
    }
}
