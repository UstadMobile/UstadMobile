package com.ustadmobile.core.viewmodel.clazz

import com.ustadmobile.core.MR
import com.ustadmobile.core.db.PermissionFlags

object CoursePermissionConstants {

    val COURSE_PERMISSIONS_LABELS = listOf(
        MR.strings.view_course to PermissionFlags.COURSE_VIEW,
        MR.strings.edit_course to PermissionFlags.COURSE_EDIT,
        MR.strings.moderate to PermissionFlags.COURSE_MODERATE,
        MR.strings.manage_student_enrolments to PermissionFlags.COURSE_MANAGE_STUDENT_ENROLMENT,
        MR.strings.manage_teacher_enrolments to PermissionFlags.COURSE_MANAGE_TEACHER_ENROLMENT,
        MR.strings.permission_attendance_select to PermissionFlags.COURSE_ATTENDANCE_VIEW,
        MR.strings.permission_attendance_update to PermissionFlags.COURSE_ATTENDANCE_RECORD,
        MR.strings.view_learning_records to PermissionFlags.COURSE_LEARNINGRECORD_VIEW,
        MR.strings.edit_learning_records to PermissionFlags.COURSE_LEARNINGRECORD_EDIT,
    )

}