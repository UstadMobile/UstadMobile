package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

/**
 * A DownloadJob with the total number of items and the total number of bytes to be download by
 * all DownloadJobItem entities in the DownloadJob. This is used with DAO method SQL that uses
 * the SQL sum function.
 */
@Serializable
class DownloadJobWithTotals() : DownloadJob() {

    /**
     * Get the total number of DownloadJobItem entities that are part of this job
     * @return the total number of DownloadJobItem entities that are part of this job
     */
    /**
     * Set the total number of DownloadJobItem entities that are part of this job
     *
     * @param numJobItems the total number of DownloadJobItem entities that are part of this job
     */
    var numJobItems: Int = 0

    /**
     * Get the total size (sum of downloadLength) of all DownloadJobItem entities in this DownloadJob (in bytes)
     *
     * @return the total size (sum of downloadLength) of all DownloadJobItem entities in this DownloadJob (in bytes)
     */
    /**
     * Set the total size (sum of downloadLength) of all DownloadJobItem entities in this DownloadJob (in bytes)
     * @param totalDownloadSize the total size (sum of downloadLength) of all DownloadJobItem entities in this DownloadJob (in bytes)
     */
    var totalDownloadSize: Long = 0


}
