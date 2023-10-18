package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@Serializable
@EntityWithAttachment
@ReplicateEntity(
    tableId = ContentEntryPicture.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(arrayOf(
    Trigger(
        name = "ceppicture_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
        sqlStatements = [TRIGGER_UPSERT],
    )
))
open class ContentEntryPicture() {

    @PrimaryKey(autoGenerate = true)
    var cepUid: Long = 0

    var cepContentEntryUid: Long = 0

    @AttachmentUri
    var cepUri: String? = null

    @AttachmentMd5
    var cepMd5: String? = null

    @AttachmentSize
    var cepFileSize: Int = 0

    @ReplicateLastModified
    @ReplicateEtag
    var cepTimestamp: Long = 0

    var cepMimeType: String? = null

    var cepActive: Boolean = true

    companion object {

        const val TABLE_ID = 138
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ContentEntryPicture

        if (cepUid != other.cepUid) return false
        if (cepContentEntryUid != other.cepContentEntryUid) return false
        if (cepUri != other.cepUri) return false
        if (cepMd5 != other.cepMd5) return false
        if (cepFileSize != other.cepFileSize) return false
        if (cepTimestamp != other.cepTimestamp) return false
        if (cepMimeType != other.cepMimeType) return false
        if (cepActive != other.cepActive) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cepUid.hashCode()
        result = 31 * result + cepContentEntryUid.hashCode()
        result = 31 * result + (cepUri?.hashCode() ?: 0)
        result = 31 * result + (cepMd5?.hashCode() ?: 0)
        result = 31 * result + cepFileSize
        result = 31 * result + cepTimestamp.hashCode()
        result = 31 * result + (cepMimeType?.hashCode() ?: 0)
        result = 31 * result + cepActive.hashCode()
        return result
    }


}
