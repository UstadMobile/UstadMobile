package com.ustadmobile.core.db.dao

object CoursePermissionDaoCommon {

    //Add order by just to be sure
    const val LEFT_JOIN_ENROLMENT_FROM_COURSEPERMISSION_WITH_ACCOUNT_UID_PARAM = """
        LEFT JOIN ClazzEnrolment
                        ON CoursePermission.cpToEnrolmentRole != 0
                       AND ClazzEnrolment.clazzEnrolmentUid = 
                           (SELECT COALESCE(
                                   (SELECT ClazzEnrolment.clazzEnrolmentUid 
                                      FROM ClazzEnrolment
                                     WHERE ClazzEnrolment.clazzEnrolmentClazzUid = CoursePermission.cpClazzUid
                                       AND ClazzEnrolment.clazzEnrolmentPersonUid = :accountPersonUid
                                       AND ClazzEnrolment.clazzEnrolmentRole = CoursePermission.cpToEnrolmentRole
                                       AND ClazzEnrolment.clazzEnrolmentActive
                                     LIMIT 1), 0))
    """

}