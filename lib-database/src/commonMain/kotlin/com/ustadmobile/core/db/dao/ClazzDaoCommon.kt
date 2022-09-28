package com.ustadmobile.core.db.dao

object ClazzDaoCommon {
    const val SORT_CLAZZNAME_ASC = 1

    const val SORT_CLAZZNAME_DESC = 2

    const val SORT_ATTENDANCE_ASC = 3

    const val SORT_ATTENDANCE_DESC = 4

    const val FILTER_ACTIVE_ONLY = 1

    const val FILTER_CURRENTLY_ENROLLED = 5

    const val FILTER_PAST_ENROLLMENTS = 6

    const val SELECT_ACTIVE_CLAZZES = "SELECT * FROM Clazz WHERE CAST(isClazzActive AS INTEGER) = 1"
}