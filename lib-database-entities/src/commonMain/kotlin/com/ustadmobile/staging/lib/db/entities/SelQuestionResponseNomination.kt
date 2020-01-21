package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable


/**
 * Represents the student who is nominated for a specific question in the question set
 */
@SyncableEntity(tableId = 24)
@Entity
@Serializable
class SelQuestionResponseNomination {

    @PrimaryKey(autoGenerate = true)
    var selqrnUid: Long = 0

    var selqrnClazzMemberUid: Long = 0

    //<-> SelQuestionResponse entity.
    var selqrnSelQuestionResponseUId: Long = 0

    //Added 31012019 sel
    var nominationActive: Boolean = false

    //Renamed:
    @MasterChangeSeqNum
    var selqrnMCSN: Long = 0
    //private long scheduleNominationQuestionResponseNominationMasterChangeSeqNum;

    //Renamed:
    @LocalChangeSeqNum
    var selqrnMCSNLCSN: Long = 0
    //private long scheduleNominationQuestionResponseNominationLocalChangeSeqNum;

    @LastChangedBy
    var selqrnMCSNLCB: Int = 0
}
