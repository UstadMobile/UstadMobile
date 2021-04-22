package com.ustadmobile.core.notification

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.NotificationSetting
import com.ustadmobile.lib.db.entities.NotificationSettingLastChecked
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on

class NotificationCheckersManager(val endpoint: Endpoint, override val di: DI,
                                  internal val context: Any,
                                  private val notificationCheckers: Map<Int, NotificationChecker> =
                              defaultNotificationCheckers(di, endpoint)): DIAware {

    private val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    private val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

    /**
     * Check the given notification. See what type of notification it is, and use the appropriate
     * checker to generate feedentry(s) etc.
     */
    suspend fun checkNotificationUids(notificationPrefUids: List<Long>) {
        checkNotifications(db.notificationSettingDao.findByUidsList(notificationPrefUids))
    }

    suspend fun checkNotifications(notifications: List<NotificationSetting>) {
        notifications.forEach { notificationSetting ->
            notificationCheckers.get(notificationSetting.nsType)?.also {
                val runTime = systemTimeInMillis()
                val nextRunTime = it.checkNotification(notificationSetting)
                db.notificationSettingLastCheckedDao.replaceAsync(
                    NotificationSettingLastChecked().apply {
                    nslcNsUid = notificationSetting.nsUid
                    lastCheckTime = runTime
                })

                if(nextRunTime.nextCheckTime > 0)
                    scheduleCheck(context, notificationSetting.nsUid, nextRunTime.nextCheckTime)
            }
        }
    }

    /**
     * Function to be called when a class schedule has been changed.
     */
    suspend fun invalidateClazzScheduleRelatedCheckers(clazzUid: Long) {
        //find all notifications that relate to this class
        val affectedNotifications = db.notificationSettingDao
            .findTakeAttendanceNotificationSettingsByClazzUid(clazzUid)
        checkNotifications(affectedNotifications)
    }


    companion object {

        const val INPUT_SITE_URL = "siteUrl"

        const val INPUT_NOTIFICATION_SETTING_UID = "nsUid"

        private fun defaultNotificationCheckers(di: DI, endpoint: Endpoint) = mapOf(
            NotificationSetting.TYPE_TAKE_ATTENDANCE_REMINDER to TakeAttendanceNotificationChecker(di, endpoint)
        )

    }

}