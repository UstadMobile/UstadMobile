package com.ustadmobile.core.db.dao

import com.ustadmobile.door.DoorDataSourceFactory
import androidx.room.*
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.HolidayCalendarWithNumEntries
import com.ustadmobile.lib.db.entities.UserSession

@Repository
@Dao
abstract class  HolidayCalendarDao : BaseDao<HolidayCalendar> {

    @Query("""
     REPLACE INTO HolidayCalendarReplicate(hcPk, hcDestination)
      SELECT DISTINCT HolidayCalendar.umCalendarUid AS hcPk,
             :newNodeId AS hcDestination
        FROM HolidayCalendar
             JOIN UserSession
                  ON UserSession.usClientNodeId = :newNodeId
        --notpsql 
       WHERE HolidayCalendar.umCalendarLct != COALESCE(
             (SELECT hcVersionId
                FROM HolidayCalendarReplicate
               WHERE hcPk = HolidayCalendar.umCalendarUid
                 AND hcDestination = UserSession.usClientNodeId), 0) 
         --endnotpsql        
      /*psql ON CONFLICT(hcPk, hcDestination) DO UPDATE
             SET hcPending = (SELECT HolidayCalendar.umCalendarLct
                                FROM HolidayCalendar
                               WHERE HolidayCalendar.umCalendarUid = EXCLUDED.hcPk ) 
                                     != HolidayCalendarReplicate.hcPk
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([HolidayCalendar::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

 @Query("""
 REPLACE INTO HolidayCalendarReplicate(hcPk, hcDestination)
  SELECT DISTINCT HolidayCalendar.umCalendarUid AS hcUid,
         UserSession.usClientNodeId AS hcDestination
    FROM ChangeLog
         JOIN HolidayCalendar
             ON ChangeLog.chTableId = ${HolidayCalendar.TABLE_ID}
                AND ChangeLog.chEntityPk = HolidayCalendar.umCalendarUid
         JOIN UserSession ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     --notpsql 
      AND HolidayCalendar.umCalendarLct != COALESCE(
             (SELECT hcVersionId
                FROM HolidayCalendarReplicate
               WHERE hcPk = HolidayCalendar.umCalendarUid
                 AND hcDestination = UserSession.usClientNodeId), 0) 
         --endnotpsql    
   /*psql ON CONFLICT(hcPk, hcDestination) DO UPDATE
             SET hcPending = (SELECT HolidayCalendar.umCalendarLct
                                FROM HolidayCalendar
                               WHERE HolidayCalendar.umCalendarUid = EXCLUDED.hcPk ) 
                                     != HolidayCalendarReplicate.hcPk     
        */                                           
    """)
    @ReplicationRunOnChange([HolidayCalendar::class])
    @ReplicationCheckPendingNotificationsFor([HolidayCalendar::class])
    abstract suspend fun replicateOnChange()

    @Query("""SELECT HolidayCalendar.* ,
            (SELECT COUNT(*) FROM Holiday 
               WHERE holHolidayCalendarUid = HolidayCalendar.umCalendarUid 
               AND CAST(holActive AS INTEGER) = 1) AS numEntries 
             FROM HolidayCalendar WHERE CAST(umCalendarActive AS INTEGER) = 1 AND 
             umCalendarCategory = ${HolidayCalendar.CATEGORY_HOLIDAY}""")
    abstract fun findAllHolidaysWithEntriesCount(): DoorDataSourceFactory<Int, HolidayCalendarWithNumEntries>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replaceList(list: List<HolidayCalendar>)

    @Query("SELECT * FROM HolidayCalendar WHERE CAST(umCalendarActive AS INTEGER) = 1 AND umCalendarCategory = "
            + HolidayCalendar.CATEGORY_HOLIDAY)
    abstract fun findAllHolidaysLiveData(): DoorLiveData<List<HolidayCalendar>>

    @Query("SELECT * FROM HolidayCalendar WHERE umCalendarUid = :uid AND CAST(umCalendarActive AS INTEGER) = 1")
    abstract fun findByUidLive(uid: Long): DoorLiveData<HolidayCalendar?>

    @Update
    abstract suspend fun updateAsync(entity: HolidayCalendar):Int

    @Query("SELECT * FROM HolidayCalendar WHERE umCalendarUid = :uid")
    abstract suspend fun findByUid(uid: Long): HolidayCalendar?

    @Query("SELECT * FROM HolidayCalendar WHERE umCalendarUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): HolidayCalendar?

}
