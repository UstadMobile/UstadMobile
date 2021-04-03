package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzLogWithClazz
import com.ustadmobile.lib.db.entities.NotificationSetting
import com.ustadmobile.lib.db.entities.NotificationSettingWithLastChecked

@Dao
@Repository
abstract class NotificationSettingDao : BaseDao<NotificationSetting> {

    @Query("""
        SELECT NotificationSetting.*, NotificationSettingLastChecked.*
          FROM NotificationSetting
     LEFT JOIN NotificationSettingLastChecked ON NotificationSettingLastChecked.nslcNsUid = NotificationSetting.nsUid
         WHERE nsUid = :uid
         LIMIT 1
    """)
    abstract suspend fun findByUidWithLastCheckedAsync(uid: Long): NotificationSettingWithLastChecked?


    @Query("""
        SELECT ClazzLog.*, Clazz.*
          FROM ClazzLog
          JOIN Clazz ON Clazz.clazzUid = ClazzLog.clazzLogClazzUid
          JOIN ClazzEnrolment ON ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid 
               AND ClazzEnrolment.clazzEnrolmentPersonUid = (
                    SELECT nsPersonUid
                      FROM NotificationSetting
                     WHERE nsUid = :notificationSettingUid)
               AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_TEACHER}
               AND :toTime BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined 
                    AND ClazzEnrolment.clazzEnrolmentDateLeft
         WHERE ClazzLog.logDate BETWEEN 
                (SELECT COALESCE((
                        SELECT lastCheckTime
                          FROM NotificationSettingLastChecked
                         WHERE nslcUid = :notificationSettingUid), 0))
                AND :toTime
           AND NOT EXISTS (
                SELECT FeedEntry.feUid
                  FROM FeedEntry
                 WHERE FeedEntry.feNsUid = :notificationSettingUid
                   AND FeedEntry.feEntityUid = ClazzLog.clazzLogUid)
    """)
    abstract suspend fun findTakeAttendanceClazzesToNotify(notificationSettingUid: Long, toTime: Long): List<ClazzLogWithClazz>

}