package com.ustadmobile.core.schedule

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.ustadmobile.core.schedule.ClazzLogCreatorManager.Companion.INPUT_CLAZZUID
import com.ustadmobile.core.schedule.ClazzLogCreatorManager.Companion.INPUT_ENDPOINTURL
import com.ustadmobile.core.schedule.ClazzLogCreatorManager.Companion.INPUT_FROMTIME
import com.ustadmobile.core.schedule.ClazzLogCreatorManager.Companion.INPUT_TOTIME
import com.ustadmobile.core.util.ext.setInitialDelayIfLater

class ClazzLogCreatorManagerAndroidImpl(val context: Context): ClazzLogCreatorManager {

    override fun requestClazzLogCreation(clazzUid: Long, endpointUrl: String, fromTime: Long, toTime: Long) {
        val inputData = Data.Builder()
            .putLong(INPUT_CLAZZUID, clazzUid)
            .putString(INPUT_ENDPOINTURL, endpointUrl)
            .putLong(INPUT_FROMTIME, fromTime)
            .putLong(INPUT_TOTIME, toTime)

        val request = OneTimeWorkRequest.Builder(ClazzLogScheduleWorker::class.java)
                .setInitialDelayIfLater(fromTime)
                .setInputData(inputData.build())
                .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "genclazzlog-$endpointUrl-$clazzUid", ExistingWorkPolicy.REPLACE, request)
    }
}