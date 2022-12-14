package com.ustadmobile.core.db.dao

import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.School

object EntityRoleCommon {

    const val FILTER_BY_PERSON_UID2 =
        """
                    SELECT  
                    (CASE 
                        WHEN EntityRole.erTableId = ${Clazz.TABLE_ID}	THEN (SELECT Clazz.clazzName FROM Clazz WHERE Clazz.clazzUid = EntityRole.erEntityUid)
                        WHEN EntityRole.erTableId = ${Person.TABLE_ID}	THEN (SELECT Person.firstNames||' '||Person.lastName FROM Person WHERE Person.personUid = EntityRole.erEntityUid)
                        WHEN EntityRole.erTableId = ${School.TABLE_ID}	THEN (SELECT School.schoolName FROM School WHERE School.schoolUid = EntityRole.erEntityUid)
                        ELSE '' 
                    END) as entityRoleScopeName,
                    Role.*, EntityRole.* FROM EntityRole
                    LEFT JOIN Role ON EntityRole.erRoleUid = Role.roleUid 
                    WHERE EntityRole.erGroupUid = :personGroupUid
                    AND CAST(EntityRole.erActive AS INTEGER) = 1 
                """
}