package com.ustadmobile.core.db.dao

import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.db.dao.SystemPermissionDaoCommon.SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT1
import com.ustadmobile.core.db.dao.SystemPermissionDaoCommon.SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT2
import com.ustadmobile.lib.db.entities.Person

object PersonDaoCommon {

    const val SORT_FIRST_NAME_ASC = 1

    const val SORT_FIRST_NAME_DESC = 2

    const val SORT_LAST_NAME_ASC = 3

    const val SORT_LAST_NAME_DESC = 4

    const val SQL_SELECT_LIST_WITH_PERMISSION = """
         WITH CanViewPersonUidsViaCoursePermission(personUid) AS
              /* Select personUids that can be viewed based on CoursePermission given the active user 
                 for their enrolments 
              */
              (SELECT DISTINCT ClazzEnrolment_ForClazzMember.clazzEnrolmentPersonUid AS personUid
                 FROM ClazzEnrolment ClazzEnrolment_ForActiveUser
                      JOIN CoursePermission 
                           ON CoursePermission.cpClazzUid = ClazzEnrolment_ForActiveUser.clazzEnrolmentClazzUid
                          AND CoursePermission.cpToEnrolmentRole = ClazzEnrolment_ForActiveUser.clazzEnrolmentRole
                          AND (CoursePermission.cpPermissionsFlag & ${PermissionFlags.COURSE_VIEW_MEMBERS}) > 0
                      JOIN ClazzEnrolment ClazzEnrolment_ForClazzMember
                           ON ClazzEnrolment_ForClazzMember.clazzEnrolmentClazzUid = CoursePermission.cpClazzUid
                WHERE :accountPersonUid != 0
                  AND ClazzEnrolment_ForActiveUser.clazzEnrolmentPersonUid = :accountPersonUid
                  AND ClazzEnrolment_ForActiveUser.clazzEnrolmentActive
              
               UNION
               /* Select personUids that can be viewed based on CoursePermission for the active user
                  where the CoursePermission is granted directly to them
                */   
               SELECT DISTINCT ClazzEnrolment_ForClazzMember.clazzEnrolmentPersonUid AS personUid
                 FROM CoursePermission
                      JOIN ClazzEnrolment ClazzEnrolment_ForClazzMember
                           ON ClazzEnrolment_ForClazzMember.clazzEnrolmentClazzUid = CoursePermission.cpClazzUid
                WHERE :accountPersonUid != 0
                  AND CoursePermission.cpToPersonUid = :accountPersonUid)
               
         SELECT Person.*, PersonPicture.*
           FROM Person
                LEFT JOIN PersonPicture
                     ON PersonPicture.personPictureUid = Person.personUid
          WHERE /* Begin permission check */ 
                (         
                      ($SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT1
                       ${PermissionFlags.VIEW_ALL_PERSONS}
                       $SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT2)
                    OR (Person.personUid IN 
                               (SELECT CanViewPersonUidsViaCoursePermission.personUid
                                  FROM CanViewPersonUidsViaCoursePermission))
                    OR (Person.personUid = :accountPersonUid)
                )
                /* End permission check */
           AND (:excludeClazz = 0 OR :excludeClazz NOT IN
                    (SELECT clazzEnrolmentClazzUid 
                       FROM ClazzEnrolment 
                      WHERE clazzEnrolmentPersonUid = Person.personUid 
                            AND :timestamp BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined 
                                AND ClazzEnrolment.clazzEnrolmentDateLeft
                        AND ClazzEnrolment.clazzEnrolmentActive))
           AND Person.personType = ${Person.TYPE_NORMAL_PERSON}                  
           AND (Person.personUid NOT IN (:excludeSelected))
           AND (:searchText = '%' 
               OR Person.firstNames || ' ' || Person.lastName LIKE :searchText)
      GROUP BY Person.personUid, PersonPicture.personPictureUid
      ORDER BY CASE(:sortOrder)
               WHEN $SORT_FIRST_NAME_ASC THEN Person.firstNames
               WHEN $SORT_LAST_NAME_ASC THEN Person.lastName
               ELSE ''
               END ASC,
               CASE(:sortOrder)
               WHEN $SORT_FIRST_NAME_DESC THEN Person.firstNames
               WHEN $SORT_LAST_NAME_DESC THEN Person.lastName
               ELSE ''
               END DESC
    """

}