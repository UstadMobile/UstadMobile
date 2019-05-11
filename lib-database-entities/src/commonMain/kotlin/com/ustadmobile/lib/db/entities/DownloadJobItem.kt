package com.ustadmobile.lib.db.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmIndexField
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

/**
 * A DownloadJobItem is a specific DownloadRun of a specific item. It corresponds with a given
 * DownloadSetItem representing the item to download, and a DownloadJob representing a specific
 * download run. Each DownloadSetItem can be downloaded multiple times (e.g. it can be downloaded,
 * updated, re-downloaded after the user deletes it, etc)
 */
@UmEntity
@Entity
open class DownloadJobItem {

    @UmPrimaryKey(autoIncrement = true)
    @PrimaryKey(autoGenerate = true)
    var djiUid: Long = 0

    var djiDsiUid: Long = 0

    var djiDjUid: Long = 0

    var djiContainerUid: Long = 0

    var djiContentEntryUid: Long = 0

    var downloadedSoFar: Long = 0

    var downloadLength: Long = 0

    var currentSpeed: Long = 0

    @UmIndexField
    var timeStarted: Long = 0

    var timeFinished: Long = 0

    @UmIndexField
    var djiStatus: Int = 0

    var destinationFile: String? = null

    var numAttempts: Int = 0


    constructor()

    constructor(src: DownloadJobItem) {
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

    constructor(djiDjUid: Long, djiContentEntryUid: Long, djiContainerUid: Long, downloadLength: Long) {
        this.djiDjUid = djiDjUid
        this.djiContentEntryUid = djiContentEntryUid
        this.djiContainerUid = djiContainerUid
        this.downloadLength = downloadLength
    }

    constructor(downloadJob: DownloadJob, djiContentEntryUid: Long, djiContainerUid: Long, downloadLength: Long) {
        djiDjUid = downloadJob.djUid
        this.djiContentEntryUid = djiContentEntryUid
        this.djiContainerUid = djiContainerUid
        this.downloadLength = downloadLength
    }

    constructor(downloadJob: DownloadJob, downloadSetItem: DownloadSetItem,
                container: Container) {
        this.djiDjUid = downloadJob.djUid
        this.djiDsiUid = downloadSetItem.dsiUid
        this.djiContainerUid = container.containerUid
    }




}
