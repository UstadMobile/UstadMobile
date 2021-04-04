package com.ustadmobile.core.schedule

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.ustadmobile.door.util.systemTimeInMillis
import java.util.concurrent.TimeUnit

class ClazzLogCreatorManagerAndroidImpl(val context: Context): ClazzLogCreatorManager {

    override fun requestClazzLogCreation(clazzUid: Long, endpointUrl: String, fromTime: Long, toTime: Long) {
        val inputData = Data.Builder()
                .putLong(ClazzLogScheduleWorker.INPUT_CLAZZUID, clazzUid)
                .putString(ClazzLogScheduleWorker.INPUT_ENDPOINTURL, endpointUrl)
                .putLong(ClazzLogScheduleWorker.INPUT_FROMTIME, fromTime)
                .putLong(ClazzLogScheduleWorker.INPUT_TOTIME, toTime)

        val request = OneTimeWorkRequest.Builder(ClazzLogScheduleWorker::class.java)
                .apply {
                    val timeUntilStart = fromTime - systemTimeInMillis()
                    if(timeUntilStart > 0)
                        setInitialDelay(timeUntilStart, TimeUnit.MILLISECONDS)
                }
                .setInputData(inputData.build())
                .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "genclazzlog-$endpointUrl-$clazzUid", ExistingWorkPolicy.REPLACE, request)
    }
}