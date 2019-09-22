package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = 47)
@Serializable
class EntityRole() {

    @PrimaryKey(autoGenerate = true)
    var erUid: Long = 0

    @MasterChangeSeqNum
    var erMasterCsn: Long = 0

    @LocalChangeSeqNum
    var erLocalCsn: Long = 0

    @LastChangedBy
    var erLastChangedBy: Int = 0

    @ColumnInfo(index = true)
    var erTableId: Int = 0

    @ColumnInfo(index = true)
    var erEntityUid: Long = 0

    @ColumnInfo(index = true)
    var erGroupUid: Long = 0

    @ColumnInfo(index = true)
    var erRoleUid: Long = 0

    constructor(erTableId: Int, erEntityUid: Long, erGroupUid: Long, erRoleUid: Long) : this() {
        this.erTableId = erTableId
        this.erEntityUid = erEntityUid
        this.erGroupUid = erGroupUid
        this.erRoleUid = erRoleUid
    }

}
