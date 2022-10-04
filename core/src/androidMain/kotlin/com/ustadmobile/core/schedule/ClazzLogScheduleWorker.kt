package com.ustadmobile.core.schedule

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.schedule.ClazzLogCreatorManager.Companion.DAY_IN_MS
import com.ustadmobile.core.schedule.ClazzLogCreatorManager.Companion.INPUT_CLAZZUID
import com.ustadmobile.core.schedule.ClazzLogCreatorManager.Companion.INPUT_ENDPOINTURL
import com.ustadmobile.core.schedule.ClazzLogCreatorManager.Companion.INPUT_FROMTIME
import com.ustadmobile.core.schedule.ClazzLogCreatorManager.Companion.INPUT_TOTIME
import com.ustadmobile.door.ext.DoorTag
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import com.ustadmobile.core.db.UmAppDatabase

class ClazzLogScheduleWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    val di: DI by closestDI(context)

    override fun doWork(): Result {
        val endpoint = inputData.getString(INPUT_ENDPOINTURL) ?: return Result.failure()
        val repo: UmAppDatabase by di.on(Endpoint(endpoint)).instance(tag = DoorTag.Companion.TAG_REPO)
        val fromTime = inputData.getLong(INPUT_FROMTIME, Long.MAX_VALUE)
        val toTime = inputData.getLong(INPUT_TOTIME, 0L)
        val clazzUid = inputData.getLong(INPUT_CLAZZUID, 0)
        val nextRunTime = repo.createClazzLogs(fromTime, toTime, clazzUid)

        val clazzLogCreatorManager: ClazzLogCreatorManager = di.direct.instance()

        /* TODO: When merging the Feed branch, this will return the next run time
        See https://github.com/UstadMobile/UstadMobile/blob/888a46edadbbc21668f6420d881999235906a389/core/src/androidMain/kotlin/com/ustadmobile/core/schedule/ClazzLogScheduleWorker.kt
        if(nextRunTime > 0)
            clazzLogCreatorManager.requestClazzLogCreation(clazzUid, endpoint, nextRunTime,
                (nextRunTime + DAY_IN_MS) - 1)
         */

        return Result.success()
    }

}