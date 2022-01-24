package com.ustadmobile.core.contentjob

import android.content.Context
import androidx.work.*
import com.ustadmobile.core.account.Endpoint

class ContentJobManagerAndroid(val appContext: Context): ContentJobManager {

    override fun enqueueContentJob(endpoint: Endpoint, contentJobUid: Long) {
        val inputData = Data.Builder()
            .putString(ContentJobManager.KEY_ENDPOINT, endpoint.url)
            .putLong(ContentJobManager.KEY_CONTENTJOB_UID, contentJobUid)
            .build()

        val request = OneTimeWorkRequest.Builder(ContentJobRunnerWorker::class.java)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(appContext)
            .enqueueUniqueWork("contentjob-${endpoint.url}-$contentJobUid",
                ExistingWorkPolicy.REPLACE, request)

    }

    override fun cancelContentJob(endpoint: Endpoint, contentJobUid: Long) {
        WorkManager.getInstance(appContext).cancelUniqueWork(
                "contentjob-${endpoint.url}-$contentJobUid")
    }

}