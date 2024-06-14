package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.CourseGroupMember
import com.ustadmobile.lib.db.entities.Person
import kotlinx.serialization.Serializable

@Serializable
data class CourseGroupMemberAndPerson(
    @Embedded
    var courseGroupMember: CourseGroupMember? = null,
    @Embedded
    var person: Person? = null,
)