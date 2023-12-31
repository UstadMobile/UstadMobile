package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @param tjiSrc: the source of the TransferJobItem - the value of the string depends on the type
 * @param tjiDest: the destination of the TransferJobItem - the value of the string depends on the type
 * @param tjiTableId if not zero, BlobUploadClientUseCase will insert an OutgoingReplication when the
 *        TransferJobItem is complete. This can be useful when handling the upload of blobs that
 *        are associated with entities in the database such as PersonPicture. This will update the
 *        uri on the server after the blob itself is successfully uploaded.
 * @param tjiEntityUid used with tjiTableId
 */
@Entity
data class TransferJobItem(
    @PrimaryKey(autoGenerate = true)
    var tjiUid: Int = 0,

    var tjiTjUid: Int = 0,

    var tjTotalSize: Long = 0,

    var tjTransferred: Long = 0,

    var tjAttemptCount: Int = 0,

    var tjiSrc: String? = null,

    var tjiDest: String? = null,

    var tjiType: Int = 0,

    var tjiStatus: Int = 0,

    var tjiTableId: Int = 0,

    var tjiEntityUid: Long = 0,

    //This should be set when the transferjobitem is created - by query.
    @ColumnInfo(defaultValue = "0")
    var tjiEntityEtag: Long = 0,
)
