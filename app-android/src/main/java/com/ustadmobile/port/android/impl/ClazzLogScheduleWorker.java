package com.ustadmobile.port.android.impl;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * This class is used to schedule a task that runs at midnight every day to create the ClazzLog
 * items for the following day
 */
public class ClazzLogScheduleWorker extends Worker {

    public static String TAG = "ClazzLogSchedule";

    public static void queueClazzLogScheduleWorker(long time) {
        OneTimeWorkRequest request= new OneTimeWorkRequest.Builder(ClazzLogScheduleWorker.class)
                .setInitialDelay(time - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addTag(TAG)
                .build();
        WorkManager.getInstance().enqueue(request);
    }

    /**
     * Determine when we should next generate ClazzLog items for any classes of the active user.
     * This is at exactly midnight.
     *
     * @return
     */
    public static long getNextClazzLogScheduleDueTime() {
        Calendar nextTimeCal = Calendar.getInstance();
        nextTimeCal.setTimeInMillis(System.currentTimeMillis() + (1000 * 60 * 60 * 24));
        nextTimeCal.set(Calendar.HOUR_OF_DAY, 0);
        nextTimeCal.set(Calendar.MINUTE, 0);
        nextTimeCal.set(Calendar.SECOND, 0);
        nextTimeCal.set(Calendar.MILLISECOND, 0);
        return nextTimeCal.getTimeInMillis();
    }

    public ClazzLogScheduleWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        UmAppDatabase dbRepo = UmAccountManager.getRepositoryForActiveAccount(getApplicationContext());
        //Create ClazzLogs for Today (call SchduledDao ) -
        // -> loop over clazzes and schedules and create ClazzLogs
        dbRepo.getScheduleDao().createClazzLogsForToday(
                UmAccountManager.getActivePersonUid(getApplicationContext()), dbRepo);
        //Queue next worker at 00:00
        queueClazzLogScheduleWorker(getNextClazzLogScheduleDueTime());
        UstadMobileSystemImpl.getInstance().scheduleChecks(getApplicationContext());
        return Result.success();
    }
}
