package com.ustadmobile.core.db.dao.xapi

import com.ustadmobile.core.db.dao.ClazzEnrolmentDaoCommon
import com.ustadmobile.lib.db.entities.xapi.XapiEntityObjectTypeFlags

object StatementDaoCommon {

    const val FROM_STATEMENT_ENTITY_WHERE_MATCHES_ACCOUNT_PERSON_UID_AND_CONTENT_ENTRY_ROOT = """
          FROM StatementEntity
         WHERE StatementEntity.statementActorPersonUid = :accountPersonUid
           AND StatementEntity.statementContentEntryUid = :contentEntryUid
           AND CAST(StatementEntity.completionOrProgress AS INTEGER) = 1
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
         AND CAST(StatementEntity.completionOrProgress AS INTEGER) = 1
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
              CAST(StatementEntity.completionOrProgress AS INTEGER) = 1
          AND CAST(StatementEntity.resultCompletion AS INTEGER) = 1    
          AND CAST(StatementEntity.resultSuccess AS INTEGER) = 1
    """

    //Exactly as above, changing only the table name to StatusStatements
    const val STATUS_STATEMENTS_IS_SUCCESSFUL_COMPLETION_CLAUSE = """
              CAST(StatusStatements.completionOrProgress AS INTEGER) = 1
          AND CAST(StatusStatements.resultCompletion AS INTEGER) = 1    
          AND CAST(StatusStatements.resultSuccess AS INTEGER) = 1
    """


    const val STATEMENT_ENTITY_IS_FAILED_COMPLETION_CLAUSE = """
              CAST(StatementEntity.completionOrProgress AS INTEGER) = 1
          AND CAST(StatementEntity.resultCompletion AS INTEGER) = 1
          AND CAST(StatementEntity.resultSuccess AS INTEGER) = 0
    """

    const val STATUS_STATEMENTS_IS_FAILED_COMPLETION_CLAUSE = """
              CAST(StatusStatements.completionOrProgress AS INTEGER) = 1
          AND CAST(StatusStatements.resultCompletion AS INTEGER) = 1
          AND CAST(StatusStatements.resultSuccess AS INTEGER) = 0
    """


    //If the GroupMemberActorJoin does not match the statement and person, it will be null
    // This should be optimized: use a CTE to find the actor uids for persons that in the query
    const val STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS = """
            StatementEntity.statementCbUid = PersonUidsAndCourseBlocks.cbUid
        AND StatementEntity.statementActorUid IN (
            SELECT ActorUidsForPersonUid.actorUid
              FROM ActorUidsForPersonUid
             WHERE ActorUidsForPersonUid.actorPersonUid = PersonUidsAndCourseBlocks.personUid)  
                   
    """

    //Same as above, modified for where tables are using an _inner postfix
    const val STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS_INNER = """
            StatementEntity_Inner.statementCbUid = PersonUidsAndCourseBlocks.cbUid
        AND StatementEntity_Inner.statementActorUid IN (
            SELECT ActorUidsForPersonUid.actorUid
              FROM ActorUidsForPersonUid
             WHERE ActorUidsForPersonUid.actorPersonUid = PersonUidsAndCourseBlocks.personUid)  
                   
    """

    //Join the actor entity, and, if relevant, the GroupMemberActorJoin for the actor group and
    //person as per ActorUidsForPersonUid
    const val JOIN_ACTOR_TABLES_FROM_ACTOR_UIDS_FOR_PERSON_UID = """
       JOIN ActorEntity
            ON ActorEntity.actorUid = StatementEntity.statementActorUid
       LEFT JOIN GroupMemberActorJoin
            ON ActorEntity.actorObjectType = ${XapiEntityObjectTypeFlags.GROUP}
               AND (GroupMemberActorJoin.gmajGroupActorUid, GroupMemberActorJoin.gmajMemberActorUid) IN (
                   SELECT GroupMemberActorJoin.gmajGroupActorUid, 
                          GroupMemberActorJoin.gmajMemberActorUid
                     FROM GroupMemberActorJoin
                    WHERE GroupMemberActorJoin.gmajGroupActorUid = StatementEntity.statementActorUid
                      AND GroupMemberActorJoin.gmajMemberActorUid IN (
                          SELECT ActorUidsForPersonUid.actorUid
                            FROM ActorUidsForPersonUid
                           WHERE ActorUidsForPersonUid.actorPersonUid = PersonUidsAndCourseBlocks.personUid))
    """

    const val JOIN_ACTOR_TABLES_FROM_ACTOR_UIDS_FOR_PERSON_UID_INNER = """
       JOIN ActorEntity ActorEntity_Inner
            ON ActorEntity_Inner.actorUid = StatementEntity_Inner.statementActorUid
       LEFT JOIN GroupMemberActorJoin GroupMemberActorJoin_Inner
            ON ActorEntity_Inner.actorObjectType = ${XapiEntityObjectTypeFlags.GROUP}
               AND (GroupMemberActorJoin_Inner.gmajGroupActorUid, GroupMemberActorJoin_Inner.gmajMemberActorUid) IN (
                   SELECT GroupMemberActorJoin.gmajGroupActorUid, 
                          GroupMemberActorJoin.gmajMemberActorUid
                     FROM GroupMemberActorJoin
                    WHERE GroupMemberActorJoin.gmajGroupActorUid = StatementEntity.statementActorUid
                      AND GroupMemberActorJoin.gmajMemberActorUid IN (
                          SELECT ActorUidsForPersonUid.actorUid
                            FROM ActorUidsForPersonUid
                           WHERE ActorUidsForPersonUid.actorPersonUid = PersonUidsAndCourseBlocks.personUid))
    """


    const val ACTOR_UIDS_FOR_PERSONUIDS_CTE = """
        -- Get the ActorUids for the PersonUids See ActoryEntity doc for info on this join relationship
        AgentActorUidsForPersonUid(actorUid, actorPersonUid) AS(
             SELECT ActorEntity.actorUid AS actorUid, 
                    ActorEntity.actorPersonUid AS actorPersonUid
               FROM ActorEntity
              WHERE ActorEntity.actorPersonUid IN
                    (SELECT PersonUids.personUid
                       FROM PersonUids)           
        ),
        
        -- Add in group actor uids
        ActorUidsForPersonUid(actorUid, actorPersonUid) AS (
             SELECT AgentActorUidsForPersonUid.actorUid AS actorUid,
                    AgentActorUidsForPersonUid.actorPersonUid AS actorPersonUid
               FROM AgentActorUidsForPersonUid     
              UNION 
             SELECT GroupMemberActorJoin.gmajGroupActorUid AS actorUid,
                    AgentActorUidsForPersonUid.actorPersonUid AS actorPersonUid
               FROM AgentActorUidsForPersonUid
                    JOIN GroupMemberActorJoin 
                         ON GroupMemberActorJoin.gmajMemberActorUid = AgentActorUidsForPersonUid.actorUid
        )
    """


}