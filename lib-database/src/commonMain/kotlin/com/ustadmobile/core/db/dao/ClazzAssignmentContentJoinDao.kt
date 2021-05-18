package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ClazzAssignmentContentJoin
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.lib.db.entities.ContentWithAttemptSummary

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


    //COUNT(DISTINCT(ResultSource.contextRegistration))

    //MIN(ResultSource.timestamp)

    //MAX(ResultSource.timestamp)
    //SUM(ResultSource.resultDuration)

    @Query("""
        SELECT ContentEntry.title AS contentEntryTitle, ContentEntry.contentEntryUid,
        
        COALESCE(CacheClazzAssignment.cacheStudentScore,0) AS resultScore,
                                           
        COALESCE(CacheClazzAssignment.cacheMaxScore,0) AS resultMax,
                                                         
        COALESCE(CacheClazzAssignment.cacheProgress,0) AS progress,                            
                            
        COALESCE(CacheClazzAssignment.cacheContentComplete,'FALSE') AS contentComplete,
        
            0 AS attempts, 
            0 AS startDate, 
            0 AS endDate, 
            0 AS duration
        
        FROM ClazzAssignmentContentJoin
                LEFT JOIN ContentEntry 
                ON ContentEntry.contentEntryUid = cacjContentUid 
                
                LEFT JOIN CacheClazzAssignment
                ON cacheContentEntryUid = ClazzAssignmentContentJoin.cacjContentUid
                AND cachePersonUid = :personUid
                AND cacheClazzAssignmentUid = :clazzAssignmentUid
            
        WHERE ClazzAssignmentContentJoin.cacjAssignmentUid = :clazzAssignmentUid
          AND cacjActive    
          AND (ContentEntry.publik OR :personUid != 0)      
     ORDER BY ContentEntry.title, ContentEntry.contentEntryUid   
    """)
    abstract fun findAllContentWithAttemptsByClazzAssignmentUid(clazzAssignmentUid: Long,
                                                               personUid: Long):
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
                            
                             COALESCE(CacheClazzAssignment.cacheContentComplete,'FALSE') AS contentComplete       
                             
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
