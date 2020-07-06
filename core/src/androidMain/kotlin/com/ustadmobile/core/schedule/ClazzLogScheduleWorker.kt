package com.ustadmobile.core.schedule

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ustadmobile.core.impl.UmAccountManager

class ClazzLogScheduleWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val db = UmAccountManager.getRepositoryForActiveAccount(super.getApplicationContext())
        val fromTime = inputData.getLong(INPUT_FROMTIME, Long.MAX_VALUE)
        val toTime = inputData.getLong(INPUT_TOTIME, 0L)
        val clazzUidFilter = inputData.getLong(INPUT_CLAZZUIDFILTER, 0)
        val matchLocalFromDay = inputData.getBoolean(INPUT_MATCH_LOCAL_FROM_DAY, false)
        db.createClazzLogs(fromTime, toTime, clazzUidFilter, matchLocalFromDay)

        return Result.success()
    }

    companion object {

        const val INPUT_DBNAME = "dbName"

        const val INPUT_FROMTIME = "fromTime"

        const val INPUT_TOTIME = "toTime"

        const val INPUT_CLAZZUIDFILTER = "clazzUidFilter"

        const val INPUT_MATCH_LOCAL_FROM_DAY = "matchLocalFromDay"

    }
}