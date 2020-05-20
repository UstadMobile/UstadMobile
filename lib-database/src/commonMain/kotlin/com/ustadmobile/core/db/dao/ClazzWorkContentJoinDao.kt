package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ClazzWorkContentJoin
import com.ustadmobile.lib.db.entities.ContentEntryWithMetrics

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
