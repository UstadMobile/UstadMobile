package com.ustadmobile.core.db

object PermissionFlags {

    /**
     * COURSE_ permissions will apply to the given course when used as part of a CoursePermission
     * entity, or to all courses if used as part of a SystemPermission entity.
     */
    const val COURSE_VIEW = 1L //2^0

    const val COURSE_EDIT = 2L //2^1

    const val COURSE_MODERATE = 4L //2^2

    const val COURSE_MANAGE_STUDENT_ENROLMENT = 8L //2^3

    const val COURSE_MANAGE_TEACHER_ENROLMENT = 16L //2^4

    const val COURSE_ATTENDANCE_VIEW = 32L //2^5

    const val COURSE_ATTENDANCE_RECORD = 64L //2^6

    const val COURSE_LEARNINGRECORD_VIEW = 128L //2^7

    const val COURSE_LEARNINGRECORD_EDIT = 256L //2^8

    const val ADD_COURSE: Long = 512L //2^9

    const val ADD_PERSON = 1024L // 2^10

    const val DIRECT_ENROL = 2048L // 2^11

    const val MANAGE_USER_PERMISSIONS = 4096L // 2^12

    const val ALL = Long.MAX_VALUE

}