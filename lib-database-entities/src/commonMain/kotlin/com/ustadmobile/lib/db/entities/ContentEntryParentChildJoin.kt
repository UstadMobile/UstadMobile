package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.database.annotation.*
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin.Companion.TABLE_ID


/**
 * ContentEntry child - parent join entity
 */
//short code = cepcj
@Entity(indices = [Index(name = "parent_child", value = ["cepcjChildContentEntryUid", "cepcjParentContentEntryUid"])])
@SyncableEntity(tableId = TABLE_ID)
class ContentEntryParentChildJoin() {

    @PrimaryKey(autoGenerate = true)
    var cepcjUid: Long = 0

    @ColumnInfo(index = true)
    var cepcjChildContentEntryUid: Long = 0

    @ColumnInfo(index = true)
    var cepcjParentContentEntryUid: Long = 0

    var childIndex: Int = 0

    @LocalChangeSeqNum
    var cepcjLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var cepcjMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var cepcjLastChangedBy: Int = 0

    constructor(parentEntry: ContentEntry, childEntry: ContentEntry,
                childIndex: Int) : this() {
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
