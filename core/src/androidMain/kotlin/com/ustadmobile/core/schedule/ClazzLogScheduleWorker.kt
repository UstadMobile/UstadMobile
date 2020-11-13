package com.ustadmobile.core.schedule

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import org.kodein.di.DI
import org.kodein.di.android.di
import org.kodein.di.instance
import org.kodein.di.on

class ClazzLogScheduleWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    val di: DI by di(context)

    override fun doWork(): Result {
        val endpoint = inputData.getString(INPUT_ENDPOINTURL) ?: return Result.failure()
        val repo: UmAppDatabase by di.on(Endpoint(endpoint)).instance(tag = TAG_REPO)
        val fromTime = inputData.getLong(INPUT_FROMTIME, Long.MAX_VALUE)
        val toTime = inputData.getLong(INPUT_TOTIME, 0L)
        val clazzUidFilter = inputData.getLong(INPUT_CLAZZUIDFILTER, 0)
        val matchLocalFromDay = inputData.getBoolean(INPUT_MATCH_LOCAL_FROM_DAY, false)
        repo.createClazzLogs(fromTime, toTime, clazzUidFilter, matchLocalFromDay)

        return Result.success()
    }

    companion object {

        const val INPUT_ENDPOINTURL = "dbName"

        const val INPUT_FROMTIME = "fromTime"

        const val INPUT_TOTIME = "toTime"

        const val INPUT_CLAZZUIDFILTER = "clazzUidFilter"

        const val INPUT_MATCH_LOCAL_FROM_DAY = "matchLocalFromDay"

    }
}