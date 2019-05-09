package com.ustadmobile.lib.db.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.*
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin.Companion.TABLE_ID


/**
 * ContentEntry child - parent join entity
 */
//short code = cepcj
@UmEntity(tableId = TABLE_ID/*, indices = [UmIndex(name = "parent_child", value = ["cepcjChildContentEntryUid", "cepcjParentContentEntryUid"])]*/)
@Entity(/*indices = [Index(name = "parent_child", value = ["cepcjChildContentEntryUid", "cepcjParentContentEntryUid"])]*/)
class ContentEntryParentChildJoin {

    @UmPrimaryKey(autoGenerateSyncable = true)
    @PrimaryKey
    var cepcjUid: Long = 0


    var cepcjChildContentEntryUid: Long = 0


    var cepcjParentContentEntryUid: Long = 0

    var childIndex: Int = 0

    @UmSyncLocalChangeSeqNum
    var cepcjLocalChangeSeqNum: Long = 0

    @UmSyncMasterChangeSeqNum
    var cepcjMasterChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
    var cepcjLastChangedBy: Int = 0

    constructor()

    constructor(parentEntry: ContentEntry, childEntry: ContentEntry,
                childIndex: Int) {
        this.cepcjParentContentEntryUid = parentEntry.contentEntryUid
        this.cepcjChildContentEntryUid = childEntry.contentEntryUid
        this.childIndex = childIndex
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        val that = other as ContentEntryParentChildJoin?

        if (cepcjUid != that!!.cepcjUid) return false
        if (cepcjChildContentEntryUid != that.cepcjChildContentEntryUid) return false
        return if (cepcjParentContentEntryUid != that.cepcjParentContentEntryUid) false else childIndex == that.childIndex
    }

    override fun hashCode(): Int {
        var result = (cepcjUid xor cepcjUid.ushr(32)).toInt()
        result = 31 * result + (cepcjChildContentEntryUid xor cepcjChildContentEntryUid.ushr(32)).toInt()
        result = 31 * result + (cepcjParentContentEntryUid xor cepcjParentContentEntryUid.ushr(32)).toInt()
        result = 31 * result + childIndex
        return result
    }

    companion object {

        const val TABLE_ID = 7
    }
}
