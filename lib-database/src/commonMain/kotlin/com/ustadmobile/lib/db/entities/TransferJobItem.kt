package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * @param tjiSrc: the source of the TransferJobItem - the value of the string depends on the type
 * @param tjiDest: the destination of the TransferJobItem - the value of the string depends on the type
 * @param tjiTableId if not zero, BlobUploadClientUseCase will insert an OutgoingReplication when the
 *        TransferJobItem is complete. This can be useful when handling the upload of blobs that
 *        are associated with entities in the database such as PersonPicture. This will update the
 *        uri on the server after the blob itself is successfully uploaded.
 * @param tjiEntityUid used with tjiTableId
 * @param tjiEntityEtag the etag (e.g. field annotated @ReplicateEtag) of the entity for which this
 *        transfer is being performed. This ensures that when TransferJobItem is queried in order to
 *        display the status of an item for the user it only returns relevant status e.g.
 *        if a previous version failed/succeeded, but it was since replaced, the status of the
 *        transfer for the previous version is no longer relevant.
 * @param tjiLockIdToRelease when an upload is finished, then the retention lock that was created to
 *        prevent its eviction from the cache before upload is finished should be cleared.
 */
@Entity(
    indices = arrayOf(
        //This table is commonly searched by tableid/entityuid/etag
        Index("tjiTableId", "tjiEntityUid", "tjiEntityEtag", name = "tji_table_entity_etag"),
        //Index when searched by TransferJobUid
        Index("tjiTjUid", name="transferjob_tjuid")
    )
)
@Serializable
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

    @ColumnInfo(defaultValue = "0")
    var tjiLockIdToRelease: Int = 0,
)
