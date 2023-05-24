package com.ustadmobile.core.db.dao

import com.ustadmobile.lib.db.entities.ClazzEnrolment

object ClazzEnrolmentDaoCommon {

    const val SORT_FIRST_NAME_ASC = 1

    const val SORT_FIRST_NAME_DESC = 2

    const val SORT_LAST_NAME_ASC = 3

    const val SORT_LAST_NAME_DESC = 4

    const val SORT_ATTENDANCE_ASC = 5

    const val SORT_ATTENDANCE_DESC = 6

    const val SORT_DATE_REGISTERED_ASC = 7

    const val SORT_DATE_REGISTERED_DESC = 8

    const val SORT_DATE_LEFT_ASC = 9

    const val SORT_DATE_LEFT_DESC = 10

    const val FILTER_ACTIVE_ONLY = 1

    /**
     * Common query to find the students enrolled in a course at a given time
     * Requires :clazzUid and :time as parameters
     *
     */
    const val WITH_CURRENTLY_ENROLED_STUDENTS_SQL = """
        WITH CurrentlyEnrolledPersonUids(enroledPersonUid) AS
              (SELECT DISTINCT ClazzEnrolment.clazzEnrolmentPersonUid AS enroledPersonUid
                 FROM ClazzEnrolment
                WHERE ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid
                  AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
                  AND :time BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined AND ClazzEnrolment.clazzEnrolmentDateLeft)
    """
}