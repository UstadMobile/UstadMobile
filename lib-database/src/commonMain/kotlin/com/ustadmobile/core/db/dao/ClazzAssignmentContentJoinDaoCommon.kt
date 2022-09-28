package com.ustadmobile.core.db.dao

object ClazzAssignmentContentJoinDaoCommon {

    const val FINDBY_CLAZZ_ASSIGNMENT_UID =
        """
                    SELECT ContentEntry.*, ContentEntryParentChildJoin.*, 
                            Container.*, 
                             COALESCE(ClazzAssignmentRollUp.cacheStudentScore,0) AS resultScore,
                                           
                             COALESCE(ClazzAssignmentRollUp.cacheMaxScore,0) AS resultMax,
                                                         
                             COALESCE(ClazzAssignmentRollUp.cacheProgress,0) AS progress,                            
                            
                             COALESCE(ClazzAssignmentRollUp.cacheContentComplete,'FALSE') AS contentComplete,
                                 
                             COALESCE(ClazzAssignmentRollUp.cacheSuccess,0) AS success,
                             
                             COALESCE(ClazzAssignmentRollUp.cachePenalty,0) AS penalty,
                               
                             COALESCE((CASE WHEN ClazzAssignmentRollUp.cacheContentComplete 
                                            THEN 1 ELSE 0 END),0) AS totalCompletedContent,
                        
                             0 as assignmentContentWeight,
                             1 as totalContent
                           
                             
                      FROM ClazzAssignmentContentJoin
                            LEFT JOIN ContentEntry 
                            ON ContentEntry.contentEntryUid = cacjContentUid 
                            
                            LEFT JOIN ContentEntryParentChildJoin 
                            ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid 
                           
                            LEFT JOIN ClazzAssignmentRollUp
                            ON cacheContentEntryUid = ClazzAssignmentContentJoin.cacjContentUid
                                AND cachePersonUid = :personUid
                                AND cacheClazzAssignmentUid = :clazzAssignmentUid
                                                        
                            
                            LEFT JOIN Container 
                            ON Container.containerUid = 
                                (SELECT containerUid 
                                   FROM Container 
                                  WHERE containerContentEntryUid =  ContentEntry.contentEntryUid 
                               ORDER BY cntLastModified DESC LIMIT 1)
                               
                    WHERE ClazzAssignmentContentJoin.cacjAssignmentUid = :clazzAssignmentUid
                      AND ClazzAssignmentContentJoin.cacjActive
                      AND NOT ContentEntry.ceInactive
                      AND (ContentEntry.publik OR :personUid != 0)
                      ORDER BY ContentEntry.title ASC , 
                               ContentEntryParentChildJoin.childIndex, ContentEntry.contentEntryUid
                               """

}