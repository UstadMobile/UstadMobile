package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.*

@Dao
@Repository
abstract class ClazzAssignmentContentJoinDao : BaseDao<ClazzAssignmentContentJoin>,
        OneToManyJoinDao<ClazzAssignmentContentJoin> {

    @Query(FINDBY_CLAZZ_ASSIGNMENT_UID)
    abstract suspend fun findAllContentByClazzAssignmentUidAsync(clazzAssignmentUid: Long, personUid : Long)
            :List <ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>

    @Query(FINDBY_CLAZZ_ASSIGNMENT_UID)
    abstract fun findAllContentByClazzAssignmentUidDF(clazzAssignmentUid: Long, personUid : Long)
            : DataSource.Factory<Int, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>


    @Query("""
        SELECT ContentEntry.title AS contentEntryTitle, ContentEntry.contentEntryUid,
        
        COALESCE(CacheClazzAssignment.cacheStudentScore,0) AS resultScore,
                                           
        COALESCE(CacheClazzAssignment.cacheMaxScore,0) AS resultMax,
                                                         
        COALESCE(CacheClazzAssignment.cacheProgress,0) AS progress,                            
                            
        COALESCE(CacheClazzAssignment.cacheContentComplete,'FALSE') AS contentComplete,
        
        COALESCE(CacheClazzAssignment.cacheSuccess,0) AS success,
        
        MIN(ResultSource.timestamp) AS startDate,
        MAX(ResultSource.timestamp)  AS endDate,
        SUM(ResultSource.resultDuration) AS duration, 
        COUNT(DISTINCT(ResultSource.contextRegistration)) AS attempts
        
        FROM  ClazzAssignmentContentJoin
                LEFT JOIN ContentEntry 
                ON ContentEntry.contentEntryUid = cacjContentUid 
                
                LEFT JOIN CacheClazzAssignment
                ON cacheContentEntryUid = ClazzAssignmentContentJoin.cacjContentUid
                AND cachePersonUid = :personUid
                AND cacheClazzAssignmentUid = :clazzAssignmentUid
                
                
                LEFT JOIN ( SELECT StatementEntity.timestamp, 
                    StatementEntity.statementContentEntryUid, 
                    StatementEntity.contextRegistration, StatementEntity.resultDuration
                      
                 ${Person.FROM_PERSONGROUPMEMBER_JOIN_PERSON_WITH_PERMISSION_PT1} ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT} ${Person.FROM_PERSONGROUPMEMBER_JOIN_PERSON_WITH_PERMISSION_PT2}
                           LEFT JOIN StatementEntity 
                           ON StatementEntity.statementPersonUid = Person.personUid 
                WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid 
                  AND PersonGroupMember.groupMemberActive  
                  AND statementContentEntryUid
                            IN (SELECT cacjContentUid
                                  FROM ClazzAssignmentContentJoin
                                        JOIN ClazzAssignment 
                                        ON ClazzAssignment.caUid = cacjAssignmentUid
                                        
                                        JOIN ClazzEnrolment
                                        ON ClazzEnrolment.clazzEnrolmentClazzUid = ClazzAssignment.caClazzUid
                                        AND ClazzEnrolment.clazzEnrolmentPersonUid = StatementEntity.statementPersonUid
                                 WHERE cacjAssignmentUid = :clazzAssignmentUid
                                  AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
                                  AND ClazzEnrolment.clazzEnrolmentActive
                                  AND StatementEntity.timestamp
                                        BETWEEN ClazzAssignment.caStartDate
                                        AND ClazzAssignment.caGracePeriodDate)
              GROUP BY StatementEntity.statementUid) AS ResultSource
          ON ClazzAssignmentContentJoin.cacjContentUid = ResultSource.statementContentEntryUid   
            
        WHERE ClazzAssignmentContentJoin.cacjAssignmentUid = :clazzAssignmentUid
          AND cacjActive    
          AND (ContentEntry.publik OR :personUid != 0)  
     GROUP BY ResultSource.statementContentEntryUid
     ORDER BY ContentEntry.title, ContentEntry.contentEntryUid   
    """)
    abstract fun findAllContentWithAttemptsByClazzAssignmentUid(clazzAssignmentUid: Long,
                                                               personUid: Long, accountPersonUid: Long):
            DataSource.Factory<Int, ContentWithAttemptSummary>

    @Query("""
        UPDATE ClazzAssignmentContentJoin 
           SET cacjActive = :active,
               cacjLCB = (SELECT nodeClientId 
                            FROM SyncNode LIMIT 1) 
        WHERE cacjUid = :uid """)
    abstract suspend fun updateInActiveByClazzWorkQuestionUid(uid: Long, active : Boolean)


    override suspend fun deactivateByUids(uidList: List<Long>) {
        uidList.forEach {
            updateInActiveByClazzWorkQuestionUid(it, true)
        }
    }


    companion object{
        const val FINDBY_CLAZZ_ASSIGNMENT_UID =
                """
                    SELECT ContentEntry.*, ContentEntryParentChildJoin.*, 
                            Container.*, 
                             COALESCE(CacheClazzAssignment.cacheStudentScore,0) AS resultScore,
                                           
                             COALESCE(CacheClazzAssignment.cacheMaxScore,0) AS resultMax,
                                                         
                             COALESCE(CacheClazzAssignment.cacheProgress,0) AS progress,                            
                            
                             COALESCE(CacheClazzAssignment.cacheContentComplete,'FALSE') AS contentComplete,
                                 
                             COALESCE(CacheClazzAssignment.cacheSuccess,0) AS success
                             
                      FROM ClazzAssignmentContentJoin
                            LEFT JOIN ContentEntry 
                            ON ContentEntry.contentEntryUid = cacjContentUid 
                            
                            LEFT JOIN ContentEntryParentChildJoin 
                            ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid 
                           
                            LEFT JOIN CacheClazzAssignment
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
                      AND cacjActive
                      AND NOT ContentEntry.ceInactive
                      AND (ContentEntry.publik OR :personUid != 0)
                      ORDER BY ContentEntry.title ASC , 
                               ContentEntryParentChildJoin.childIndex, ContentEntry.contentEntryUid
                               """
    }


}
