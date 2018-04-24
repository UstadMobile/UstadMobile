package com.ustadmobile.lib.db.entities;

/**
 * A DownloadJob with the total number of items and the total number of bytes to be download by
 * all DownloadJobItem entities in the DownloadJob. This is used with DAO method SQL that uses
 * the SQL sum function.
 */
public class DownloadJobWithTotals extends DownloadJob{

    private int numJobItems;

    private long totalDownloadSize;

    /**
     * Get the total number of DownloadJobItem entities that are part of this job
     * @return the total number of DownloadJobItem entities that are part of this job
     */
    public int getNumJobItems() {
        return numJobItems;
    }

    /**
     * Set the total number of DownloadJobItem entities that are part of this job
     *
     * @param numJobItems the total number of DownloadJobItem entities that are part of this job
     */
    public void setNumJobItems(int numJobItems) {
        this.numJobItems = numJobItems;
    }

    /**
     * Get the total size (sum of downloadLength) of all DownloadJobItem entities in this DownloadJob (in bytes)
     *
     * @return the total size (sum of downloadLength) of all DownloadJobItem entities in this DownloadJob (in bytes)
     */
    public long getTotalDownloadSize() {
        return totalDownloadSize;
    }

    /**
     * Set the total size (sum of downloadLength) of all DownloadJobItem entities in this DownloadJob (in bytes)
     * @param totalDownloadSize the total size (sum of downloadLength) of all DownloadJobItem entities in this DownloadJob (in bytes)
     */
    public void setTotalDownloadSize(long totalDownloadSize) {
        this.totalDownloadSize = totalDownloadSize;
    }


}
