package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

/**
 * Represents the question within a question set.
 * eg: "Select the students who sit alone"
 *
 */
@UmEntity(tableId = 22)
@Entity
class SelQuestion {

    @PrimaryKey(autoGenerate = true)
    var selQuestionUid: Long = 0

    var questionText: String? = null

    // -> SelQuestionSet - what set is this question a part of
    var selQuestionSelQuestionSetUid: Long = 0

    //The order.
    var questionIndex: Int = 0

    //If this question is to be assigned to all classes. (if not - not handled / implemented yet).
    var assignToAllClasses: Boolean = false

    //If this question allows for multiple nominations.
    var multiNominations: Boolean = false

    var questionType: Int = 0

    var questionActive: Boolean = false

    @UmSyncMasterChangeSeqNum
    var selQuestionMasterChangeSeqNum: Long = 0

    @UmSyncLocalChangeSeqNum
    var selQuestionLocalChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
    var selQuestionLastChangedBy: Int = 0
}
