package com.ustadmobile.lib.db.entities

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
open class DownloadJobItem {

    @UmPrimaryKey(autoIncrement = true)
    var djiUid: Long = 0

    var djiDsiUid: Long = 0

    var djiDjUid: Long = 0

    @get:Deprecated("")
    @set:Deprecated("")
    var djiContentEntryFileUid: Long = 0

    var djiContainerUid: Long = 0

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

    constructor(downloadJob: DownloadJob, downloadSetItem: DownloadSetItem,
                container: Container) {
        this.djiDjUid = downloadJob.djUid
        this.djiDsiUid = downloadSetItem.dsiUid
        this.djiContainerUid = container.containerUid
    }
}
