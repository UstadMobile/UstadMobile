package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ContentEntryContentCategoryJoin.Companion.TABLE_ID
import kotlinx.serialization.Serializable


/**
 * Join entity to link ContentEntry many:many with ContentCategory
 */
@Entity
@ReplicateEntity(
    tableId = TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Serializable
@Triggers(arrayOf(
 Trigger(
     name = "contententrycontentcategoryjoin_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
        TRIGGER_UPSERT_WHERE_NEWER
     ]
 )
))
class ContentEntryContentCategoryJoin() {

    @PrimaryKey(autoGenerate = true)
    var ceccjUid: Long = 0

    @ColumnInfo(index = true)
    var ceccjContentEntryUid: Long = 0

    var ceccjContentCategoryUid: Long = 0

    @LocalChangeSeqNum
    var ceccjLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var ceccjMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var ceccjLastChangedBy: Int = 0

    @ReplicateLastModified
    @ReplicateEtag
    var ceccjLct: Long = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        val that = other as ContentEntryContentCategoryJoin?

        if (ceccjUid != that!!.ceccjUid) return false
        return if (ceccjContentEntryUid != that.ceccjContentEntryUid) false else ceccjContentCategoryUid == that.ceccjContentCategoryUid
    }

    override fun hashCode(): Int {
        var result = (ceccjUid xor ceccjUid.ushr(32)).toInt()
        result = 31 * result + (ceccjContentEntryUid xor ceccjContentEntryUid.ushr(32)).toInt()
        result = 31 * result + (ceccjContentCategoryUid xor ceccjContentCategoryUid.ushr(32)).toInt()
        return result
    }

    companion object {

        const val TABLE_ID = 3
    }
}
