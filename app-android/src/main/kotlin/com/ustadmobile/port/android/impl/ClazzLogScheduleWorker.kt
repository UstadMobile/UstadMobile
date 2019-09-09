package com.ustadmobile.port.android.impl

import android.content.Context
import androidx.work.*
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * This class is used to schedule a task that runs at midnight every day to create the ClazzLog
 * items for the following day
 */
class ClazzLogScheduleWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): ListenableWorker.Result {
        val dbRepo = UmAccountManager.getRepositoryForActiveAccount(applicationContext)
        //Create ClazzLogs for Today (call SchduledDao ) -
        // -> loop over clazzes and schedules and create ClazzLogs
        dbRepo.scheduleDao.createClazzLogsForToday(
                UmAccountManager.getActivePersonUid(applicationContext), dbRepo)
        //Queue next worker at 00:00
        queueClazzLogScheduleWorker(getNextClazzLogScheduleDueTime())
        UstadMobileSystemImpl.instance.scheduleChecks(applicationContext)
        return ListenableWorker.Result.success()
    }


    companion object {

        var TAG = "ClazzLogSchedule"

        fun queueClazzLogScheduleWorker(time: Long) {
            val request = OneTimeWorkRequest.Builder(ClazzLogScheduleWorker::class.java)
                    .setInitialDelay(time - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .addTag(TAG)
                    .build()
            WorkManager.getInstance().enqueue(request)
        }

        /**
         * Determine when we should next generate ClazzLog items for any classes of the active user.
         * This is at exactly midnight.
         *
         * @return
         */
        fun getNextClazzLogScheduleDueTime(): Long {
            val nextTimeCal = Calendar.getInstance()
            nextTimeCal.timeInMillis = System.currentTimeMillis() + 1000 * 60 * 60 * 24
            nextTimeCal.set(Calendar.HOUR_OF_DAY, 0)
            nextTimeCal.set(Calendar.MINUTE, 0)
            nextTimeCal.set(Calendar.SECOND, 0)
            nextTimeCal.set(Calendar.MILLISECOND, 0)
            return nextTimeCal.timeInMillis
        }


    }
}
