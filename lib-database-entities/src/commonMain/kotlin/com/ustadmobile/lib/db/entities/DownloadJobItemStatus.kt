package com.ustadmobile.lib.db.entities

import kotlin.jvm.Transient

/**
 * This is used as a memory efficient summary of the status of a download
 *
 */
class DownloadJobItemStatus {

    var jobItemUid: Int = 0

    @field:Transient
    var parents: MutableList<DownloadJobItemStatus>? = null
        private set

    var contentEntryUid: Long = 0

    var bytesSoFar: Long = 0

    var totalBytes: Long = 0

    var status: Byte = 0

    @field:Transient
    var children: MutableList<DownloadJobItemStatus>? = null
        private set

    constructor() {

    }

    constructor(item: DownloadJobItem) {
        jobItemUid = item.djiUid.toInt()
        contentEntryUid = item.djiContentEntryUid
        bytesSoFar = item.downloadedSoFar
        totalBytes = item.downloadLength
    }

    fun incrementTotalBytes(increment: Long) {
        totalBytes += increment
    }

    fun incrementBytesSoFar(increment: Long) {
        bytesSoFar += increment
    }

    fun addParent(parent: DownloadJobItemStatus) {
        if (parents == null)
            parents = mutableListOf()

        parents!!.add(parent)
    }

    fun addChild(child: DownloadJobItemStatus) {
        if (children == null)
            children = mutableListOf()

        children!!.add(child)
    }

    override fun hashCode(): Int {
        return jobItemUid
    }


    override fun equals(other: Any?): Boolean {
        return other != null && other is DownloadJobItemStatus && other.jobItemUid == this.jobItemUid
    }
}
