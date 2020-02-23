package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ClazzAssignmentContentJoin
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithMetrics

@UmDao
@UmRepository
@Dao
abstract class ClazzAssignmentContentJoinDao : BaseDao<ClazzAssignmentContentJoin> {

    @Query("SELECT * FROM ClazzAssignmentContentJoin " +
            "WHERE clazzAssignmentContentJoinUid = :clazzAssignmentContentJoinUid " +
            "AND CAST(clazzAssignmentContentJoinInactive AS INTEGER) = 0")
    abstract suspend fun findByUidAsync(clazzAssignmentContentJoinUid: Long): ClazzAssignmentContentJoin?

    @Query("""UPDATE ClazzAssignmentContentJoin SET clazzAssignmentContentJoinInactive = 1 
        WHERE clazzAssignmentContentJoinUid = :clazzAssignmentContentJoinUid
    """)
    abstract suspend fun deactivateJoin(clazzAssignmentContentJoinUid : Long ): Int

    @Query("""SELECT * FROM ClazzAssignmentContentJoin 
        WHERE clazzAssignmentContentJoinClazzAssignmentUid = :clazzAssignmentUid 
        AND CAST(clazzAssignmentContentJoinInactive AS INTEGER) = 0""")
    abstract fun findJoinsByAssignmentUid(clazzAssignmentUid: Long)
            : DataSource.Factory<Int, ClazzAssignmentContentJoin>

    @Query("""SELECT ContentEntry.*, 0.0 as contentEntryWithMetricsProgress FROM ClazzAssignmentContentJoin 
        LEFT JOIN ContentEntry ON ContentEntry.contentEntryUid = clazzAssignmentContentJoinContentUid
        WHERE clazzAssignmentContentJoinClazzAssignmentUid = :clazzAssignmentUid 
        AND CAST(clazzAssignmentContentJoinInactive AS INTEGER) = 0""")
    abstract fun findContentByAssignmentUid(clazzAssignmentUid: Long)
            : DataSource.Factory<Int, ContentEntryWithMetrics>

    @Update
    abstract suspend fun updateAsync(entity: ClazzAssignmentContentJoin): Int

}
