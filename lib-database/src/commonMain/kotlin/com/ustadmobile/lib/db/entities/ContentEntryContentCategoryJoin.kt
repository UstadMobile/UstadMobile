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
@ReplicateEntity(tableId = TABLE_ID, tracker = ContentEntryContentCategoryJoinReplicate::class)
@Serializable
@Triggers(arrayOf(
 Trigger(
     name = "contententrycontentcategoryjoin_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO ContentEntryContentCategoryJoin(ceccjUid, ceccjContentEntryUid, ceccjContentCategoryUid, ceccjLocalChangeSeqNum, ceccjMasterChangeSeqNum, ceccjLastChangedBy, ceccjLct) 
         VALUES (NEW.ceccjUid, NEW.ceccjContentEntryUid, NEW.ceccjContentCategoryUid, NEW.ceccjLocalChangeSeqNum, NEW.ceccjMasterChangeSeqNum, NEW.ceccjLastChangedBy, NEW.ceccjLct) 
         /*psql ON CONFLICT (ceccjUid) DO UPDATE 
         SET ceccjContentEntryUid = EXCLUDED.ceccjContentEntryUid, ceccjContentCategoryUid = EXCLUDED.ceccjContentCategoryUid, ceccjLocalChangeSeqNum = EXCLUDED.ceccjLocalChangeSeqNum, ceccjMasterChangeSeqNum = EXCLUDED.ceccjMasterChangeSeqNum, ceccjLastChangedBy = EXCLUDED.ceccjLastChangedBy, ceccjLct = EXCLUDED.ceccjLct
         */"""
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

    @LastChangedTime
    @ReplicationVersionId
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
