package com.ustadmobile.core.notification

import com.ustadmobile.core.notification.NotificationCheckersManager.Companion.INPUT_NOTIFICATION_SETTING_UID
import com.ustadmobile.core.notification.NotificationCheckersManager.Companion.INPUT_SITE_URL
import com.ustadmobile.core.util.ext.startNowOrAt
import org.kodein.di.direct
import org.kodein.di.instance
import org.quartz.JobBuilder.newJob
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey

actual fun NotificationCheckersManager.scheduleCheck(context: Any, notificationSettingUid: Long,
                                                     runTime: Long) {
    val scheduler: Scheduler = di.direct.instance()
    val job = newJob(NotificationCheckerJob::class.java)
        .usingJobData(INPUT_NOTIFICATION_SETTING_UID, notificationSettingUid)
        .usingJobData(INPUT_SITE_URL, endpoint.url)
        .build()

    val triggerKey = TriggerKey("nscheck-$endpoint:$notificationSettingUid")
    scheduler.unscheduleJob(triggerKey)

    val trigger = TriggerBuilder.newTrigger()
        .withIdentity(triggerKey)
        .startNowOrAt(runTime)
        .build()

    scheduler.scheduleJob(job, trigger)
}
