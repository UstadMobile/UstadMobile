package com.ustadmobile.core.viewmodel.clazz

import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.CoursePermission

object CoursePermissionConstants {

    val COURSE_PERMISSIONS_LABELS = listOf(
        MR.strings.view_course to CoursePermission.PERMISSION_VIEW,
        MR.strings.edit_course to CoursePermission.PERMISSION_EDIT,
    )

}