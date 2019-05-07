package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmIndexField
import com.ustadmobile.lib.database.annotation.UmPrimaryKey
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

import com.ustadmobile.lib.db.entities.ContentEntryContentCategoryJoin.Companion.TABLE_ID


/**
 * Join entity to link ContentEntry many:many with ContentCategory
 */
@UmEntity(tableId = TABLE_ID)
class ContentEntryContentCategoryJoin {

    @UmPrimaryKey(autoGenerateSyncable = true)
    var ceccjUid: Long = 0

    //TODO: Migration
    @UmIndexField
    var ceccjContentEntryUid: Long = 0

    var ceccjContentCategoryUid: Long = 0

    @UmSyncLocalChangeSeqNum
    var ceccjLocalChangeSeqNum: Long = 0

    @UmSyncMasterChangeSeqNum
    var ceccjMasterChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
    var ceccjLastChangedBy: Int = 0

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val that = o as ContentEntryContentCategoryJoin?

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
