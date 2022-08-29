package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.SyncNode
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Holiday
import com.ustadmobile.lib.db.entities.UserSession

@DoorDao
@Repository
expect abstract class HolidayDao: BaseDao<Holiday>, OneToManyJoinDao<Holiday> {

    @Query("""
     REPLACE INTO HolidayReplicate(holidayPk, holidayDestination)
      SELECT DISTINCT Holiday.holUid AS holidayPk,
             :newNodeId AS holidayDestination
        FROM Holiday
       WHERE Holiday.holLct != COALESCE(
             (SELECT holidayVersionId
                FROM HolidayReplicate
               WHERE holidayPk = Holiday.holUid
                 AND holidayDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(holidayPk, holidayDestination) DO UPDATE
             SET holidayPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([Holiday::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)


    @Query("""
 REPLACE INTO HolidayReplicate(holidayPk, holidayDestination)
  SELECT DISTINCT Holiday.holUid AS holidayUid,
         UserSession.usClientNodeId AS holidayDestination
    FROM ChangeLog
         JOIN Holiday
             ON ChangeLog.chTableId = ${Holiday.TABLE_ID}
                AND ChangeLog.chEntityPk = Holiday.holUid
         JOIN UserSession ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND Holiday.holLct != COALESCE(
         (SELECT holidayVersionId
            FROM HolidayReplicate
           WHERE holidayPk = Holiday.holUid
             AND holidayDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(holidayPk, holidayDestination) DO UPDATE
     SET holidayPending = true
  */               
    """)
    @ReplicationRunOnChange([Holiday::class])
    @ReplicationCheckPendingNotificationsFor([Holiday::class])
    abstract suspend fun replicateOnChange()

    @Query("SELECT * FROM Holiday WHERE holHolidayCalendarUid = :holidayCalendarUid")
    abstract fun findByHolidayCalendaUid(holidayCalendarUid: Long): List<Holiday>

    @Query("SELECT * FROM Holiday WHERE holHolidayCalendarUid = :holidayCalendarUid")
    abstract suspend fun findByHolidayCalendaUidAsync(holidayCalendarUid: Long): List<Holiday>

    @Query("""
        UPDATE Holiday 
           SET holActive = :active, 
               holLct = :changeTime
         WHERE holUid = :holidayUid""")
    abstract fun updateActiveByUid(holidayUid: Long, active: Boolean, changeTime: Long)

    @Insert
    abstract suspend fun updateAsync(entity: Holiday)
}