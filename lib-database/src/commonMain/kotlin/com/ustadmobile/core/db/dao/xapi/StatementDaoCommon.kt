package com.ustadmobile.core.db.dao.xapi

import com.ustadmobile.core.db.dao.ClazzEnrolmentDaoCommon

object StatementDaoCommon {

    const val FROM_STATEMENT_ENTITY_WHERE_MATCHES_ACCOUNT_PERSON_UID_AND_CONTENT_ENTRY_ROOT = """
          FROM StatementEntity
         WHERE StatementEntity.statementActorPersonUid = :accountPersonUid
           AND StatementEntity.statementContentEntryUid = :contentEntryUid
           AND CAST(StatementEntity.contentEntryRoot AS INTEGER) = 1
           AND (:courseBlockUid = 0 OR StatementEntity.statementCbUid = :courseBlockUid)
    """

    const val FROM_STATEMENT_ENTITY_STATUS_STATEMENTS_FOR_CONTENT_ENTRY = """
            $FROM_STATEMENT_ENTITY_WHERE_MATCHES_ACCOUNT_PERSON_UID_AND_CONTENT_ENTRY_ROOT
        AND (    (CAST(StatementEntity.resultCompletion AS INTEGER) = 1)
              OR (StatementEntity.extensionProgress IS NOT NULL))
        
        
    """

    const val FROM_STATEMENT_ENTITY_WHERE_MATCHES_ACCOUNT_PERSON_UID_AND_PARENT_CONTENT_ENTRY_ROOT = """
        FROM StatementEntity
       WHERE StatementEntity.statementActorPersonUid = :accountPersonUid
         AND StatementEntity.statementContentEntryUid IN (
             SELECT ContentEntryParentChildJoin.cepcjChildContentEntryUid
               FROM ContentEntryParentChildJoin
              WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid)
         AND CAST(StatementEntity.contentEntryRoot AS INTEGER) = 1
         AND (    (CAST(StatementEntity.resultCompletion AS INTEGER) = 1)
              OR (StatementEntity.extensionProgress IS NOT NULL))     
    """


    const val FROM_STATEMENT_ENTITY_STATUS_STATEMENTS_FOR_CLAZZ_STUDENT = """
               FROM StatementEntity
              WHERE (${ClazzEnrolmentDaoCommon.SELECT_ACCOUNT_PERSON_UID_IS_STUDENT_IN_CLAZZ_UID})
                AND StatementEntity.statementActorPersonUid = :accountPersonUid
                AND StatementEntity.statementClazzUid = :clazzUid
                AND (    (CAST(StatementEntity.resultCompletion AS INTEGER) = 1)
                      OR (StatementEntity.extensionProgress IS NOT NULL))
    """

    const val STATEMENT_ENTITY_IS_SUCCESSFUL_COMPLETION_CLAUSE = """
              CAST(StatementEntity.contentEntryRoot AS INTEGER) = 1
          AND CAST(StatementEntity.resultCompletion AS INTEGER) = 1    
          AND CAST(StatementEntity.resultSuccess AS INTEGER) = 1
    """

    //Exactly as above, changing only the table name to StatusStatements
    const val STATUS_STATEMENTS_IS_SUCCESSFUL_COMPLETION_CLAUSE = """
              CAST(StatusStatements.contentEntryRoot AS INTEGER) = 1
          AND CAST(StatusStatements.resultCompletion AS INTEGER) = 1    
          AND CAST(StatusStatements.resultSuccess AS INTEGER) = 1
    """


    const val STATEMENT_ENTITY_IS_FAILED_COMPLETION_CLAUSE = """
              CAST(StatementEntity.contentEntryRoot AS INTEGER) = 1
          AND CAST(StatementEntity.resultCompletion AS INTEGER) = 1
          AND CAST(StatementEntity.resultSuccess AS INTEGER) = 0
    """

    const val STATUS_STATEMENTS_IS_FAILED_COMPLETION_CLAUSE = """
              CAST(StatusStatements.contentEntryRoot AS INTEGER) = 1
          AND CAST(StatusStatements.resultCompletion AS INTEGER) = 1
          AND CAST(StatusStatements.resultSuccess AS INTEGER) = 0
    """

    //This will evolve to include handling group membership
    //May also need to check that the statement is for the root entry.
    const val STATEMENT_MATCHES_PERSON_AND_COURSEBLOCK_CLAUSE = """
         StatementEntity.statementCbUid = CourseBlock.cbUid
     AND ActorEntity.actorAccountName = Person.username 
    """




    const val SORT_FIRST_NAME_ASC = 1

    const val SORT_FIRST_NAME_DESC = 2

    const val SORT_LAST_NAME_ASC = 3

    const val SORT_LAST_NAME_DESC = 4

    const val SORT_LAST_ACTIVE_ASC = 5

    const val SORT_LAST_ACTIVE_DESC = 6

}