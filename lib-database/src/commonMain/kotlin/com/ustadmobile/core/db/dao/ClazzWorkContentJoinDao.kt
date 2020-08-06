package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ClazzWorkContentJoin
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer

@UmDao
@UmRepository
@Dao
abstract class ClazzWorkContentJoinDao : BaseDao<ClazzWorkContentJoin>, OneToManyJoinDao<ClazzWorkContentJoin> {

    @Query("SELECT * FROM ClazzWorkContentJoin " +
            "WHERE clazzWorkContentJoinUid = :clazzWorkContentJoinUid " +
            "AND CAST(clazzWorkContentJoinInactive AS INTEGER) = 0")
    abstract suspend fun findByUidAsync(clazzWorkContentJoinUid: Long) : ClazzWorkContentJoin?

    @Query("""UPDATE ClazzWorkContentJoin SET clazzWorkContentJoinInactive = 1 
        WHERE clazzWorkContentJoinUid = :clazzWorkContentJoinUid
    """)
    abstract suspend fun deactivateJoin(clazzWorkContentJoinUid : Long ): Int


    @Query(FINDBY_CLAZZWORK_UID)
    abstract fun findAllContentByClazzWorkUid(clazzWorkUid: Long, personUid : Long)
            :List <ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>

    @Query(FINDBY_CLAZZWORK_UID)
    abstract fun findAllContentByClazzWorkUidDF(clazzWorkUid: Long, personUid : Long)
            :DataSource.Factory<Int, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>

    @Query("UPDATE ClazzWorkContentJoin SET clazzWorkContentJoinInactive = :active " +
            "WHERE clazzWorkContentJoinUid = :uid ")
    abstract suspend fun updateInActiveByClazzWorkQuestionUid(uid: Long, active : Boolean)


    override suspend fun deactivateByUids(uidList: List<Long>) {
        uidList.forEach {
            updateInActiveByClazzWorkQuestionUid(it, true)
        }
    }

    companion object{
        const val FINDBY_CLAZZWORK_UID =
            """SELECT ContentEntry.*, ContentEntryStatus.*, ContentEntryParentChildJoin.*, 
            Container.*, ContentEntryProgress.*
            FROM ClazzWorkContentJoin
            LEFT JOIN ContentEntry ON ContentEntry.contentEntryUid = clazzWorkContentJoinContentUid 
            LEFT JOIN ContentEntryProgress ON ContentEntryProgress.contentEntryProgressContentEntryUid = ContentEntry.contentEntryUid 
                 AND ContentEntryProgress.contentEntryProgressPersonUid = :personUid AND ContentEntryProgress.contentEntryProgressActive 
            LEFT JOIN ContentEntryParentChildJoin ON 
                ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid 
            LEFT JOIN ContentEntryStatus ON ContentEntryStatus.cesUid = ContentEntry.contentEntryUid
            LEFT JOIN Container ON Container.containerUid = (SELECT containerUid FROM Container 
                WHERE containerContentEntryUid =  ContentEntry.contentEntryUid 
                ORDER BY cntLastModified DESC LIMIT 1)
            WHERE 
                ClazzWorkContentJoin.clazzWorkContentJoinClazzWorkUid = :clazzWorkUid
                AND CAST(clazzWorkContentJoinInactive AS INTEGER) = 0
                AND NOT ContentEntry.ceInactive
                AND (ContentEntry.publik OR :personUid != 0)
            ORDER BY ContentEntry.title ASC , 
                    ContentEntryParentChildJoin.childIndex, ContentEntry.contentEntryUid"""
    }

}
