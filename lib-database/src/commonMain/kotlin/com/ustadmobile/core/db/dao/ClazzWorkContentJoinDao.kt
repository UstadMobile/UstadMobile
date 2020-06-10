package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ClazzWorkContentJoin
import com.ustadmobile.lib.db.entities.ContentEntryWithMetrics
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer

@UmDao
@UmRepository
@Dao
abstract class ClazzWorkContentJoinDao : BaseDao<ClazzWorkContentJoin> {

    @Query("SELECT * FROM ClazzWorkContentJoin " +
            "WHERE clazzWorkContentJoinUid = :clazzWorkContentJoinUid " +
            "AND CAST(clazzWorkContentJoinInactive AS INTEGER) = 0")
    abstract suspend fun findByUidAsync(clazzWorkContentJoinUid: Long)
            : ClazzWorkContentJoin?

    @Query("""UPDATE ClazzWorkContentJoin SET clazzWorkContentJoinInactive = 1 
        WHERE clazzWorkContentJoinUid = :clazzWorkContentJoinUid
    """)
    abstract suspend fun deactivateJoin(clazzWorkContentJoinUid : Long ): Int

    @Query("""SELECT ContentEntry.* ,
         (
            SELECT MAX(extensionProgress) FROM StatementEntity
            LEFT JOIN XObjectEntity ON XObjectEntity.xObjectUid = StatementEntity.xObjectUid
            WHERE XObjectEntity.objectContentEntryUid = clazzWorkContentJoinContentUid
            AND timestamp BETWEEN :fromDate AND :endDate
        ) as contentEntryWithMetricsProgress
        FROM ClazzWorkContentJoin
        LEFT JOIN ContentEntry ON ContentEntry.contentEntryUid = clazzWorkContentJoinContentUid
        WHERE clazzWorkContentJoinClazzWorkUid = :clazzWorkUid
        AND CAST(clazzWorkContentJoinInactive AS INTEGER) = 0""")
    abstract fun findContentByClazzWorkUid(clazzWorkUid: Long, fromDate: Long, endDate: Long)
            : List <ContentEntryWithMetrics>

    @Query("""SELECT ContentEntry.*, ContentEntryStatus.*, ContentEntryParentChildJoin.*, Container.*
            FROM ClazzWorkContentJoin
            LEFT JOIN ContentEntry ON ContentEntry.contentEntryUid = clazzWorkContentJoinContentUid
            LEFT JOIN ContentEntryParentChildJoin ON 
                ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid 
            LEFT JOIN ContentEntryStatus ON ContentEntryStatus.cesUid = ContentEntry.contentEntryUid
            LEFT JOIN Container ON Container.containerUid = (SELECT containerUid FROM Container 
                WHERE containerContentEntryUid =  ContentEntry.contentEntryUid ORDER BY cntLastModified DESC LIMIT 1)
            WHERE 
            ClazzWorkContentJoin.clazzWorkContentJoinClazzWorkUid = :clazzWorkUid
            AND CAST(clazzWorkContentJoinInactive AS INTEGER) = 0
            AND NOT ContentEntry.ceInactive
            AND (ContentEntry.publik OR :personUid != 0)
            ORDER BY ContentEntry.title ASC , 
                    ContentEntryParentChildJoin.childIndex, ContentEntry.contentEntryUid""")
    abstract fun findAllContentByClazzWorkUid(clazzWorkUid: Long, personUid : Long)
            :List <ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>


    @Query("""SELECT ContentEntry.* ,
         (
            SELECT MAX(extensionProgress) FROM StatementEntity
            LEFT JOIN XObjectEntity ON XObjectEntity.xObjectUid = StatementEntity.xObjectUid
            WHERE XObjectEntity.objectContentEntryUid = clazzWorkContentJoinContentUid
            AND timestamp BETWEEN :fromDate AND :endDate
        ) as contentEntryWithMetricsProgress
        FROM ClazzWorkContentJoin
        LEFT JOIN ContentEntry ON ContentEntry.contentEntryUid = clazzWorkContentJoinContentUid
        WHERE clazzWorkContentJoinClazzWorkUid = :clazzWorkUid
        AND CAST(clazzWorkContentJoinInactive AS INTEGER) = 0""")
    abstract fun findContentByClazzWorkUidLive(clazzWorkUid: Long, fromDate: Long, endDate: Long)
            : DataSource.Factory<Int, ContentEntryWithMetrics>


}
