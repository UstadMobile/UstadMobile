package com.ustadmobile.core.db.dao

import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.lib.db.entities.ReportQueryResult

@DoorDao
abstract class ReportQueryResultDao {

    @Query("""
        DELETE FROM ReportQueryResult
         WHERE rqrReportUid = :reportUid
    """)
    abstract suspend fun deleteByReportUid(reportUid: Long)

    @Query("""
        SELECT ReportQueryResult.*
          FROM ReportQueryResult
         WHERE ReportQueryResult.rqrReportUid = :reportUid 
    """)
    abstract suspend fun getAllByReportUid(reportUid: Long): List<ReportQueryResult>

}