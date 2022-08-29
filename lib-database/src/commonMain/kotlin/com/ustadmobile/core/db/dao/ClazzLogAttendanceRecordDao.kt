package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.SyncNode
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*

@DoorDao
@Repository
expect abstract class ClazzLogAttendanceRecordDao : BaseDao<ClazzLogAttendanceRecord> {


    @Query("""
     REPLACE INTO ClazzLogAttendanceRecordReplicate(clarPk, clarDestination)
      SELECT DISTINCT ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid AS clarUid,
             :newNodeId AS clarDestination
        FROM UserSession
             JOIN PersonGroupMember 
                  ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                  ${Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT} 
                  ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}
             JOIN ClazzLog
                  ON ClazzLog.clazzLogClazzUid = Clazz.clazzUid
             JOIN ClazzLogAttendanceRecord 
                  ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid
       WHERE ClazzLogAttendanceRecord.clazzLogAttendanceRecordLastChangedTime != COALESCE(
             (SELECT clarVersionId
                FROM ClazzLogAttendanceRecordReplicate
               WHERE clarPk = ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid
                 AND clarDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(clarPk, clarDestination) DO UPDATE
             SET clarPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([ClazzLogAttendanceRecord::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO ClazzLogAttendanceRecordReplicate(clarPk, clarDestination)
  SELECT DISTINCT ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid AS clarUid,
         UserSession.usClientNodeId AS clarDestination
    FROM ChangeLog
         JOIN ClazzLogAttendanceRecord 
              ON ChangeLog.chTableId = ${ClazzLogAttendanceRecord.TABLE_ID} 
             AND ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid = ChangeLog.chEntityPk
         JOIN ClazzLog
              ON ClazzLog.clazzLogUid = ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid
         JOIN Clazz 
              ON Clazz.clazzUid = ClazzLog.clazzLogClazzUid 
         ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_CLAZZ_SELECT}
              ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND ClazzLogAttendanceRecord.clazzLogAttendanceRecordLastChangedTime != COALESCE(
             (SELECT clarVersionId
                FROM ClazzLogAttendanceRecordReplicate
               WHERE clarPk = ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid
                 AND clarDestination = UserSession.usClientNodeId), 0) 
 /*psql ON CONFLICT(clarPk, clarDestination) DO UPDATE
     SET clarPending = true
  */               
    """)
    @ReplicationRunOnChange([ClazzLogAttendanceRecord::class])
    @ReplicationCheckPendingNotificationsFor([ClazzLogAttendanceRecord::class])
    abstract suspend fun replicateOnChange()

    @Insert
    abstract suspend fun insertListAsync(entities: List<ClazzLogAttendanceRecord>)

    @Query("SELECT * from ClazzLogAttendanceRecord WHERE clazzLogAttendanceRecordUid = :uid")
    abstract fun findByUid(uid: Long): ClazzLogAttendanceRecord?

    @Update
    abstract suspend fun updateListAsync(entities: List<ClazzLogAttendanceRecord>)


    @Query("""SELECT ClazzLogAttendanceRecord.*, Person.*
         FROM ClazzLogAttendanceRecord 
         LEFT JOIN Person ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordPersonUid = Person.personUid
         WHERE clazzLogAttendanceRecordClazzLogUid = :clazzLogUid""")
    abstract suspend fun findByClazzLogUid(clazzLogUid: Long): List<ClazzLogAttendanceRecordWithPerson>

    @Query("""
        UPDATE ClazzLogAttendanceRecord
           SET clazzLogAttendanceRecordClazzLogUid = :newClazzLogUid,
               clazzLogAttendanceRecordLastChangedTime = :changedTime
        WHERE clazzLogAttendanceRecordClazzLogUid = :oldClazzLogUid
    """)
    abstract fun updateRescheduledClazzLogUids(
        oldClazzLogUid: Long,
        newClazzLogUid: Long,
        changedTime: Long
    )

}
