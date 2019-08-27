package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity


/**
 * Represents a set of social nomination question eg: "Question set for Region A"
 */
@SyncableEntity(tableId = 25)
@Entity
open class SelQuestionSet {

    @PrimaryKey(autoGenerate = true)
    var selQuestionSetUid: Long = 0

    // The set title.
    var title: String? = null

    @MasterChangeSeqNum
    var selQuestionSetMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var selQuestionSetLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var selQuestionSetLastChangedBy: Int = 0
}
