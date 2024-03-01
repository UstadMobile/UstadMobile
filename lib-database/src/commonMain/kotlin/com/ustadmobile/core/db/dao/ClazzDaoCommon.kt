package com.ustadmobile.core.db.dao


object ClazzDaoCommon {
    const val SORT_CLAZZNAME_ASC = 1

    const val SORT_CLAZZNAME_DESC = 2

    const val SORT_ATTENDANCE_ASC = 3

    const val SORT_ATTENDANCE_DESC = 4

    const val FILTER_CURRENTLY_ENROLLED = 5

    const val FILTER_PAST_ENROLLMENTS = 6

    const val SELECT_ACTIVE_CLAZZES = "SELECT * FROM Clazz WHERE CAST(isClazzActive AS INTEGER) = 1"

    /**
     * Determine if the person as per :accountPersonUid parameter has a given permission with a
     * particular course:
     */
    const val PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT1 =
        """(
             /* If the accountPersonUid is the owner of the course, all permissions are granted */
             (COALESCE(
                          (SELECT _Clazz_Permission.clazzOwnerPersonUid 
                             FROM Clazz _Clazz_Permission
                            WHERE _Clazz_Permission.clazzUid = :clazzUid), 0) = :accountPersonUid)
              /* 
              If there is a CoursePermission entity that is for the course as per the clazzUid
              parameter that is granted to the person directly or to the enrolmentRole that the 
              person has in the course, then permission is granted.
              */              
              OR EXISTS(SELECT CoursePermission.cpUid
                          FROM CoursePermission
                               ${CoursePermissionDaoCommon.LEFT_JOIN_ENROLMENT_FROM_COURSEPERMISSION_WITH_ACCOUNT_UID_PARAM}
                         WHERE CoursePermission.cpClazzUid = :clazzUid
                           AND (CoursePermission.cpToPersonUid = :accountPersonUid 
                                OR CoursePermission.cpToEnrolmentRole = ClazzEnrolment_ForAccountPerson.clazzEnrolmentRole)
                           AND (CoursePermission.cpPermissionsFlag & 
        """


    const val SELECT_CLAZZ_UID_FOR_ENROLMENT_UID_SQL = """
        SELECT COALESCE(
               (SELECT _ClazzEnrolment_ForClazzUid.clazzEnrolmentClazzUid
                  FROM ClazzEnrolment _ClazzEnrolment_ForClazzUid
                 WHERE _ClazzEnrolment_ForClazzUid.clazzEnrolmentUid = :clazzEnrolmentUid), 0)
    """

    /**
     * As per the section above, but replaced the :clazzUid query parameter with getting the clazzUid
     * as per the :clazzEnrolmentUid parameter using SELECT_CLAZZ_UID_FOR_ENROLMENT_UID_SQL
     */
    const val PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZENROLMENTUID_SQL_PT1 = """(
             /* If the accountPersonUid is the owner of the course, all permissions are granted */
             (COALESCE(
                          (SELECT _Clazz_Permission.clazzOwnerPersonUid 
                             FROM Clazz _Clazz_Permission
                            WHERE _Clazz_Permission.clazzUid = ($SELECT_CLAZZ_UID_FOR_ENROLMENT_UID_SQL)), 0) = :accountPersonUid)
              /* 
              If there is a CoursePermission entity that is for the course as per the clazzUid
              parameter that is granted to the person directly or to the enrolmentRole that the 
              person has in the course, then permission is granted.
              */              
              OR EXISTS(SELECT CoursePermission.cpUid
                          FROM CoursePermission
                               ${CoursePermissionDaoCommon.LEFT_JOIN_ENROLMENT_FROM_COURSEPERMISSION_WITH_ACCOUNT_UID_PARAM}
                         WHERE CoursePermission.cpClazzUid = ($SELECT_CLAZZ_UID_FOR_ENROLMENT_UID_SQL)
                           AND (CoursePermission.cpToPersonUid = :accountPersonUid 
                                OR CoursePermission.cpToEnrolmentRole = ClazzEnrolment_ForAccountPerson.clazzEnrolmentRole)
                           AND (CoursePermission.cpPermissionsFlag & 
        """


    /**
     * If there is a SystemPermission for the active user as per accountPersonUid, permission is granted
     */
    const val PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2 = """
        ) > 0)
              OR EXISTS(SELECT SystemPermission.spUid
                          FROM SystemPermission
                         WHERE SystemPermission.spToPersonUid = :accountPersonUid
                           AND (SystemPermission.spPermissionsFlag & 
    """
    const val PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3 = """
        ) > 0)
               )
    """



    const val PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL = """
         $PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT1 :permission
         $PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2 :permission
         $PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3
    """




    const val PERSON_HAS_PERMISSION_WITH_CLAZZ_SQL = """
        SELECT (:clazzUid != 0 AND :accountPersonUid != 0)
          AND  ($PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL)            
    """



}