package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.ClazzAssignmentContentJoin.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = 204)
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


}
