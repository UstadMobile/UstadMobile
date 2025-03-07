package com.ustadmobile.core.db.dao

import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.PostgresQuery
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

    /**
     * Determine if a previous report run is fresh (as the term is used in caching). This checks
     * that a) the results were generated after the report was last modified AND b) the result was
     * generated after the freshThresholdTime (eg does not exceed max age).
     *
     * @param reportUid reportUid
     * @param freshThresholdTime minimum timestamp for ReportQueryResult to be considered fresh
     */
    @Query("""
        SELECT COALESCE(
               (SELECT ReportQueryResult.rqrLastModified
                  FROM ReportQueryResult
                 WHERE ReportQueryResult.rqrReportUid = :reportUid
                 LIMIT 1), 0) >= 
               (SELECT MAX(:freshThresholdTime, 
                            (SELECT COALESCE(
                                    (SELECT Report.reportLastModTime
                                       FROM Report
                                      WHERE Report.reportUid = :reportUid), 0))))
    """,)
    @PostgresQuery("""
        SELECT COALESCE(
               (SELECT ReportQueryResult.rqrLastModified
                  FROM ReportQueryResult
                 WHERE ReportQueryResult.rqrReportUid = :reportUid
                 LIMIT 1), 0) >= 
               (SELECT GREATEST(:freshThresholdTime, 
                            (SELECT COALESCE(
                                    (SELECT Report.reportLastModTime
                                       FROM Report
                                      WHERE Report.reportUid = :reportUid), 0))))
    """)
    abstract suspend fun isReportFresh(
        reportUid: Long,
        freshThresholdTime: Long,
    ): Boolean

}