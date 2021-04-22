package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.NotificationSetting.Companion.TYPE_TAKE_ATTENDANCE_REMINDER

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
        SELECT NotificationSetting.*, NotificationSettingLastChecked.*
          FROM NotificationSetting
     LEFT JOIN NotificationSettingLastChecked ON NotificationSettingLastChecked.nslcNsUid = NotificationSetting.nsUid
         WHERE nsUid IN (:uidList)
    """)
    abstract suspend fun findByUidsList(uidList: List<Long>): List<NotificationSettingWithLastChecked>

    /**
     * Find a list of classes for which take attendance notifications need to be generated according
     * to a given NotificationSetting
     */
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
         WHERE (ClazzLog.logDate + ClazzLog.logDuration) BETWEEN 
                (SELECT COALESCE((
                        SELECT lastCheckTime
                          FROM NotificationSettingLastChecked
                         WHERE nslcNsUid = :notificationSettingUid), 0))
                AND :toTime
           AND NOT EXISTS (
                SELECT FeedEntry.feUid
                  FROM FeedEntry
                 WHERE FeedEntry.feNsUid = :notificationSettingUid
                   AND FeedEntry.feEntityUid = ClazzLog.clazzLogUid)
    """)
    abstract suspend fun findTakeAttendanceClazzesToNotify(notificationSettingUid: Long, toTime: Long): List<ClazzLogWithClazz>

    @Query("""
        SELECT NotificationSetting.* 
          FROM NotificationSetting
          JOIN ClazzEnrolment ON ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid
               AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_TEACHER}
               AND ClazzEnrolment.clazzEnrolmentPersonUid = NotificationSetting.nsPersonUid
         WHERE NotificationSetting.nsType = $TYPE_TAKE_ATTENDANCE_REMINDER
    """)
    abstract suspend fun findTakeAttendanceNotificationSettingsByClazzUid(clazzUid: Long): List<NotificationSetting>

}