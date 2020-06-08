package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import com.ustadmobile.door.DoorQuery
import com.ustadmobile.door.SimpleDoorQuery
import com.ustadmobile.door.annotation.ParamName
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.ReportWithFilters
import com.ustadmobile.lib.db.entities.XapiReportOptions

@Dao
@UmRepository
abstract class ReportDao : BaseDao<Report> {

    @RawQuery
    abstract fun getResults(query: DoorQuery): List<Report>

    open suspend fun getResultsFromOptions(@ParamName("options") options: XapiReportOptions): List<Report> {
        val sql = options.toSql()
        return getResults(SimpleDoorQuery(sql.sqlStr, sql.queryParams))
    }

    @Query("SELECT * FROM REPORT WHERE NOT reportInactive AND reportOwnerUid = :loggedInPersonUid  ORDER BY reportTitle ASC")
    abstract fun findAllActiveReportByUserAsc(loggedInPersonUid: Long): DataSource.Factory<Int, Report>

    @Query("SELECT * FROM REPORT WHERE NOT reportInactive AND reportOwnerUid = :loggedInPersonUid ORDER BY reportTitle DESC")
    abstract fun findAllActiveReportByUserDesc(loggedInPersonUid: Long): DataSource.Factory<Int, Report>

    @Query("SELECT * FROM Report WHERE reportUid = :entityUid")
    abstract fun findByUid(entityUid: Long): Report?

    @Update
    abstract fun updateAsync(entity: Report)


}