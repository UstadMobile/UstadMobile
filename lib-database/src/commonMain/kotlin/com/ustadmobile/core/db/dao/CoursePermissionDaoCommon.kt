package com.ustadmobile.core.db.dao

object CoursePermissionDaoCommon {

    const val LEFT_JOIN_ENROLMENT_FROM_COURSEPERMISSION_WITH_ACCOUNT_UID_PARAM = """
        LEFT JOIN ClazzEnrolment ClazzEnrolment_ForAccountPerson 
                        ON CoursePermission.cpToEnrolmentRole != 0
                       AND ClazzEnrolment_ForAccountPerson.clazzEnrolmentUid = 
                           (SELECT COALESCE(
                                   (SELECT _ClazzEnrolment_AccountPersonInner.clazzEnrolmentUid 
                                      FROM ClazzEnrolment _ClazzEnrolment_AccountPersonInner
                                     WHERE _ClazzEnrolment_AccountPersonInner.clazzEnrolmentClazzUid = CoursePermission.cpClazzUid
                                       AND _ClazzEnrolment_AccountPersonInner.clazzEnrolmentPersonUid = :accountPersonUid
                                       AND _ClazzEnrolment_AccountPersonInner.clazzEnrolmentActive
                                  ORDER BY _ClazzEnrolment_AccountPersonInner.clazzEnrolmentDateLeft DESC   
                                     LIMIT 1), 0))
    """

}