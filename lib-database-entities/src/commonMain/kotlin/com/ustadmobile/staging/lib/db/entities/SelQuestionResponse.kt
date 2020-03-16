package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

/**
 * Represents the students response to a specific question in the question set
 */
@SyncableEntity(tableId = 23)
@Entity
@Serializable
class SelQuestionResponse {

    @PrimaryKey(autoGenerate = true)
    var selQuestionResponseUid: Long = 0

    // -> SelQuestionSetResponse
    var selQuestionResponseSelQuestionSetResponseUid: Long = 0

    //Added the actual Question UID (28012019):
    var selQuestionResponseSelQuestionUid: Long = 0

    @MasterChangeSeqNum
    var selQuestionResponseMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var selQuestionResponseLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var selQuestionResponseLastChangedBy: Int = 0
}
