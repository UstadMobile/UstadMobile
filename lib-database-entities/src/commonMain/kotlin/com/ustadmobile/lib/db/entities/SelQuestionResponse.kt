package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

/**
 * Represents the students response to a specific question in the question set
 */
@UmEntity(tableId = 23)
@Entity
class SelQuestionResponse {

    @PrimaryKey(autoGenerate = true)
    var selQuestionResponseUid: Long = 0

    // -> SelQuestionSetResponse
    var selQuestionResponseSelQuestionSetResponseUid: Long = 0

    //Added the actual Question UID (28012019):
    var selQuestionResponseSelQuestionUid: Long = 0

    @UmSyncMasterChangeSeqNum
    var selQuestionResponseMasterChangeSeqNum: Long = 0

    @UmSyncLocalChangeSeqNum
    var selQuestionResponseLocalChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
    var selQuestionResponseLastChangedBy: Int = 0
}
