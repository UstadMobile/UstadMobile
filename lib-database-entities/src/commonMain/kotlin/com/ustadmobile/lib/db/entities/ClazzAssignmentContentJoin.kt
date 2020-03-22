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
@SyncableEntity(tableId = TABLE_ID)
@Serializable
open class ClazzAssignmentContentJoin() {

    @PrimaryKey(autoGenerate = true)
    var clazzAssignmentContentJoinUid: Long = 0

    //ContentEntry
    var clazzAssignmentContentJoinContentUid : Long = 0

    //ClazzAssignment
    var clazzAssignmentContentJoinClazzAssignmentUid : Long = 0

    //Inactive flag. Default to false (everything that persists will be active & shown)
    var clazzAssignmentContentJoinInactive : Boolean = false

    var clazzAssignmentContentJoinDateAdded : Long = 0

    @MasterChangeSeqNum
    var clazzAssignmentContentJoinMCSN: Long = 0

    @LocalChangeSeqNum
    var clazzAssignmentContentJoinLCSN: Long = 0

    @LastChangedBy
    var clazzAssignmentContentJoinLCB: Int = 0

    companion object {

        const val TABLE_ID = 177
    }
}
