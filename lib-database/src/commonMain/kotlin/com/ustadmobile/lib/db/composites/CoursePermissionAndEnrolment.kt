package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.CoursePermission
import kotlinx.serialization.Serializable

@Serializable
data class CoursePermissionAndEnrolment(
    @Embedded
    var coursePermission: CoursePermission? = null,
    @Embedded
    var clazzEnrolment: ClazzEnrolment? = null,
)

