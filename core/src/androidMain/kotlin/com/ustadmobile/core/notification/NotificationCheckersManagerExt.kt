package com.ustadmobile.core.notification

import android.content.Context
import androidx.work.*
import com.ustadmobile.core.notification.NotificationCheckersManager.Companion.INPUT_NOTIFICATION_SETTING_UID
import com.ustadmobile.core.notification.NotificationCheckersManager.Companion.INPUT_SITE_URL
import com.ustadmobile.core.util.ext.setInitialDelayIfLater

actual fun NotificationCheckersManager.scheduleCheck(context: Any, notificationSettingUid: Long,
                                                     runTime: Long) {

    val workRequest: OneTimeWorkRequest = OneTimeWorkRequestBuilder<NotificationCheckerWorker>()
        .setInputData(Data.Builder()
            .putLong(INPUT_NOTIFICATION_SETTING_UID, notificationSettingUid)
            .putString(INPUT_SITE_URL, endpoint.url)
            .build())
        .setInitialDelayIfLater(runTime)
        .build()
    val workName = "nscheck-$endpoint:$notificationSettingUid"
    WorkManager.getInstance(context as Context).enqueueUniqueWork(workName,
        ExistingWorkPolicy.REPLACE, workRequest)
}
