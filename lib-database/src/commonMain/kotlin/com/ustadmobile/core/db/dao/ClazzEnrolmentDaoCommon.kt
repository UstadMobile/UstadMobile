package com.ustadmobile.core.db.dao

import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.db.dao.CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL
import com.ustadmobile.core.db.dao.CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT1
import com.ustadmobile.core.db.dao.CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2
import com.ustadmobile.core.db.dao.CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3
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

    /**
     * Commonly used snippet to check if the current user as per the accountPersonUid param is a
     * student in the given class as per clazzUid
     */
    const val SELECT_ACCOUNT_PERSON_UID_IS_STUDENT_IN_CLAZZ_UID = """
        (SELECT EXISTS(
                SELECT 1
                  FROM ClazzEnrolment
                 WHERE ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid
                   AND ClazzEnrolment.clazzEnrolmentPersonUid = :accountPersonUid
                   AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}))
    """


    /*
    * Note: SELECT * FROM (Subquery) AS CourseMember is needed so that sorting by
    * earliestJoinDate/latestDateLeft will work as expected on postgres.
    *
    * This query uses a permission check so that users will only see participants that they have
    * permission to see (e.g. on some courses / MOOC style students might not have permission to
    * see other students etc).
    *
    * This Query is used by ClazzMemberListViewModel.
    *
    *
    AND (
                          ($PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL)
                       OR Person.personUid = :accountPersonUid
                      )
    *
    *
    */
    const val SELECT_BY_UID_AND_ROLE_SQL = """
        SELECT * 
          FROM (SELECT Person.*, PersonPicture.*,
                       (SELECT MIN(ClazzEnrolment.clazzEnrolmentDateJoined) 
                          FROM ClazzEnrolment 
                         WHERE Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid) AS earliestJoinDate, 
        
                       (SELECT MAX(ClazzEnrolment.clazzEnrolmentDateLeft) 
                          FROM ClazzEnrolment 
                         WHERE Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid) AS latestDateLeft, 
        
                       (SELECT ClazzEnrolment.clazzEnrolmentRole 
                          FROM ClazzEnrolment 
                         WHERE Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid 
                           AND ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid 
                           AND ClazzEnrolment.clazzEnrolmentActive
                      ORDER BY ClazzEnrolment.clazzEnrolmentDateLeft DESC
                         LIMIT 1) AS enrolmentRole
                  FROM Person
                       LEFT JOIN PersonPicture
                                 ON PersonPicture.personPictureUid = Person.personUid
                 WHERE Person.personUid IN 
                       (SELECT DISTINCT ClazzEnrolment.clazzEnrolmentPersonUid 
                          FROM ClazzEnrolment 
                         WHERE ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid 
                           AND ClazzEnrolment.clazzEnrolmentActive 
                           AND ClazzEnrolment.clazzEnrolmentRole = :roleId 
                           AND (:filter != $FILTER_ACTIVE_ONLY 
                                 OR (:currentTime 
                                      BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined 
                                      AND ClazzEnrolment.clazzEnrolmentDateLeft))) 
                   /* Begin permission check */
                   AND (
                           ($PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL)
                        OR Person.personUid = :accountPersonUid
                       )  
                   /* End permission check */                   
                   AND Person.firstNames || ' ' || Person.lastName LIKE :searchText
               GROUP BY Person.personUid, PersonPicture.personPictureUid) AS CourseMember
      ORDER BY CASE(:sortOrder)
                WHEN $SORT_FIRST_NAME_ASC THEN CourseMember.firstNames
                WHEN $SORT_LAST_NAME_ASC THEN CourseMember.lastName
                ELSE ''
            END ASC,
            CASE(:sortOrder)
                WHEN $SORT_FIRST_NAME_DESC THEN CourseMember.firstNames
                WHEN $SORT_LAST_NAME_DESC THEN CourseMember.lastName
                ELSE ''
            END DESC,
            CASE(:sortOrder)
                WHEN $SORT_DATE_REGISTERED_ASC THEN CourseMember.earliestJoinDate
                WHEN $SORT_DATE_LEFT_ASC THEN CourseMember.latestDateLeft
                ELSE 0
            END ASC,
            CASE(:sortOrder)
                WHEN $SORT_DATE_REGISTERED_DESC THEN CourseMember.earliestJoinDate
                WHEN $SORT_DATE_LEFT_DESC THEN CourseMember.latestDateLeft
                ELSE 0
            END DESC
    """

    /**
     * Get the PersonUids for a paged gradebook query
     *
     * Requires studentsLimit and studentsOffset parameters in addition to those found on
     * ClazzEnrolmentDao#findByClazzUidAndRoleForGradebook
     *
     */
    const val PERSON_UIDS_FOR_PAGED_GRADEBOOK_QUERY_CTE = """
        PersonUids(personUid) AS (
            SELECT CourseMember.personUid 
              FROM (SELECT Person.*,
                           (SELECT MIN(ClazzEnrolment.clazzEnrolmentDateJoined) 
                              FROM ClazzEnrolment 
                             WHERE Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid) AS earliestJoinDate, 
            
                           (SELECT MAX(ClazzEnrolment.clazzEnrolmentDateLeft) 
                              FROM ClazzEnrolment 
                             WHERE Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid) AS latestDateLeft, 
            
                           (SELECT ClazzEnrolment.clazzEnrolmentRole 
                              FROM ClazzEnrolment 
                             WHERE Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid 
                               AND ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid 
                               AND ClazzEnrolment.clazzEnrolmentActive
                          ORDER BY ClazzEnrolment.clazzEnrolmentDateLeft DESC
                             LIMIT 1) AS enrolmentRole
                      FROM Person
                     WHERE Person.personUid IN 
                           (SELECT DISTINCT ClazzEnrolment.clazzEnrolmentPersonUid 
                              FROM ClazzEnrolment 
                             WHERE ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid 
                               AND ClazzEnrolment.clazzEnrolmentActive 
                               AND ClazzEnrolment.clazzEnrolmentRole = :roleId 
                               AND (:filter != $FILTER_ACTIVE_ONLY 
                                     OR (:currentTime 
                                          BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined 
                                          AND ClazzEnrolment.clazzEnrolmentDateLeft))) 
                       /* Begin permission check */
                       AND (
                               ($PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT1 ${PermissionFlags.COURSE_LEARNINGRECORD_VIEW}
                                $PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2 ${PermissionFlags.COURSE_LEARNINGRECORD_VIEW}
                                $PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3)
                            OR Person.personUid = :accountPersonUid
                           )  
                       /* End permission check */                   
                       AND Person.firstNames || ' ' || Person.lastName LIKE :searchText
                   GROUP BY Person.personUid) AS CourseMember
          ORDER BY CASE(:sortOrder)
                    WHEN $SORT_FIRST_NAME_ASC THEN CourseMember.firstNames
                    WHEN $SORT_LAST_NAME_ASC THEN CourseMember.lastName
                    ELSE ''
                END ASC,
                CASE(:sortOrder)
                    WHEN $SORT_FIRST_NAME_DESC THEN CourseMember.firstNames
                    WHEN $SORT_LAST_NAME_DESC THEN CourseMember.lastName
                    ELSE ''
                END DESC,
                CASE(:sortOrder)
                    WHEN $SORT_DATE_REGISTERED_ASC THEN CourseMember.earliestJoinDate
                    WHEN $SORT_DATE_LEFT_ASC THEN CourseMember.latestDateLeft
                    ELSE 0
                END ASC,
                CASE(:sortOrder)
                    WHEN $SORT_DATE_REGISTERED_DESC THEN CourseMember.earliestJoinDate
                    WHEN $SORT_DATE_LEFT_DESC THEN CourseMember.latestDateLeft
                    ELSE 0
                END DESC
             LIMIT :studentsLimit
            OFFSET :studentsOffset   
         )
    """


}