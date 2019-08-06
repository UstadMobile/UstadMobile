package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

@UmEntity(tableId = 52)
@Entity
class SelQuestionOption {

    @PrimaryKey(autoGenerate = true)
    var selQuestionOptionUid: Long = 0

    var optionText: String? = null

    var selQuestionOptionQuestionUid: Long = 0

    @UmSyncMasterChangeSeqNum
    var selQuestionOptionMasterChangeSeqNum: Long = 0

    @UmSyncLocalChangeSeqNum
    var selQuestionOptionLocalChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
    var selQuestionOptionLastChangedBy: Int = 0

    var isOptionActive: Boolean = false
}
