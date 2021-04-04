package com.ustadmobile.core.notification

import com.ustadmobile.lib.db.entities.NotificationSetting

/**
 * A notification checker runs a check on a given NotificationSetting type (e.g.
 * TakeAttendance, AttendanceNotTaken, ClazzAttendanceThreshold etc). The NotificationSetting
 * contains the preferences of a specific user for a specific type of notification.
 *
 * It should generate FeedEntry(s) based on the notification setting for the given user (personUid).
 * It MUST check for previously generated FeedEntry items and it should not create duplicates. It can
 * be called multiple times e.g. when its inputs have been changed (class schedule updated etc).
 */
interface NotificationChecker {

    /**
     * Check the notification given by the UID.
     *
     * @param notificationSetting The NotificationSetting (nsUid) to check
     *
     * @return NotificationResult specifying at least the time that this should next be checked (or
     * 0 if there is no specific check time).
     */
    suspend fun checkNotification(notificationSetting: NotificationSetting): NotificationCheckResult

}