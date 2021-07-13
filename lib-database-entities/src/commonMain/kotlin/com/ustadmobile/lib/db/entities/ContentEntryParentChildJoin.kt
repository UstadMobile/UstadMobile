package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin.Companion.TABLE_ID
import kotlinx.serialization.Serializable


/**
 * ContentEntry child - parent join entity
 */
//short code = cepcj
@Entity(indices = [Index(name = "parent_child", value = ["cepcjChildContentEntryUid", "cepcjParentContentEntryUid"])])
@SyncableEntity(tableId = TABLE_ID,
        notifyOnUpdate = ["""
        SELECT DISTINCT UserSession.usClientNodeId AS deviceId, 
               $TABLE_ID AS tableId 
          FROM UserSession 
    """])
@Serializable
class ContentEntryParentChildJoin(
    @ColumnInfo(index = true)
    var cepcjParentContentEntryUid: Long = 0,

    @ColumnInfo(index = true)
    var cepcjChildContentEntryUid: Long = 0,

    var childIndex: Int = 0) {

    @PrimaryKey(autoGenerate = true)
    var cepcjUid: Long = 0

    @LocalChangeSeqNum
    var cepcjLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var cepcjMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var cepcjLastChangedBy: Int = 0

    @LastChangedTime
    var cepcjLct: Long = 0

    constructor(parentEntry: ContentEntry, childEntry: ContentEntry, index: Int) : this(){
        cepcjParentContentEntryUid = parentEntry.contentEntryUid
        cepcjChildContentEntryUid = childEntry.contentEntryUid
        childIndex = index
    }

    companion object {

        const val TABLE_ID = 7
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ContentEntryParentChildJoin

        if (cepcjUid != other.cepcjUid) return false
        if (cepcjLocalChangeSeqNum != other.cepcjLocalChangeSeqNum) return false
        if (cepcjMasterChangeSeqNum != other.cepcjMasterChangeSeqNum) return false
        if (cepcjLastChangedBy != other.cepcjLastChangedBy) return false
        if (cepcjParentContentEntryUid != other.cepcjParentContentEntryUid) return false
        if (cepcjChildContentEntryUid != other.cepcjChildContentEntryUid) return false
        if (childIndex != other.childIndex) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cepcjUid.hashCode()
        result = 31 * result + cepcjLocalChangeSeqNum.hashCode()
        result = 31 * result + cepcjMasterChangeSeqNum.hashCode()
        result = 31 * result + cepcjLastChangedBy
        result = 31 * result + cepcjParentContentEntryUid.hashCode()
        result = 31 * result + cepcjChildContentEntryUid.hashCode()
        result = 31 * result + childIndex
        return result
    }
}
