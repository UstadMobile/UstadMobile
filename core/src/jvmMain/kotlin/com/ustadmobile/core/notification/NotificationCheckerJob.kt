package com.ustadmobile.core.notification

import com.ustadmobile.core.account.Endpoint
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import org.quartz.Job
import org.quartz.JobExecutionContext

class NotificationCheckerJob: Job {

    override fun execute(context: JobExecutionContext) {
        val di = context.scheduler.context.get("di") as DI
        val jobDataMap = context.jobDetail.jobDataMap
        val nsUid = jobDataMap.getLong(NotificationCheckersManager.INPUT_NOTIFICATION_SETTING_UID)
        val siteUrl = jobDataMap.getString(NotificationCheckersManager.INPUT_SITE_URL)
        val notificationCheckersMgr: NotificationCheckersManager by di.on(Endpoint(siteUrl)).instance()
        runBlocking {
            notificationCheckersMgr.checkNotificationUids(listOf(nsUid))
        }
    }
}