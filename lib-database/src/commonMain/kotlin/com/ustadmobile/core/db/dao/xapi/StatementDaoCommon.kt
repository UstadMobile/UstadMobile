package com.ustadmobile.core.db.dao.xapi

import com.ustadmobile.core.db.dao.ClazzEnrolmentDaoCommon
import com.ustadmobile.lib.db.entities.xapi.XapiEntityObjectTypeFlags

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


    //If the GroupMemberActorJoin does not match the statement and person, it will be null
    const val STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS = """
            StatementEntity.statementCbUid = PersonUidsAndCourseBlocks.cbUid
        AND (    ActorEntity.actorPersonUid = PersonUidsAndCourseBlocks.personUid
              OR GroupMemberActorJoin.gmajGroupActorUid = StatementEntity.statementActorUid)
                   
    """

    //Join the actor entity, and, if relevant, the GroupMemberActorJoin for the actor group and
    //person as per ActorUidsForPersonUid
    const val JOIN_ACTOR_TABLES_FROM_ACTOR_UIDS_FOR_PERSON_UID = """
       JOIN ActorEntity
            ON StatementEntity.statementActorUid = ActorEntity.actorUid
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

    const val JOIN_ACTOR_TABLES_FROM_STATEMENT_OUTER = """
       JOIN ActorEntity ActorEntity_Outer
            ON ActorEntity_Outer.actorUid = StatementEntity_Outer.statementActorUid
       LEFT JOIN GroupMemberActorJoin GroupMemberActorJoin_Outer
            ON ActorEntity_Outer.actorObjectType = ${XapiEntityObjectTypeFlags.GROUP}
               AND (GroupMemberActorJoin_Outer.gmajGroupActorUid, GroupMemberActorJoin_Outer.gmajMemberActorUid) IN (
                   SELECT GroupMemberActorJoin.gmajGroupActorUid, 
                          GroupMemberActorJoin.gmajMemberActorUid
                     FROM GroupMemberActorJoin
                    WHERE GroupMemberActorJoin.gmajGroupActorUid = StatementEntity_Outer.statementActorUid
                      AND GroupMemberActorJoin.gmajMemberActorUid IN (
                          SELECT ActorUidsForPersonUid.actorUid
                            FROM ActorUidsForPersonUid
                           WHERE ActorUidsForPersonUid.actorPersonUid = PersonUidsAndCourseBlocks.personUid))
    """



}