package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

/**
 * Represents a set of social nomination question eg: "Question set for Region A"
 */
@UmEntity(tableId = 25)
@Entity
open class SelQuestionSet {

    @PrimaryKey(autoGenerate = true)
    var selQuestionSetUid: Long = 0

    // The set title.
    var title: String? = null

    @UmSyncMasterChangeSeqNum
    var selQuestionSetMasterChangeSeqNum: Long = 0

    @UmSyncLocalChangeSeqNum
    var selQuestionSetLocalChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
    var selQuestionSetLastChangedBy: Int = 0
}
