package com.ustadmobile.core.notification

import android.content.Context
import androidx.work.*
import com.ustadmobile.core.util.ext.setInitialDelayIfLater
import com.ustadmobile.door.util.systemTimeInMillis
import java.util.concurrent.TimeUnit

actual fun NotificationCheckersManager.scheduleCheck(context: Any, notificationSettingUid: Long,
                                                     runTime: Long) {

    val workRequest: OneTimeWorkRequest = OneTimeWorkRequestBuilder<NotificationCheckerWorker>()
        .setInputData(Data.Builder()
            .putLong(NotificationCheckerWorker.INPUT_NOTIFICATION_SETTING_UID, notificationSettingUid)
            .putString(NotificationCheckerWorker.INPUT_SITE_URL, endpoint.url)
            .build())
        .setInitialDelayIfLater(runTime)
        .build()
    val workName = "nscheck-$endpoint:$notificationSettingUid"
    WorkManager.getInstance(context as Context).enqueueUniqueWork(workName,
        ExistingWorkPolicy.REPLACE, workRequest)
}
