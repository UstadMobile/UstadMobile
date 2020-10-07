package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.GroupLearningSession.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = TABLE_ID)
@Serializable
class GroupLearningSession {

    @PrimaryKey(autoGenerate = true)
    var groupLearningSessionUid: Long = 0

    var groupLearningSessionContentUid : Long = 0

    var groupLearningSessionLearnerGroupUid : Long = 0

    var groupLearningSessionInactive : Boolean = false

    @MasterChangeSeqNum
    var groupLearningSessionMCSN: Long = 0

    @LocalChangeSeqNum
    var groupLearningSessionCSN: Long = 0

    @LastChangedBy
    var groupLearningSessionLCB: Int = 0


    companion object {

        const val TABLE_ID = 302

    }
}