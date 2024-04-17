package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * To be used by BlobUploadClient and content downloader. Provides database storage so that:
 *  - The job can be enqueued and run at an appropriate time (e.g. when connectivity is available)
 *  - The job can be interrupted and resumed
 *  - The progress of the job can be displayed to the user.
 *
 * A TransferJob has one or more TransferJobItem(s).
 *
 * @param tjStatus Status int as per TransferJobItemStatus
 * @param tjTableId Where this transfer job is associated with one specific entity, the tableId (optional)
 * @param tjEntityUid Where this transfer job is associated with one specific entity, the entity uid field (optional)
 */
@Entity(
    indices = arrayOf(
        Index("tjTableId", "tjEntityUid", name = "TransferJob_idx_tjTableId_EntityUid")
    )
)
/**
 * @param tjOiUid Id of the related OfflineItem (if any)
 */
@Serializable
data class TransferJob(
    @PrimaryKey(autoGenerate = true)
    var tjUid: Int = 0,
    var tjType: Int = 0,
    var tjStatus: Int = 0,
    var tjName: String? = null,
    var tjUuid: String? = null,
    @ColumnInfo(defaultValue = "0")
    var tjTableId: Int = 0,
    @ColumnInfo(defaultValue = "0")
    var tjEntityUid: Long = 0,
    @ColumnInfo(defaultValue = "0")
    var tjTimeCreated: Long = 0,
    @ColumnInfo(defaultValue = "0")
    var tjCreationType: Int = 0,

    @ColumnInfo(defaultValue = "0")
    var tjOiUid: Long = 0,
) {
    companion object {
        //This entity is not replicated, however, this can be used as part of the key in lists
        const val TABLE_ID = 1081

        const val TYPE_BLOB_UPLOAD = 1

        const val TYPE_DOWNLOAD = 2


        const val CREATION_TYPE_USER = 1

        const val CREATION_TYPE_UPDATE = 2
    }

}