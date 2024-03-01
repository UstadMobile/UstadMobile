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
                               $LEFT_JOIN_ENROLMENT_FROM_COURSEPERMISSION_WITH_ACCOUNT_UID_PARAM
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
                               $LEFT_JOIN_ENROLMENT_FROM_COURSEPERMISSION_WITH_ACCOUNT_UID_PARAM
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