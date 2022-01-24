package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin.Companion.TABLE_ID
import kotlinx.serialization.Serializable


/**
 * Represents a relationship between two ContentEntry items. This could be that one ContentEntry is
 * the translated version of another ContentEntry (relType = REL_TYPE_TRANSLATED_VERSION), or it
 * could be that the other entry is a see also link.
 */
//shortcode cerej
@Entity
@ReplicateEntity(tableId = TABLE_ID, tracker = ContentEntryRelatedEntryJoinReplicate::class)
@Serializable
@Triggers(arrayOf(
     Trigger(
         name = "contententryrelatedentryjoin_remote_insert",
         order = Trigger.Order.INSTEAD_OF,
         on = Trigger.On.RECEIVEVIEW,
         events = [Trigger.Event.INSERT],
         sqlStatements = [
             """REPLACE INTO ContentEntryRelatedEntryJoin(cerejUid, cerejContentEntryUid, cerejRelatedEntryUid, cerejLastChangedBy, relType, comment, cerejRelLanguageUid, cerejLocalChangeSeqNum, cerejMasterChangeSeqNum, cerejLct) 
             VALUES (NEW.cerejUid, NEW.cerejContentEntryUid, NEW.cerejRelatedEntryUid, NEW.cerejLastChangedBy, NEW.relType, NEW.comment, NEW.cerejRelLanguageUid, NEW.cerejLocalChangeSeqNum, NEW.cerejMasterChangeSeqNum, NEW.cerejLct) 
             /*psql ON CONFLICT (cerejUid) DO UPDATE 
             SET cerejContentEntryUid = EXCLUDED.cerejContentEntryUid, cerejRelatedEntryUid = EXCLUDED.cerejRelatedEntryUid, cerejLastChangedBy = EXCLUDED.cerejLastChangedBy, relType = EXCLUDED.relType, comment = EXCLUDED.comment, cerejRelLanguageUid = EXCLUDED.cerejRelLanguageUid, cerejLocalChangeSeqNum = EXCLUDED.cerejLocalChangeSeqNum, cerejMasterChangeSeqNum = EXCLUDED.cerejMasterChangeSeqNum, cerejLct = EXCLUDED.cerejLct
             */"""
         ]
     )
))
open class ContentEntryRelatedEntryJoin() {


    @PrimaryKey(autoGenerate = true)
    var cerejUid: Long = 0

    var cerejContentEntryUid: Long = 0

    var cerejRelatedEntryUid: Long = 0

    @LastChangedBy
    var cerejLastChangedBy: Int = 0

    var relType: Int = 0

    var comment: String? = null

    var cerejRelLanguageUid: Long = 0

    @LocalChangeSeqNum
    var cerejLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var cerejMasterChangeSeqNum: Long = 0

    @LastChangedTime
    @ReplicationVersionId
    var cerejLct: Long = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        val that = other as ContentEntryRelatedEntryJoin?

        if (cerejUid != that!!.cerejUid) return false
        if (cerejContentEntryUid != that.cerejContentEntryUid) return false
        if (cerejRelatedEntryUid != that.cerejRelatedEntryUid) return false
        if (relType != that.relType) return false
        if (cerejRelLanguageUid != that.cerejRelLanguageUid) return false
        return if (comment != null) comment == that.comment else that.comment == null
    }

    override fun hashCode(): Int {
        var result = (cerejUid xor cerejUid.ushr(32)).toInt()
        result = 31 * result + (cerejContentEntryUid xor cerejContentEntryUid.ushr(32)).toInt()
        result = 31 * result + (cerejRelatedEntryUid xor cerejRelatedEntryUid.ushr(32)).toInt()
        result = 31 * result + relType
        result = 31 * result + if (comment != null) comment!!.hashCode() else 0
        result = 31 * result + (cerejRelLanguageUid xor cerejRelLanguageUid.ushr(32)).toInt()
        return result
    }

    companion object {

        const val TABLE_ID = 8

        const val REL_TYPE_TRANSLATED_VERSION = 1

        const val REL_TYPE_SEE_ALSO = 2
    }
}
