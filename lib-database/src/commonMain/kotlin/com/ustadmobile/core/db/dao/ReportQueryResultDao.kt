package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.PostgresQuery
import com.ustadmobile.lib.db.entities.ReportQueryResult

@DoorDao
expect abstract class ReportQueryResultDao {

    @Query("""
        DELETE FROM ReportQueryResult
         WHERE rqrReportUid = :reportUid
           AND rqrTimeZone = :timeZone
    """)
    abstract suspend fun deleteByReportUidAndTimeZone(reportUid: Long, timeZone: String)

    @Insert
    abstract suspend fun insertAllAsync(results: List<ReportQueryResult>)


    @Query("""
        SELECT ReportQueryResult.*
          FROM ReportQueryResult
         WHERE ReportQueryResult.rqrReportUid = :reportUid 
           AND ReportQueryResult.rqrTimeZone = :timeZone
    """)
    abstract suspend fun getAllByReportUidAndTimeZone(
        reportUid: Long,
        timeZone: String
    ): List<ReportQueryResult>

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
                   AND ReportQueryResult.rqrTimeZone = :timeZone
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
                   AND ReportQueryResult.rqrTimeZone = :timeZone
                 LIMIT 1), 0) >= 
               (SELECT GREATEST(:freshThresholdTime, 
                            (SELECT COALESCE(
                                    (SELECT Report.reportLastModTime
                                       FROM Report
                                      WHERE Report.reportUid = :reportUid), 0))))
    """)
    abstract suspend fun isReportFresh(
        reportUid: Long,
        timeZone: String,
        freshThresholdTime: Long,
    ): Boolean

}