package com.ustadmobile.core.viewmodel.clazz

import com.ustadmobile.core.MR
import com.ustadmobile.core.db.PermissionFlags

object CoursePermissionConstants {

    val COURSE_PERMISSIONS_LABELS = listOf(
        MR.strings.view_course to PermissionFlags.COURSE_VIEW,
        MR.strings.edit_course to PermissionFlags.COURSE_EDIT,
    )

}