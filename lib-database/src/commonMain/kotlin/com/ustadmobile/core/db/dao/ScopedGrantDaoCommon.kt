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

    const val SQL_FIND_BY_TABLE_AND_ENTITY_WITH_PARAMS = """
        SELECT ScopedGrant.*,
               CASE
               WHEN Person.firstNames IS NOT NULL THEN Person.firstNames||' '||Person.lastName
               ELSE PersonGroup.groupName 
               END AS name
          FROM ScopedGrant
               JOIN PersonGroup 
                    ON ScopedGrant.sgGroupUid = PersonGroup.groupUid
               LEFT JOIN Person
                         ON Person.personGroupUid = PersonGroup.groupUid
         WHERE ScopedGrant.sgTableId = :tableId
             AND ScopedGrant.sgEntityUid = :entityUid  
             AND (:searchText = '%' 
                   OR name LIKE :searchText)
         ORDER BY CASE(:sortOrder)
               WHEN ${PersonDaoCommon.SORT_FIRST_NAME_ASC} THEN name
               ELSE ''
               END ASC,
               CASE(:sortOrder)
               WHEN ${PersonDaoCommon.SORT_FIRST_NAME_DESC} THEN name
               ELSE ''
               END DESC

    """

    const val SQL_FIND_PEOPLE_GRANTED_BY_TABLE_AND_ENTITY = """
        SELECT Person.personUid
          FROM ScopedGrant
               JOIN PersonGroup 
                    ON ScopedGrant.sgGroupUid = PersonGroup.groupUid
               LEFT JOIN Person
                         ON Person.personGroupUid = PersonGroup.groupUid
         WHERE ScopedGrant.sgTableId = :tableId
             AND ScopedGrant.sgEntityUid = :entityUid  
             AND Person.personUid IS NOT NULL 
    """

}