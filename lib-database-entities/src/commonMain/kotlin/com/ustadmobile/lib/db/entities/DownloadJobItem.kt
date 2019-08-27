package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A DownloadJobItem is a specific DownloadRun of a specific item. It corresponds with a given
 * DownloadSetItem representing the item to download, and a DownloadJob representing a specific
 * download run. Each DownloadSetItem can be downloaded multiple times (e.g. it can be downloaded,
 * updated, re-downloaded after the user deletes it, etc)
 */
@Entity
open class DownloadJobItem() {

    @PrimaryKey(autoGenerate = true)
    var djiUid: Int = 0

    var djiDsiUid: Int = 0

    var djiDjUid: Int = 0

    var djiContainerUid: Long = 0

    var djiContentEntryUid: Long = 0

    var downloadedSoFar: Long = 0

    var downloadLength: Long = 0

    var currentSpeed: Long = 0

    @ColumnInfo(index = true)
    var timeStarted: Long = 0

    var timeFinished: Long = 0

    @ColumnInfo(index = true)
    var djiStatus: Int = 0

    var destinationFile: String? = null

    var numAttempts: Int = 0

    constructor(src: DownloadJobItem) : this() {
        djiUid = src.djiUid
        downloadLength = src.downloadLength
        downloadedSoFar = src.downloadedSoFar
        djiContentEntryUid = src.djiContentEntryUid
        djiDjUid = src.djiDjUid
        djiStatus = src.djiStatus
        djiContainerUid = src.djiContainerUid
        currentSpeed = src.currentSpeed
        destinationFile = src.destinationFile
        numAttempts = src.numAttempts
    }

    constructor(djiDjUid: Int, djiContentEntryUid: Long, djiContainerUid: Long, downloadLength: Long) : this() {
        this.djiDjUid = djiDjUid
        this.djiContentEntryUid = djiContentEntryUid
        this.djiContainerUid = djiContainerUid
        this.downloadLength = downloadLength
    }

    constructor(downloadJob: DownloadJob, djiContentEntryUid: Long, djiContainerUid: Long, downloadLength: Long) : this() {
        djiDjUid = downloadJob.djUid
        this.djiContentEntryUid = djiContentEntryUid
        this.djiContainerUid = djiContainerUid
        this.downloadLength = downloadLength
    }


}
