package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class CourseBlockWithEntity: CourseBlockWithEntityDb() {

    var topics: List<DiscussionTopic>? = null

    var topicUidsToRemove: List<Long>? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as CourseBlockWithEntity

        if (topics != other.topics) return false
        if (topicUidsToRemove != other.topicUidsToRemove) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (topics?.hashCode() ?: 0)
        result = 31 * result + (topicUidsToRemove?.hashCode() ?: 0)
        return result
    }


    fun createFromDb(dbEntity: CourseBlockWithEntityDb){
        assignment = dbEntity.assignment
        entry = dbEntity.entry
        courseDiscussion = dbEntity.courseDiscussion
        language = dbEntity.language
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