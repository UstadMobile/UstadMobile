package com.ustadmobile.core.db.dao

import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.School

object PersonDaoCommon {

    const val SORT_FIRST_NAME_ASC = 1

    const val SORT_FIRST_NAME_DESC = 2

    const val SORT_LAST_NAME_ASC = 3

    const val SORT_LAST_NAME_DESC = 4

    const val SQL_SELECT_LIST_WITH_PERMISSION = """
         SELECT Person.* 
           FROM PersonGroupMember 
                ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_PERSON_SELECT}
                    ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
         WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid
           AND PersonGroupMember.groupMemberActive 
           AND (:excludeClazz = 0 OR :excludeClazz NOT IN
                    (SELECT clazzEnrolmentClazzUid 
                       FROM ClazzEnrolment 
                      WHERE clazzEnrolmentPersonUid = Person.personUid 
                            AND :timestamp BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined 
                                AND ClazzEnrolment.clazzEnrolmentDateLeft
           AND ClazzEnrolment.clazzEnrolmentActive))
           AND (:excludeSchool = 0 OR :excludeSchool NOT IN
                    (SELECT schoolMemberSchoolUid
                      FROM SchoolMember 
                     WHERE schoolMemberPersonUid = Person.personUid 
                       AND :timestamp BETWEEN SchoolMember.schoolMemberJoinDate
                            AND SchoolMember.schoolMemberLeftDate ))
           AND Person.personType = ${Person.TYPE_NORMAL_PERSON}                  
           AND (Person.personUid NOT IN (:excludeSelected))
           AND (:searchText = '%' 
               OR Person.firstNames || ' ' || Person.lastName LIKE :searchText)
      GROUP BY Person.personUid
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


    internal const val ENTITY_PERSONS_WITH_PERMISSION_PT1 = """
            SELECT DISTINCT Person_Perm.personUid FROM Person Person_Perm
            LEFT JOIN PersonGroupMember ON Person_Perm.personUid = PersonGroupMember.groupMemberPersonUid
            LEFT JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid
            LEFT JOIN Role ON EntityRole.erRoleUid = Role.roleUid
            WHERE
            CAST(Person_Perm.admin AS INTEGER) = 1 OR ( (
            """
    internal const val ENTITY_PERSONS_WITH_PERMISSION_PT2 =  """
            = 0) AND (Person_Perm.personUid = Person.personUid))
            OR
            (
            ((EntityRole.erTableId = ${Person.TABLE_ID} AND EntityRole.erEntityUid = Person.personUid) OR 
            (EntityRole.erTableId = ${Clazz.TABLE_ID} AND EntityRole.erEntityUid IN (SELECT DISTINCT clazzEnrolmentClazzUid FROM ClazzEnrolment WHERE clazzEnrolmentPersonUid = Person.personUid)) OR
            (EntityRole.erTableId = ${School.TABLE_ID} AND EntityRole.erEntityUid IN (SELECT DISTINCT schoolMemberSchoolUid FROM SchoolMember WHERE schoolMemberPersonUid = Person.PersonUid)) OR
            (EntityRole.erTableId = ${School.TABLE_ID} AND EntityRole.erEntityUid IN (
                SELECT DISTINCT Clazz.clazzSchoolUid 
                FROM Clazz
                JOIN ClazzEnrolment ON ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid AND ClazzEnrolment.clazzEnrolmentPersonUid = Person.personUid
            ))
            ) 
            AND (Role.rolePermissions & 
        """

    internal const val ENTITY_PERSONS_WITH_PERMISSION_PT4 = ") > 0)"

    const val SESSION_LENGTH = 28L * 24L * 60L * 60L * 1000L// 28 days

    @Deprecated("Replaced with ScopedGrant")
    const val ENTITY_PERSONS_WITH_LEARNING_RECORD_PERMISSION = "$ENTITY_PERSONS_WITH_PERMISSION_PT1 0 ${ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT} $ENTITY_PERSONS_WITH_PERMISSION_PT4"

}