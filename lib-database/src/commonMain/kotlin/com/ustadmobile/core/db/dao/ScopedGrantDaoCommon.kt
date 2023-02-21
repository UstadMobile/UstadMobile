package com.ustadmobile.core.db.dao

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


}