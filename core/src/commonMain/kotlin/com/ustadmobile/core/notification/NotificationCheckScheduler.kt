package com.ustadmobile.core.notification

interface NotificationCheckScheduler {

    /**
     * Schedule running a check. This is either a ClazzLog check (to check for creating ClazzLogs
     * for a particular Clazz) that will call createClazzLogs or a NotificationPref check that will
     * call the NotificationChecker. The UID of the entity to check (e.g. clazzUid or
     * notificationPrefUid) should be in the taskParams parameter.
     *
     *
     */
    fun scheduleNotificationCheck(checkTime: Long, notificationUid: Long)

}