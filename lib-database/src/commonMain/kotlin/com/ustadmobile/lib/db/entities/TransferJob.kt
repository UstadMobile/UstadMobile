package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * To be used by BlobUploadClient and content downloader. Provides database storage so that:
 *  - The job can be enqueued and run at an appropriate time (e.g. when connectivity is available)
 *  - The job can be interrupted and resumed
 *  - The progress of the job can be displayed to the user.
 *
 * A TransferJob has one or more TransferJobItem(s).
 */
@Entity
data class TransferJob(
    @PrimaryKey(autoGenerate = true)
    var tjUid: Int = 0,
    var tjType: Int = 0,
    var tjStatus: Int = 0,
    var tjName: String? = null,
    var tjUuid: String? = null,
) {
    companion object {

        @Suppress("unused")
        const val TYPE_BLOB_UPLOAD = 1

        @Suppress("unused")
        const val TYPE_DOWNLOAD = 2

    }

}