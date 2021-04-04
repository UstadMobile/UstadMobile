package com.ustadmobile.core.notification

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.schedule.duration
import com.ustadmobile.core.schedule.nextOccurence
import com.ustadmobile.core.view.ClazzLogEditAttendanceView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.FeedEntry
import com.ustadmobile.lib.db.entities.NotificationSetting
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on

/**
 * NotificationChecker to generate feedentry(s) for classes where a teacher is due to take attendance.
 */
class TakeAttendanceNotificationChecker(override val di: DI, val siteEndpoint: Endpoint): NotificationChecker, DIAware {

    private val db: UmAppDatabase by di.on(siteEndpoint).instance(tag = DoorTag.TAG_DB)

    override suspend fun checkNotification(notificationSetting: NotificationSetting): NotificationCheckResult {

        val startTime = systemTimeInMillis()

        val clazzesToNotify = db.notificationSettingDao.findTakeAttendanceClazzesToNotify(
            notificationSetting.nsUid, systemTimeInMillis() + 10)

        val feedEntries = clazzesToNotify.map {  clazzLog ->
            FeedEntry().apply {
                fePersonUid = notificationSetting.nsPersonUid
                feTitle = "Take attendance - ${clazzLog.clazz?.clazzName}"
                feViewDest = "${ClazzLogEditAttendanceView.VIEW_NAME}?$ARG_ENTITY_UID=${clazzLog.clazzLogUid}"
                feEntityUid = clazzLog.clazzLogUid
                feNsUid = notificationSetting.nsUid
                feTimestamp = clazzLog.logDate + clazzLog.logDuration
            }
        }

        db.feedEntryDao.insertListAsync(feedEntries)

        //determine the next time to run
        val allSchedules = db.scheduleDao.findAllScheduleByPerson(notificationSetting.nsPersonUid,
            startTime, ClazzEnrolment.ROLE_TEACHER)

        val nextRunTime = allSchedules.mapNotNull {
            val schedule = it.schedule
            val timeZone = it.scheduleTimeZone
            if(schedule != null && timeZone != null){
                schedule.nextOccurence(timeZone, after = (startTime - schedule.duration))
            }else {
                null
            }
        }.map { it.to.unixMillisLong }.minOrNull() ?: 0

        return NotificationCheckResult(nextRunTime)
    }
}