package com.ustadmobile.core.notification

/**
 * Schedule a check using the underlying platform (e.g. JVM Quartz / Android WorkManager)
 * for a particular notificationsetting
 *
 */
expect fun NotificationCheckersManager.scheduleCheck(context: Any, notificationSettingUid: Long,
    runTime: Long)
