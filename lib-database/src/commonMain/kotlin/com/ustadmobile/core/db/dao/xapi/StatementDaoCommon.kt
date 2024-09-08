package com.ustadmobile.core.db.dao.xapi

import com.ustadmobile.core.db.dao.ClazzEnrolmentDaoCommon
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseBlock
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
                AND StatementEntity.statementActorUid IN (
                    SELECT ActorUidsForPersonUid.actorUid
                      FROM ActorUidsForPersonUid)
                AND StatementEntity.statementClazzUid = :clazzUid
                AND (    (CAST(StatementEntity.resultCompletion AS INTEGER) = 1)
                      OR (StatementEntity.extensionProgress IS NOT NULL))
    """

    const val STATEMENT_ENTITY_IS_SUCCESSFUL_COMPLETION_CLAUSE = """
              CAST(StatementEntity.completionOrProgress AS INTEGER) = 1
          AND CAST(StatementEntity.resultCompletion AS INTEGER) = 1    
          AND CAST(StatementEntity.resultSuccess AS INTEGER) = 1
    """

    const val STATEMENT_ENTITY_IS_FAILED_COMPLETION_CLAUSE = """
              CAST(StatementEntity.completionOrProgress AS INTEGER) = 1
          AND CAST(StatementEntity.resultCompletion AS INTEGER) = 1
          AND CAST(StatementEntity.resultSuccess AS INTEGER) = 0
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


    const val FIND_STATUS_FOR_STUDENTS_SQL = """
        WITH PersonUids(personUid) AS (
             SELECT Person.personUid
               FROM Person
              WHERE Person.personUid IN (:studentPersonUids)
        ),
        
        $ACTOR_UIDS_FOR_PERSONUIDS_CTE,
        
        PersonUidsAndCourseBlocks(personUid, cbUid, cbType, caMarkingType) AS (
             SELECT Person.personUid AS personUid,
                    CourseBlock.cbUid AS cbUid,
                    CourseBlock.cbType AS cbType,
                    ClazzAssignment.caMarkingType AS caMarkingType
               FROM Person
                    JOIN CourseBlock
                         ON CourseBlock.cbClazzUid = :clazzUid
                    LEFT JOIN ClazzAssignment
                         ON CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
                        AND ClazzAssignment.caUid = CourseBlock.cbEntityUid     
              WHERE Person.personUid IN (:studentPersonUids)       
        )
        
        SELECT PersonUidsAndCourseBlocks.personUid AS sPersonUid,
               PersonUidsAndCourseBlocks.cbUid AS sCbUid,
               (SELECT MAX(StatementEntity.extensionProgress)
                  FROM StatementEntity
                       $JOIN_ACTOR_TABLES_FROM_ACTOR_UIDS_FOR_PERSON_UID
                 WHERE $STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS 
               ) AS sProgress,
               (SELECT EXISTS(
                       SELECT 1
                         FROM StatementEntity
                              $JOIN_ACTOR_TABLES_FROM_ACTOR_UIDS_FOR_PERSON_UID
                        WHERE ($STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS)
                          AND CAST(StatementEntity.resultCompletion AS INTEGER) = 1
               )) AS sIsCompleted,
               (SELECT CASE
                       /*If there is a statement marked as success, then count as successful even if
                        *there were subsequent failed attempts
                        */
                       WHEN (
                            SELECT EXISTS(
                                    SELECT 1
                                      FROM StatementEntity
                                           $JOIN_ACTOR_TABLES_FROM_ACTOR_UIDS_FOR_PERSON_UID
                                    WHERE ($STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS)
                                      AND CAST(StatementEntity.resultSuccess AS INTEGER) = 1
                                   )                           
                       ) THEN 1
                       /*If there are no statements marked as success, however there are statements marekd as fail,
                        *then count as fail 
                        */
                       WHEN (
                            SELECT EXISTS(
                                    SELECT 1
                                      FROM StatementEntity
                                           $JOIN_ACTOR_TABLES_FROM_ACTOR_UIDS_FOR_PERSON_UID
                                    WHERE ($STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS)
                                      AND CAST(StatementEntity.resultSuccess AS INTEGER) = 0
                                   )                           
                       ) THEN 0
                       /* Else there is no known success/fail result*/
                       ELSE NULL
                       END
               ) AS sIsSuccess,
               -- See ClazzGradebookScreen for info on which score is selected
               (SELECT CASE
                       -- When there is a peer marked assignment, take the average of the latest distinct ...
                       WHEN (     PersonUidsAndCourseBlocks.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
                              AND PersonUidsAndCourseBlocks.caMarkingType = ${ClazzAssignment.MARKED_BY_PEERS}
                            ) 
                            THEN (SELECT AVG(StatementEntity.resultScoreScaled)
                                    FROM StatementEntity
                                         $JOIN_ACTOR_TABLES_FROM_ACTOR_UIDS_FOR_PERSON_UID
                                   WHERE ($STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS)
                                     AND StatementEntity.timestamp = (
                                         SELECT MAX(StatementEntity_Inner.timestamp)
                                           FROM StatementEntity StatementEntity_Inner
                                                $JOIN_ACTOR_TABLES_FROM_ACTOR_UIDS_FOR_PERSON_UID_INNER
                                          WHERE ($STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS_INNER)
                                            AND StatementEntity_Inner.contextInstructorActorUid = StatementEntity.contextInstructorActorUid)
                                   LIMIT 1)
                       -- When an assignment, but not peer marked, then the latest score     
                       WHEN PersonUidsAndCourseBlocks.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
                            THEN (SELECT StatementEntity.resultScoreScaled
                                    FROM StatementEntity
                                         $JOIN_ACTOR_TABLES_FROM_ACTOR_UIDS_FOR_PERSON_UID
                                   WHERE ($STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS)
                                ORDER BY StatementEntity.timestamp DESC
                                   LIMIT 1)
                       -- else the best score accomplished so far            
                       ELSE (SELECT MAX(StatementEntity.resultScoreScaled) 
                               FROM StatementEntity
                                    $JOIN_ACTOR_TABLES_FROM_ACTOR_UIDS_FOR_PERSON_UID
                              WHERE ($STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS))            
                       END
               ) AS sScoreScaled
          FROM PersonUidsAndCourseBlocks
         WHERE :accountPersonUid = :accountPersonUid 
    """

    const val SELECT_STATUS_STATEMENTS_FOR_ACTOR_PERSON_UIDS = """
        -- Fetch all statements that could be completion or progress for the Gradebook report
        SELECT StatementEntity.*, ActorEntity.*, GroupMemberActorJoin.*
          FROM StatementEntity
               JOIN ActorEntity
                    ON ActorEntity.actorUid = StatementEntity.statementActorUid
               LEFT JOIN GroupMemberActorJoin
                    ON ActorEntity.actorObjectType = ${XapiEntityObjectTypeFlags.GROUP}
                       AND GroupMemberActorJoin.gmajGroupActorUid = StatementEntity.statementActorUid
                       AND GroupMemberActorJoin.gmajMemberActorUid IN (
                           SELECT DISTINCT ActorUidsForPersonUid.actorUid
                             FROM ActorUidsForPersonUid)
         WHERE StatementEntity.statementClazzUid = :clazzUid
           AND StatementEntity.completionOrProgress = :completionOrProgressTrueVal
           AND StatementEntity.statementActorUid IN (
               SELECT DISTINCT ActorUidsForPersonUid.actorUid
                 FROM ActorUidsForPersonUid) 
           AND (      StatementEntity.resultScoreScaled IS NOT NULL
                   OR StatementEntity.resultCompletion IS NOT NULL
                   OR StatementEntity.resultSuccess IS NOT NULL
                   OR StatementEntity.extensionProgress IS NOT NULL 
               )
    """


}