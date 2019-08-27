package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin.Companion.TABLE_ID


/**
 * Represents a relationship between two ContentEntry items. This could be that one ContentEntry is
 * the translated version of another ContentEntry (relType = REL_TYPE_TRANSLATED_VERSION), or it
 * could be that the other entry is a see also link.
 */
//shortcode cerej
@Entity
@SyncableEntity(tableId = TABLE_ID)
class ContentEntryRelatedEntryJoin() {


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
