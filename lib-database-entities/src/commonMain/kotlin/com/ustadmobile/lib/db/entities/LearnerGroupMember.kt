package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.LearnerGroupMember.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = TABLE_ID)
@Serializable
open class LearnerGroupMember {

    @PrimaryKey(autoGenerate = true)
    var learnerGroupMemberUid: Long = 0

    var learnerGroupMemberPersonUid: Long = 0

    var learnerGroupMemberLgUid: Long = 0

    var learnerGroupMemberRole: Int = PARTICIPANT_ROLE

    var learnerGroupMemberActive: Boolean = true

    @MasterChangeSeqNum
    var learnerGroupMemberMCSN: Long = 0

    @LocalChangeSeqNum
    var learnerGroupMemberCSN: Long = 0

    @LastChangedBy
    var learnerGroupMemberLCB: Int = 0

    companion object {

        const val TABLE_ID = 300

        const val PRIMARY_ROLE = 1

        const val PARTICIPANT_ROLE = 2

    }

}