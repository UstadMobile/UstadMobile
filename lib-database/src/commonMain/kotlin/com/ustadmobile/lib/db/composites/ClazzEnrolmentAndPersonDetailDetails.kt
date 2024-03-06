package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.CourseTerminology
import kotlinx.serialization.Serializable

@Serializable
data class ClazzEnrolmentAndPersonDetailDetails(
    @Embedded
    var enrolment: ClazzEnrolment? = null,
    @Embedded
    var clazz: Clazz? = null,
    @Embedded
    var courseTerminology: CourseTerminology? = null,

)
