package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

/**
 * Represents the student who is nominated for a specific question in the question set
 */
@UmEntity(tableId = 24)
@Entity
class SelQuestionResponseNomination {

    @PrimaryKey(autoGenerate = true)
    var selQuestionResponseNominationUid: Long = 0

    var selQuestionResponseNominationClazzMemberUid: Long = 0

    //<-> SelQuestionResponse entity.
    var selQuestionResponseNominationSelQuestionResponseUId: Long = 0

    //Added 31012019 sel
    var isNominationActive: Boolean = false

    //Renamed:
    @UmSyncMasterChangeSeqNum
    var selQuestionResponseNominationMasterChangeSeqNum: Long = 0
    //private long scheduleNominationQuestionResponseNominationMasterChangeSeqNum;

    //Renamed:
    @UmSyncLocalChangeSeqNum
    var selQuestionResponseNominationLocalChangeSeqNum: Long = 0
    //private long scheduleNominationQuestionResponseNominationLocalChangeSeqNum;

    @UmSyncLastChangedBy
    var selQuestionResponseNominationLastChangedBy: Int = 0
}
