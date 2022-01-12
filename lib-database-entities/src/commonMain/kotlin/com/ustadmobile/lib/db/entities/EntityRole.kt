package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable


@Entity(indices =[
    //Index to provide fields required in permission queries
    Index(value = ["erGroupUid", "erRoleUid", "erTableId"])
])
@Serializable
@Deprecated("Replaced with ScopedGrant")
open class EntityRole() {

    @PrimaryKey(autoGenerate = true)
    var erUid: Long = 0

    @MasterChangeSeqNum
    var erMasterCsn: Long = 0

    @LocalChangeSeqNum
    var erLocalCsn: Long = 0

    @LastChangedBy
    var erLastChangedBy: Int = 0

    @LastChangedTime
    var erLct: Long = 0

    @ColumnInfo(index = true)
    var erTableId: Int = 0

    @ColumnInfo(index = true)
    var erEntityUid: Long = 0

    @ColumnInfo(index = true)
    var erGroupUid: Long = 0

    @ColumnInfo(index = true)
    var erRoleUid: Long = 0

    var erActive: Boolean = false

    constructor(erTableId: Int, erEntityUid: Long, erGroupUid: Long, erRoleUid: Long) : this() {
        this.erTableId = erTableId
        this.erEntityUid = erEntityUid
        this.erGroupUid = erGroupUid
        this.erRoleUid = erRoleUid
        this.erActive = true
    }

    companion object {
        const val TABLE_ID = 47
    }

}
