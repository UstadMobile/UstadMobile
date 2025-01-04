package com.ustadmobile.core.db.dao

import app.cash.paging.PagingSource
import androidx.room.*
import com.ustadmobile.door.DoorQuery
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import kotlinx.coroutines.flow.Flow
import com.ustadmobile.lib.db.entities.Report
import kotlin.js.JsName

@DoorDao
@Repository
expect abstract class ReportDao : BaseDao<Report> {

    @RawQuery
    abstract fun getResults(query: DoorQuery): List<Report>

    @Query("DELETE FROM Report WHERE reportUid = :reportUid")
    abstract suspend fun deleteReportByUid(reportUid: Long)

    @Query("SELECT * FROM Report ORDER BY reportTitle ASC")
    abstract fun findAllReports(): PagingSource<Int, Report>

    @Query("""
        SELECT * FROM Report 
        WHERE reportIsTemplate = :isTemplate
        AND reportTitle LIKE :searchBit
        ORDER BY reportTitle
    """)
    abstract fun findAllActiveReport(
        searchBit: String,
        isTemplate: Boolean
    ): PagingSource<Int, Report>

    @Query("SELECT * FROM Report WHERE reportUid = :entityUid")
    abstract suspend fun findByUid(entityUid: Long): Report?

    @Update
    abstract suspend fun updateAsync(entity: Report)

    @Query("SELECT * FROM Report WHERE reportUid = :uid")
    abstract fun findByUidLive(uid: Long): Flow<Report?>

    @Query("""
        SELECT * FROM Report 
        WHERE reportIsTemplate = :isTemplate
        ORDER BY reportTitle ASC
    """)
    abstract fun findAllActiveReportLive(isTemplate: Boolean): Flow<List<Report>>

    @Query("""
        SELECT * FROM Report 
        WHERE reportIsTemplate = :isTemplate
        ORDER BY reportTitle ASC
    """)
    abstract fun findAllActiveReportList(isTemplate: Boolean): List<Report>

    @JsName("findByUidList")
    @Query("SELECT reportUid FROM Report WHERE reportUid IN (:uidList)")
    abstract fun findByUidList(uidList: List<Long>): List<Long>


    @Query("""
        UPDATE Report 
        SET reportIsTemplate = :toggleVisibility,
            reportLastModTime = :updateTime
        WHERE reportUid IN (:selectedItem)
    """)
    abstract suspend fun toggleVisibilityReportItems(
        toggleVisibility: Boolean,
        selectedItem: List<Long>,
        updateTime: Long
    )


    @JsName("replaceList")
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replaceList(entityList: List<Report>)

}
