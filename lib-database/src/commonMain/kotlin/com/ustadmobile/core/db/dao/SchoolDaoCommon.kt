package com.ustadmobile.core.db.dao

import com.ustadmobile.lib.db.entities.School

object SchoolDaoCommon {
    const val SORT_NAME_ASC = 1

    const val SORT_NAME_DESC = 2

    const val ENTITY_PERSONS_WITH_PERMISSION_PT1 = """
            SELECT DISTINCT Person.PersonUid FROM Person
            LEFT JOIN PersonGroupMember ON Person.personUid = PersonGroupMember.groupMemberPersonUid
            LEFT JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid
            LEFT JOIN Role ON EntityRole.erRoleUid = Role.roleUid
            WHERE 
            CAST(Person.admin AS INTEGER) = 1
            OR 
            (EntityRole.ertableId = ${School.TABLE_ID} AND 
            EntityRole.erEntityUid = School.schoolUid AND
            (Role.rolePermissions &  
        """

    const val ENTITY_PERSONS_WITH_PERMISSION_PT2 = ") > 0)"

    const val ENTITY_PERSONS_WITH_PERMISSION = "${ENTITY_PERSONS_WITH_PERMISSION_PT1} " +
            ":permission ${ENTITY_PERSONS_WITH_PERMISSION_PT2}"

}