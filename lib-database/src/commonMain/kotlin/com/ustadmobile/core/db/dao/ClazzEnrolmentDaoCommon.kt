package com.ustadmobile.core.db.dao

import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.lib.db.entities.ClazzEnrolment

object ClazzEnrolmentDaoCommon {

    const val SORT_FIRST_NAME_ASC = 1

    const val SORT_FIRST_NAME_DESC = 2

    const val SORT_LAST_NAME_ASC = 3

    const val SORT_LAST_NAME_DESC = 4

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

    const val SELECT_ENROLMENT_TYPE_BY_UID_SQL = """
        SELECT ClazzEnrolment.clazzEnrolmentRole
          FROM ClazzEnrolment
         WHERE ClazzEnrolment.clazzEnrolmentUid = :clazzEnrolmentUid         
    """

    const val PERMISSION_REQUIRED_BY_CLAZZENROLMENT_UID = """
        CASE ($SELECT_ENROLMENT_TYPE_BY_UID_SQL)
             WHEN ${ClazzEnrolment.ROLE_STUDENT} THEN ${PermissionFlags.COURSE_MANAGE_STUDENT_ENROLMENT}
             ELSE ${PermissionFlags.COURSE_MANAGE_STUDENT_ENROLMENT}
        END     
    """

}