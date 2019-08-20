package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * DownloadJobItemHistory represents one attempt to download a given DownloadJobItem. It is used
 * to track the performance of different peers, and inform the selection of peers when attempting to
 * download items in the future.
 */
@Entity
class DownloadJobItemHistory() {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var url: String? = null

    //Foreign key for the networknode this is connected to
    var networkNode: Long = 0

    var downloadJobItemId: Int = 0

    var mode: Int = 0

    var numBytes: Long = 0

    var successful: Boolean = false

    var startTime: Long = 0

    var endTime: Long = 0

    constructor(networkNode: Long, mode: Int, successful: Boolean, startTime: Long, endTime: Long) : this() {
        this.networkNode = networkNode
        this.mode = mode
        this.successful = successful
        this.startTime = startTime
        this.endTime = endTime
    }

    constructor(node: NetworkNode?, item: DownloadJobItem?, mode: Int, startTime: Long) : this() {
        if (node != null)
            networkNode = node.nodeId

        if (item != null)
            downloadJobItemId = item.djiUid

        this.mode = mode
        this.startTime = startTime
    }

    companion object {

        const val MODE_CLOUD = 1

        const val MODE_LOCAL = 2
    }
}
