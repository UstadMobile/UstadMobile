package com.ustadmobile.core.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ustadmobile.core.account.Endpoint
import org.kodein.di.DI
import org.kodein.di.android.di
import org.kodein.di.instance
import org.kodein.di.on

class NotificationCheckerWorker(appContext: Context, val params: WorkerParameters): CoroutineWorker(appContext, params){

    val di: DI by di(appContext)

    override suspend fun doWork(): Result {
        val siteUrl = inputData.getString(INPUT_SITE_URL) ?: return Result.failure()
        val nsUid = inputData.getLong(INPUT_NOTIFICATION_SETTING_UID, 0)
        val notificationCheckersMgr: NotificationCheckersManager by di.on(Endpoint(siteUrl)).instance()
        notificationCheckersMgr.checkNotificationUids(listOf(nsUid))

        return Result.success()
    }

    companion object {
        const val INPUT_SITE_URL = "siteUrl"
        const val INPUT_NOTIFICATION_SETTING_UID = "nsUid"
    }

}