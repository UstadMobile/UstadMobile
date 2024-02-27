package com.ustadmobile.core.db.dao


object ClazzDaoCommon {
    const val SORT_CLAZZNAME_ASC = 1

    const val SORT_CLAZZNAME_DESC = 2

    const val SORT_ATTENDANCE_ASC = 3

    const val SORT_ATTENDANCE_DESC = 4

    const val FILTER_CURRENTLY_ENROLLED = 5

    const val FILTER_PAST_ENROLLMENTS = 6

    const val SELECT_ACTIVE_CLAZZES = "SELECT * FROM Clazz WHERE CAST(isClazzActive AS INTEGER) = 1"

    const val PERSON_HAS_PERMISSION_WITH_CLAZZ_SQL = """
        SELECT (:clazzUid != 0 AND :accountPersonUid != 0)
          AND  (
                 (COALESCE(
                          (SELECT Clazz.clazzOwnerPersonUid 
                             FROM Clazz 
                            WHERE Clazz.clazzUid = :clazzUid), 0) = :accountPersonUid)
              OR EXISTS(SELECT CoursePermission.cpUid
                          FROM CoursePermission
                               ${CoursePermissionDaoCommon.LEFT_JOIN_ENROLMENT_FROM_COURSEPERMISSION_WITH_ACCOUNT_UID_PARAM}
                         WHERE CoursePermission.cpClazzUid = :clazzUid
                           AND (CoursePermission.cpToPersonUid = :accountPersonUid 
                                OR CoursePermission.cpToEnrolmentRole = ClazzEnrolment.clazzEnrolmentRole)
                           AND (CoursePermission.cpPermissionsFlag & :permission) > 0)
              OR EXISTS(SELECT SystemPermission.spUid
                          FROM SystemPermission
                         WHERE SystemPermission.spToPersonUid = :accountPersonUid
                           AND (SystemPermission.spPermissionsFlag & :permission) > 0)
               )            
    """

}