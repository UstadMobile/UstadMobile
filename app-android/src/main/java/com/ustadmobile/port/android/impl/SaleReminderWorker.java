package com.ustadmobile.port.android.impl;

import android.content.Context;
import android.support.annotation.NonNull;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class SaleReminderWorker extends Worker {

    int days;
    long saleDueDate;

    public static String TAG = "SaleReminderWorker";

    public static void queueSaleReminder(long time) {
        OneTimeWorkRequest request= new OneTimeWorkRequest.Builder(SaleReminderWorker.class)
                .setInitialDelay(time - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addTag(TAG)
                .build();
        WorkManager.getInstance().enqueue(request);
    }


    public static long getNextMidnightReminder(int days, long saleDueDate){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(saleDueDate);
        cal.add(Calendar.DATE, -days);
        return cal.getTimeInMillis();
    }

    @NonNull
    @Override
    public Result doWork() {
        UmAppDatabase dbRepo = UmAccountManager.getRepositoryForActiveAccount(getApplicationContext());
        //Queue next worker
        queueSaleReminder(getNextMidnightReminder(days, saleDueDate));
        return Result.success();
    }

    public SaleReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public SaleReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams,
                              int daysbefore, long saleDate) {
        super(context, workerParams);
        days = daysbefore;
        saleDueDate = saleDate;
    }


}
