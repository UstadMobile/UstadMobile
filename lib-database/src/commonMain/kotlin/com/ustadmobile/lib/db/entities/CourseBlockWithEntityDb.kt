package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
open class CourseBlockWithEntityDb: CourseBlock() {

    @Embedded
    var assignment: ClazzAssignment? = null

    /**
     * If the assignment is set to be marked in groups - this is the label for name of the group
     */
    var assignmentCourseGroupSetName: String? = null

    @Embedded
    var entry: ContentEntry? = null

    @Embedded
    var language: Language? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CourseBlockWithEntityDb) return false
        if (!super.equals(other)) return false

        if (assignment != other.assignment) return false
        if (assignmentCourseGroupSetName != other.assignmentCourseGroupSetName) return false
        if (entry != other.entry) return false
        if (language != other.language) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (assignment?.hashCode() ?: 0)
        result = 31 * result + (assignmentCourseGroupSetName?.hashCode() ?: 0)
        result = 31 * result + (entry?.hashCode() ?: 0)
        result = 31 * result + (language?.hashCode() ?: 0)
        return result
    }


}