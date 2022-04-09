package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class CourseBlockWithEntity: CourseBlock() {

    @Embedded
    var assignment: ClazzAssignment? = null

    @Embedded
    var entry: ContentEntry? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as CourseBlockWithEntity

        if (assignment != other.assignment) return false
        if (entry != other.entry) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (assignment?.hashCode() ?: 0)
        result = 31 * result + (entry?.hashCode() ?: 0)
        return result
    }


}