package com.ustadmobile.core.db.dao

import app.cash.paging.PagingSource
import androidx.room.*
import com.ustadmobile.core.db.dao.ReportDaoCommon.SORT_TITLE_ASC
import com.ustadmobile.core.db.dao.ReportDaoCommon.SORT_TITLE_DESC
import kotlinx.coroutines.flow.Flow
import com.ustadmobile.door.DoorQuery
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Report
import kotlin.js.JsName

@DoorDao
@Repository
expect abstract class ReportDao : BaseDao<Report> {

    @RawQuery
    abstract fun getResults(query: DoorQuery): List<Report>

    @Query("""SELECT * FROM REPORT WHERE NOT reportInactive 
        AND reportOwnerUid = :personUid
        AND isTemplate = :isTemplate
        AND reportTitle LIKE :searchBit
        ORDER BY priority, CASE(:sortOrder)
            WHEN $SORT_TITLE_ASC THEN Report.reportTitle
            ELSE ''
        END ASC,
        CASE(:sortOrder)
            WHEN $SORT_TITLE_DESC THEN Report.reportTitle
            ELSE ''
        END DESC
            """)
    abstract fun findAllActiveReport(searchBit: String, personUid: Long, sortOrder: Int,
                                     isTemplate: Boolean)
            : PagingSource<Int, Report>

    @Query("SELECT * FROM Report WHERE reportUid = :entityUid")
    abstract suspend fun findByUid(entityUid: Long): Report?

    @Update
    abstract suspend fun updateAsync(entity: Report)

    @Query("SELECT * From Report WHERE  reportUid = :uid")
    abstract fun findByUidLive(uid: Long): Flow<Report?>

    @Query("""SELECT * FROM REPORT WHERE NOT reportInactive 
        AND isTemplate = :isTemplate
        ORDER BY priority ASC
            """)
    abstract fun findAllActiveReportLive(isTemplate: Boolean)
            : Flow<List<Report>>

    @Query("""SELECT * FROM REPORT WHERE NOT reportInactive 
        AND isTemplate = :isTemplate
        ORDER BY priority ASC
            """)
    abstract fun findAllActiveReportList(isTemplate: Boolean): List<Report>

    @JsName("findByUidList")
    @Query("SELECT reportUid FROM Report WHERE reportUid IN (:uidList)")
    abstract fun findByUidList(uidList: List<Long>): List<Long>


    @Query("""
        UPDATE Report 
           SET reportInactive = :toggleVisibility,
               reportLct = :updateTime 
         WHERE reportUid IN (:selectedItem)
    """)
    abstract suspend fun toggleVisibilityReportItems(
        toggleVisibility: Boolean,
        selectedItem: List<Long>,
        updateTime: Long,
    )


    @JsName("replaceList")
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replaceList(entityList: List<Report>)


}