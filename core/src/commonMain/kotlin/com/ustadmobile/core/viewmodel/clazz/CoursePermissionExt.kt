package com.ustadmobile.core.viewmodel.clazz

import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.CoursePermission
import dev.icerock.moko.resources.StringResource
import com.ustadmobile.core.MR

fun CoursePermission.titleStringResource(): StringResource? {
    return when(cpToEnrolmentRole) {
        ClazzEnrolment.ROLE_STUDENT -> MR.strings.students
        ClazzEnrolment.ROLE_TEACHER -> MR.strings.teachers_literal
        else -> null
    }
}
