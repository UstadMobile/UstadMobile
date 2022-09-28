package com.ustadmobile.core.db.dao

import com.ustadmobile.door.paging.DataSourceFactory
import androidx.room.*
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.UserSession


@Repository
@DoorDao
expect abstract class ScheduleDao : BaseDao<Schedule>, OneToManyJoinDao<Schedule> {

    @Query("""
     REPLACE INTO ScheduleReplicate(schedulePk, scheduleDestination)
      SELECT DISTINCT Schedule.scheduleUid AS schedulePk,
             :newNodeId AS scheduleDestination
        FROM UserSession
              JOIN PersonGroupMember
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
              ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_CLAZZ_SELECT}
                    ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}
              JOIN Schedule
                   ON Schedule.scheduleClazzUid = Clazz.clazzUid
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND Schedule.scheduleLastChangedTime != COALESCE(
             (SELECT scheduleVersionId
                FROM ScheduleReplicate
               WHERE schedulePk = Schedule.scheduleUid
                 AND scheduleDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(schedulePk, scheduleDestination) DO UPDATE
             SET schedulePending = true
      */       
 """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([Schedule::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

 @Query("""
 REPLACE INTO ScheduleReplicate(schedulePk, scheduleDestination)
  SELECT DISTINCT Schedule.scheduleUid AS scheduleUid,
         UserSession.usClientNodeId AS scheduleDestination
    FROM ChangeLog
         JOIN Schedule
              ON ChangeLog.chTableId = ${Schedule.TABLE_ID}
                 AND Schedule.scheduleUid = ChangeLog.chEntityPk
         JOIN Clazz
              ON Clazz.clazzUid = Schedule.scheduleClazzUid
         ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_CLAZZ_SELECT}
              ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND Schedule.scheduleLastChangedTime != COALESCE(
         (SELECT scheduleVersionId
            FROM ScheduleReplicate
           WHERE schedulePk = Schedule.scheduleUid
             AND scheduleDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(schedulePk, scheduleDestination) DO UPDATE
     SET schedulePending = true
  */               
    """)
    @ReplicationRunOnChange([Schedule::class])
    @ReplicationCheckPendingNotificationsFor([Schedule::class])
    abstract suspend fun replicateOnChange()

    @Insert
    abstract override fun insert(entity: Schedule): Long

    @Update
    abstract suspend fun updateAsync(entity: Schedule) : Int


    @Query("""
        UPDATE Schedule 
           SET scheduleActive = :active,
               scheduleLastChangedTime = :changeTime
         WHERE scheduleUid = :scheduleUid
            """)
    abstract suspend fun updateScheduleActivated(
        scheduleUid: Long,
        active: Boolean,
        changeTime: Long
    )

    @Query("SELECT * FROM Schedule WHERE scheduleUid = :uid")
    abstract fun findByUid(uid: Long): Schedule?

    @Query("SELECT * FROM Schedule WHERE scheduleUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : Schedule?

    @Query("SELECT * FROM Schedule WHERE scheduleClazzUid = :clazzUid AND CAST(scheduleActive AS INTEGER) = 1 ")
    abstract fun findAllSchedulesByClazzUid(clazzUid: Long): DataSourceFactory<Int, Schedule>

    @Query("SELECT * FROM Schedule WHERE scheduleClazzUid = :clazzUid AND CAST(scheduleActive AS INTEGER) = 1")
    abstract fun findAllSchedulesByClazzUidAsList(clazzUid: Long): List<Schedule>

    //Used for testing ClazzEdit
    @Query("SELECT * FROM Schedule WHERE scheduleClazzUid = :clazzUid AND CAST(scheduleActive AS INTEGER) = 1")
    abstract fun findAllSchedulesByClazzUidAsLiveList(clazzUid: Long): LiveData<List<Schedule>>

    @Query("SELECT * FROM Schedule WHERE scheduleClazzUid = :clazzUid AND CAST(scheduleActive AS INTEGER) = 1 ")
    abstract suspend fun findAllSchedulesByClazzUidAsync(clazzUid: Long): List<Schedule>

}
