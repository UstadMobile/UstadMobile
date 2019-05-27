package com.ustadmobile.lib.db.entities

import android.arch.persistence.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.*
import com.ustadmobile.lib.db.entities.ContentEntryContentCategoryJoin.Companion.TABLE_ID


/**
 * Join entity to link ContentEntry many:many with ContentCategory
 */
@UmEntity(tableId = TABLE_ID)
@Entity
class ContentEntryContentCategoryJoin() {

    @UmPrimaryKey(autoGenerateSyncable = true)
    @PrimaryKey
    var ceccjUid: Long = 0

    @UmIndexField
    @ColumnInfo(index = true)
    var ceccjContentEntryUid: Long = 0

    var ceccjContentCategoryUid: Long = 0

    @UmSyncLocalChangeSeqNum
    var ceccjLocalChangeSeqNum: Long = 0

    @UmSyncMasterChangeSeqNum
    var ceccjMasterChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
    var ceccjLastChangedBy: Int = 0

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
