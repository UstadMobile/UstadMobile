package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ClazzAssignmentContentJoin
import com.ustadmobile.lib.db.entities.ClazzWorkContentJoin
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer

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


    companion object{
        const val FINDBY_CLAZZ_ASSIGNMENT_UID =
                """
                    SELECT ContentEntry.*, ContentEntryStatus.*, ContentEntryParentChildJoin.*, 
                            Container.*, ContentEntryProgress.*
                      FROM ClazzAssignmentContentJoin
                            LEFT JOIN ContentEntry 
                            ON ContentEntry.contentEntryUid = cacjContentUid 
                            
                            LEFT JOIN ContentEntryProgress 
                            ON ContentEntryProgress.contentEntryProgressContentEntryUid = ContentEntry.contentEntryUid 
                                AND ContentEntryProgress.contentEntryProgressPersonUid = :personUid 
                                AND ContentEntryProgress.contentEntryProgressActive 
                                
                            LEFT JOIN ContentEntryParentChildJoin 
                            ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid 
                            
                            LEFT JOIN ContentEntryStatus 
                            ON ContentEntryStatus.cesUid = ContentEntry.contentEntryUid
                            
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