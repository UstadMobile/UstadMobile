package com.ustadmobile.core.db.dao

import com.ustadmobile.door.paging.DataSourceFactory
import androidx.room.*
import com.ustadmobile.core.db.dao.ReportDaoCommon.SORT_TITLE_ASC
import com.ustadmobile.core.db.dao.ReportDaoCommon.SORT_TITLE_DESC
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.DoorQuery
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.UserSession
import kotlin.js.JsName

@DoorDao
@Repository
expect abstract class ReportDao : BaseDao<Report> {

    @Query("""
     REPLACE INTO ReportReplicate(reportPk, reportDestination)
      SELECT DISTINCT Report.reportUid AS reportPk,
             :newNodeId AS reportDestination
        FROM Report
             JOIN UserSession
                  ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
                     AND CAST(Report.isTemplate AS INTEGER) = 1
       WHERE Report.reportLct != COALESCE(
             (SELECT reportVersionId
                FROM ReportReplicate
               WHERE reportPk = Report.reportUid
                 AND reportDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(reportPk, reportDestination) DO UPDATE
             SET reportPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([Report::class])
    abstract suspend fun replicateOnNewNodeTemplates(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO ReportReplicate(reportPk, reportDestination)
  SELECT DISTINCT Report.reportUid AS reportUid,
         UserSession.usClientNodeId AS reportDestination
    FROM ChangeLog
         JOIN Report
              ON ChangeLog.chTableId = ${Report.TABLE_ID} 
                 AND ChangeLog.chEntityPk = Report.reportUid
         JOIN UserSession
              ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
                 AND CAST(Report.isTemplate AS INTEGER) = 1
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND Report.reportLct != COALESCE(
         (SELECT reportVersionId
            FROM ReportReplicate
           WHERE reportPk = Report.reportUid
             AND reportDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(reportPk, reportDestination) DO UPDATE
     SET reportPending = true
  */               
 """)
    @ReplicationRunOnChange([Report::class])
    @ReplicationCheckPendingNotificationsFor([Report::class])
    abstract suspend fun replicateOnChangeTemplates()

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
            : DataSourceFactory<Int, Report>

    @Query("SELECT * FROM Report WHERE reportUid = :entityUid")
    abstract suspend fun findByUid(entityUid: Long): Report?

    @Update
    abstract suspend fun updateAsync(entity: Report)

    @Query("SELECT * From Report WHERE  reportUid = :uid")
    abstract fun findByUidLive(uid: Long): LiveData<Report?>

    @Query("""SELECT * FROM REPORT WHERE NOT reportInactive 
        AND isTemplate = :isTemplate
        ORDER BY priority ASC
            """)
    abstract fun findAllActiveReportLive(isTemplate: Boolean)
            : LiveData<List<Report>>

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