package com.ustadmobile.core.db.dao

import com.ustadmobile.core.db.dao.xapi.StatementDaoCommon

object ContentEntryDaoCommon {

    const val SORT_TITLE_ASC = 1

    const val SORT_TITLE_DESC = 2

    /**
     * Subqueries to provide the fields for BlockStatus for the given ContentEntry where there is a
     * query parameter for the account person.
     */
    const val SELECT_STATUS_FIELDS_FOR_CONTENT_ENTRY = """
                   (SELECT MAX(StatementEntity.extensionProgress)
                     FROM StatementEntity
                    WHERE (SELECT includeResults FROM IncludeResults) = 1
                      AND StatementEntity.statementActorPersonUid = :accountPersonUid
                      AND StatementEntity.statementContentEntryUid = ContentEntry.contentEntryUid
                      AND CAST(StatementEntity.completionOrProgress AS INTEGER) = 1
                   ) AS sProgress,
                   (SELECT CASE
                       -- If a successful completion statement exists, then count as success
                       WHEN (SELECT EXISTS(
                                    SELECT 1
                                      FROM StatementEntity
                                     WHERE (SELECT includeResults FROM IncludeResults) = 1
                                       AND StatementEntity.statementActorPersonUid = :accountPersonUid
                                       AND StatementEntity.statementContentEntryUid = ContentEntry.contentEntryUid 
                                       AND (${StatementDaoCommon.STATEMENT_ENTITY_IS_SUCCESSFUL_COMPLETION_CLAUSE})))
                            THEN 1
                       -- Else if no success record exists, however a fail record exists, mark as failed
                       WHEN (SELECT EXISTS(
                                    SELECT 1
                                      FROM StatementEntity
                                     WHERE (SELECT includeResults FROM IncludeResults) = 1
                                       AND StatementEntity.statementActorPersonUid = :accountPersonUid
                                       AND StatementEntity.statementContentEntryUid = ContentEntry.contentEntryUid 
                                       AND (${StatementDaoCommon.STATEMENT_ENTITY_IS_FAILED_COMPLETION_CLAUSE})))
                            THEN 0
                            
                       ELSE NULL
                       END                    
                   ) AS sIsSuccess,
                   (SELECT EXISTS(
                           SELECT 1
                             FROM StatementEntity
                            WHERE (SELECT includeResults FROM IncludeResults) = 1
                              AND StatementEntity.statementActorPersonUid = :accountPersonUid
                              AND StatementEntity.statementContentEntryUid = ContentEntry.contentEntryUid
                              AND CAST(StatementEntity.resultCompletion AS INTEGER) = 1
                              AND CAST(StatementEntity.completionOrProgress AS INTEGER) = 1)
                   ) AS sIsCompleted,
                   (SELECT MAX(StatementEntity.resultScoreScaled)
                      FROM StatementEntity
                     WHERE (SELECT includeResults FROM IncludeResults) = 1
                       AND StatementEntity.statementActorPersonUid = :accountPersonUid
                       AND StatementEntity.statementContentEntryUid = ContentEntry.contentEntryUid
                   ) AS sScoreScaled
    """

    const val SELECT_ACCOUNT_PERSON_AND_STATUS_FIELDS = """
               :accountPersonUid AS sPersonUid,
               0 AS sCbUid,
               $SELECT_STATUS_FIELDS_FOR_CONTENT_ENTRY
    """

}

