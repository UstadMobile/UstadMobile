package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = ClazzWorkContentJoin.TABLE_ID)
@Serializable
open class ClazzWorkContentJoin() {

    @PrimaryKey(autoGenerate = true)
    var clazzWorkContentJoinUid: Long = 0

    var clazzWorkContentJoinContentUid : Long = 0

    var clazzWorkContentJoinClazzWorkUid : Long = 0

    var clazzWorkContentJoinInactive : Boolean = false

    var clazzWorkContentJoinDateAdded : Long = 0

    @MasterChangeSeqNum
    var clazzWorkContentJoinMCSN: Long = 0

    @LocalChangeSeqNum
    var clazzWorkContentJoinLCSN: Long = 0

    @LastChangedBy
    var clazzWorkContentJoinLCB: Int = 0

    @LastChangedTime
    var clazzWorkContentJoinLct: Long = 0

    companion object {

        const val TABLE_ID = 204

    }

}
