package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ClazzContentJoin.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = TABLE_ID,
        notifyOnUpdate = ["""
        SELECT DISTINCT UserSession.usClientNodeId AS deviceId, 
               $TABLE_ID AS tableId
          FROM UserSession 
    """])
@Serializable
class ClazzContentJoin  {

    @PrimaryKey(autoGenerate = true)
    var ccjUid: Long = 0

    @ColumnInfo(index = true)
    var ccjContentEntryUid: Long = 0

    var ccjClazzUid: Long = 0

    var ccjActive: Boolean = true

    @LocalChangeSeqNum
    var ccjLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var ccjMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var ccjLastChangedBy: Int = 0

    @LastChangedTime
    var ccjLct: Long = 0

    companion object {

        const val TABLE_ID = 134

    }

}