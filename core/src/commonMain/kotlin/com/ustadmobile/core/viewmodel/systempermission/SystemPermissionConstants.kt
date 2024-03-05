package com.ustadmobile.core.viewmodel.systempermission

import com.ustadmobile.core.MR
import com.ustadmobile.core.db.PermissionFlags

object SystemPermissionConstants {

    val SYSTEM_PERMISSION_LABELS = listOf(
        MR.strings.add_new_users to PermissionFlags.ADD_PERSON,
        MR.strings.view_all_users to PermissionFlags.PERSON_VIEW,
        MR.strings.edit_all_users to PermissionFlags.EDIT_ALL_PERSONS,
        MR.strings.manage_user_permissions to PermissionFlags.MANAGE_USER_PERMISSIONS,
        MR.strings.add_new_courses to PermissionFlags.ADD_COURSE,
        MR.strings.view_all_courses to PermissionFlags.COURSE_VIEW,
        MR.strings.edit_all_courses to PermissionFlags.COURSE_EDIT,
        MR.strings.moderate_all_courses to PermissionFlags.COURSE_MODERATE,
        MR.strings.direct_enrol_users_onto_courses to PermissionFlags.DIRECT_ENROL,
        MR.strings.manage_student_enrolments_for_all_courses to PermissionFlags.COURSE_MANAGE_STUDENT_ENROLMENT,
        MR.strings.manage_teacher_enrolments_for_all_courses to PermissionFlags.COURSE_MANAGE_TEACHER_ENROLMENT,
        MR.strings.view_attendance_records_for_all_courses to PermissionFlags.COURSE_ATTENDANCE_VIEW,
        MR.strings.edit_attendance_records_for_all_courses to PermissionFlags.COURSE_ATTENDANCE_RECORD,
        MR.strings.view_learning_records_for_all_courses to PermissionFlags.COURSE_LEARNINGRECORD_VIEW,
        MR.strings.edit_learning_records_for_all_courses to PermissionFlags.COURSE_LEARNINGRECORD_EDIT,
    )

}