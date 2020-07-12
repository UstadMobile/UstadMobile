package com.ustadmobile.core.schedule

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager

class ClazzLogCreatorManagerAndroidImpl(val context: Context): ClazzLogCreatorManager {

    override fun requestClazzLogCreation(clazzUidFilter: Long, endpointUrl: String, fromTime: Long, toTime: Long) {
        val inputData = Data.Builder()
                .putLong(ClazzLogScheduleWorker.INPUT_CLAZZUIDFILTER, clazzUidFilter)
                .putString(ClazzLogScheduleWorker.INPUT_ENDPOINTURL, endpointUrl)
                .putLong(ClazzLogScheduleWorker.INPUT_FROMTIME, fromTime)
                .putLong(ClazzLogScheduleWorker.INPUT_TOTIME, toTime)
                .putBoolean(ClazzLogScheduleWorker.INPUT_MATCH_LOCAL_FROM_DAY, false)

        val request = OneTimeWorkRequest.Builder(ClazzLogScheduleWorker::class.java)
                .setInputData(inputData.build())
                .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}