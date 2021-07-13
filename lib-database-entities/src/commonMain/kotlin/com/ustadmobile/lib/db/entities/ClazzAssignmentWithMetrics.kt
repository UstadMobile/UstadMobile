package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ClazzAssignmentWithMetrics : ClazzAssignment() {

    @Embedded
    var studentProgress: StudentAssignmentProgress? = null

    @Embedded
    var studentScore: ContentEntryStatementScoreProgress? = null

}