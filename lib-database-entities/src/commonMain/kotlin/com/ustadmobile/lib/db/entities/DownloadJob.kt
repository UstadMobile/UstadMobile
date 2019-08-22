package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A DownloadJob represents a specific run of downloading a DownloadSet. The DownloadSet contains
 * the list of entries that are to be downloaded. One DownloadSet can have multiple DownloadJobs, e.g.
 * one DownloadJob that initially downloads it, and then further DownloadJobs when it is updated, when
 * new entries become available, etc.
 */
@Entity
open class DownloadJob() {

    @PrimaryKey(autoGenerate = true)
    var djUid: Int = 0

    var djDsUid: Int = 0

    var timeCreated: Long = 0

    var timeRequested: Long = 0

    var timeCompleted: Long = 0

    var totalBytesToDownload: Long = 0

    var bytesDownloadedSoFar: Long = 0

    /**
     * Status as per flags on NetworkTask
     */
    var djStatus: Int = 0

    var meteredNetworkAllowed: Boolean = false

    var djRootContentEntryUid: Long = 0

    var djDestinationDir: String? = null


    /**
     * Empty constructor
     */
    constructor(contentEntryEntryUid: Long, timeCreated: Long) : this() {
        djRootContentEntryUid = contentEntryEntryUid
        this.timeCreated = timeCreated
    }
}
