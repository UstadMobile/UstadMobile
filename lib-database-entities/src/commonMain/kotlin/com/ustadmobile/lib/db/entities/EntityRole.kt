package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.*

@UmEntity(tableId = 47)
@Entity
class EntityRole() {

    @PrimaryKey(autoGenerate = true)
    var erUid: Long = 0

    @UmSyncMasterChangeSeqNum
    var erMasterCsn: Long = 0

    @UmSyncLocalChangeSeqNum
    var erLocalCsn: Long = 0

    @UmSyncLastChangedBy
    var erLastChangedBy: Int = 0

    @UmIndexField
    @ColumnInfo(index = true)
    var erTableId: Int = 0

    @UmIndexField
    @ColumnInfo(index = true)
    var erEntityUid: Long = 0

    @UmIndexField
    @ColumnInfo(index = true)
    var erGroupUid: Long = 0

    @UmIndexField
    @ColumnInfo(index = true)
    var erRoleUid: Long = 0

    constructor(erTableId: Int, erEntityUid: Long, erGroupUid: Long, erRoleUid: Long) : this() {
        this.erTableId = erTableId
        this.erEntityUid = erEntityUid
        this.erGroupUid = erGroupUid
        this.erRoleUid = erRoleUid
    }

}
