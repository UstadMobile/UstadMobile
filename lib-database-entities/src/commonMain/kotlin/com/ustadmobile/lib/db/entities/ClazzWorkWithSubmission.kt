package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

/**
 * POJO representing ClazzWork and ClazzWorkSubmission
 */
@Serializable
class ClazzWorkWithSubmission : ClazzWork() {
    @Embedded
    var clazzWorkSubmission: ClazzWorkSubmission? = null

    fun generateWithClazzWorkAndClazzWorkSubmission(cw: ClazzWork, cws: ClazzWorkSubmission?): ClazzWorkWithSubmission{
        clazzWorkSubmission = cws
        clazzWorkUid = cw.clazzWorkUid
        clazzWorkCreatorPersonUid = cw.clazzWorkCreatorPersonUid
        clazzWorkClazzUid = cw.clazzWorkClazzUid
        clazzWorkTitle = cw.clazzWorkTitle
        clazzWorkCreatedDate = cw.clazzWorkCreatedDate
        clazzWorkStartDateTime = cw.clazzWorkStartDateTime
        clazzWorkStartTime = cw.clazzWorkStartTime
        clazzWorkDueTime = cw.clazzWorkDueTime
        clazzWorkDueDateTime = cw.clazzWorkDueDateTime
        clazzWorkSubmissionType = cw.clazzWorkSubmissionType
        clazzWorkCommentsEnabled = cw.clazzWorkCommentsEnabled
        clazzWorkMaximumScore = cw.clazzWorkMaximumScore
        clazzWorkInstructions = cw.clazzWorkInstructions
        clazzWorkActive = cw.clazzWorkActive
        clazzWorkLocalChangeSeqNum = cw.clazzWorkLocalChangeSeqNum
        clazzWorkLocalChangeSeqNum = cw.clazzWorkLocalChangeSeqNum
        clazzWorkLastChangedBy = cw.clazzWorkLastChangedBy

        return this
    }


}
