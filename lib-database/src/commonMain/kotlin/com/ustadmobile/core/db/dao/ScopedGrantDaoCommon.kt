package com.ustadmobile.core.db.dao

import com.ustadmobile.lib.db.entities.ScopedGrant

object ScopedGrantDaoCommon {

    const val SQL_FIND_BY_TABLE_AND_ENTITY = """
        SELECT ScopedGrant.*,
               CASE
               WHEN Person.firstNames IS NOT NULL THEN Person.firstNames
               ELSE PersonGroup.groupName 
               END AS name
          FROM ScopedGrant
               JOIN PersonGroup 
                    ON ScopedGrant.sgGroupUid = PersonGroup.groupUid
               LEFT JOIN Person
                         ON Person.personGroupUid = PersonGroup.groupUid
         WHERE ScopedGrant.sgTableId = :tableId
               AND ScopedGrant.sgEntityUid = :entityUid  
    """


    const val SQL_USER_HAS_SYSTEM_LEVEL_PERMISSION = """
        SELECT EXISTS(
                SELECT PersonGroupMember.groupMemberGroupUid
                  FROM PersonGroupMember 
                       JOIN ScopedGrant
                           ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
                 WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid
                   AND (ScopedGrant.sgEntityUid = 0 OR ScopedGrant.sgEntityUid = ${ScopedGrant.ALL_ENTITIES})
                   AND (ScopedGrant.sgTableId = 0 OR ScopedGrant.sgTableId = ${ScopedGrant.ALL_TABLES})
                   AND (ScopedGrant.sgPermissions & :permission) > 0    
               )
    """
}