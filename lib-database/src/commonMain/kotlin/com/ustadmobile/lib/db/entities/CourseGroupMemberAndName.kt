package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
data class CourseGroupMemberAndName(
    @Embedded
    var cgm: CourseGroupMember? = null,
    var name: String? = null,
    var personUid: Long = 0L,
    var enrolmentIsActive: Boolean = false,
    var pictureUri: String? = null,
)
