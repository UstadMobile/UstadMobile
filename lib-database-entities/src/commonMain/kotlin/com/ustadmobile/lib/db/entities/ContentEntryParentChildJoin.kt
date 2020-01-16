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
import kotlinx.serialization.Serializable


/**
 * ContentEntry child - parent join entity
 */
//short code = cepcj
@Entity(indices = [Index(name = "parent_child", value = ["cepcjChildContentEntryUid", "cepcjParentContentEntryUid"])])
@SyncableEntity(tableId = TABLE_ID)
@Serializable
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


    companion object {

        const val TABLE_ID = 7
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ContentEntryParentChildJoin

        if (cepcjChildContentEntryUid != other.cepcjChildContentEntryUid) return false
        if (cepcjParentContentEntryUid != other.cepcjParentContentEntryUid) return false
        if (childIndex != other.childIndex) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cepcjChildContentEntryUid.hashCode()
        result = 31 * result + cepcjParentContentEntryUid.hashCode()
        result = 31 * result + childIndex
        return result
    }
}
