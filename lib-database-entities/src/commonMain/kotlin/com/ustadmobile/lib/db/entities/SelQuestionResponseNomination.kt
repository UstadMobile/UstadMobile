package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity


/**
 * Represents the student who is nominated for a specific question in the question set
 */
@SyncableEntity(tableId = 24)
@Entity
class SelQuestionResponseNomination {

    @PrimaryKey(autoGenerate = true)
    var selQuestionResponseNominationUid: Long = 0

    var selQuestionResponseNominationClazzMemberUid: Long = 0

    //<-> SelQuestionResponse entity.
    var selQuestionResponseNominationSelQuestionResponseUId: Long = 0

    //Added 31012019 sel
    var nominationActive: Boolean = false

    //Renamed:
    @MasterChangeSeqNum
    var selQuestionResponseNominationMasterChangeSeqNum: Long = 0
    //private long scheduleNominationQuestionResponseNominationMasterChangeSeqNum;

    //Renamed:
    @LocalChangeSeqNum
    var selQuestionResponseNominationLocalChangeSeqNum: Long = 0
    //private long scheduleNominationQuestionResponseNominationLocalChangeSeqNum;

    @LastChangedBy
    var selQuestionResponseNominationLastChangedBy: Int = 0
}
