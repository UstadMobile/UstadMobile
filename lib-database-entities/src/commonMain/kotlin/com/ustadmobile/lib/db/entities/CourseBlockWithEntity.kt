package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class CourseBlockWithEntity: CourseBlockWithEntityDb() {

    var topics: List<DiscussionTopic>? = null

    var topicUidsToRemove: List<Long>? = null


    fun createFromDb(dbEntity: CourseBlockWithEntityDb){
        assignment = dbEntity.assignment
        entry = dbEntity.entry
        courseDiscussion = dbEntity.courseDiscussion
        cbUid = dbEntity.cbUid
        cbType = dbEntity.cbType
        cbIndentLevel = dbEntity.cbIndentLevel
        cbModuleParentBlockUid = dbEntity.cbModuleParentBlockUid
        cbTitle = dbEntity.cbTitle
        cbDescription = dbEntity.cbDescription
        cbCompletionCriteria = dbEntity.cbCompletionCriteria
        cbHideUntilDate = dbEntity.cbHideUntilDate
        cbDeadlineDate = dbEntity.cbDeadlineDate
        cbLateSubmissionPenalty = dbEntity.cbLateSubmissionPenalty
        cbGracePeriodDate = dbEntity.cbGracePeriodDate
        cbMaxPoints = dbEntity.cbMaxPoints
        cbIndex = dbEntity.cbIndex
        cbClazzUid = dbEntity.cbClazzUid
        cbActive = dbEntity.cbActive
        cbHidden = dbEntity.cbHidden
        cbEntityUid = dbEntity.cbEntityUid
        cbLct = dbEntity.cbLct
    }

}