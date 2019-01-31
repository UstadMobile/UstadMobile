package com.ustadmobile.port.android.sync;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.lib.db.entities.UmAccount;
import com.ustadmobile.port.android.netwokmanager.UmAppDatabaseSyncService;

import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class UmAppDatabaseSyncWorker extends Worker {

    public static final String TAG = "UmAppDbSync";

    public UmAppDatabaseSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void queueSyncWorker(long delay, TimeUnit timeUnit) {
        Constraints workConstraint = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(UmAppDatabaseSyncWorker.class)
                .setInitialDelay(delay, timeUnit)
                .addTag(TAG)
                .setConstraints(workConstraint)
                .build();
        WorkManager.getInstance().enqueue(request);
    }


    @NonNull
    @Override
    public Result doWork() {
        UmAccount activeAccount = UmAccountManager.getActiveAccount(getApplicationContext());

        UmAppDatabase umAppDb = UmAppDatabase.getInstance(getApplicationContext());
        try {
            umAppDb.syncWith(
                    UmAccountManager.getRepositoryForActiveAccount(getApplicationContext()),
                    activeAccount != null ? activeAccount.getPersonUid() : 0, 100, 100);
            UstadMobileSystemImpl.l(UMLog.INFO, 100, "database syncWith repo ran");
        }catch(Exception e) {
            UstadMobileSystemImpl.l(UMLog.WARN, 101, "Exception running syncWith :" +
                    e.getMessage());
        }

        if(!isStopped()) {
             boolean appRecentlyActive = UmAppDatabaseSyncService.isInForeground()
                    || System.currentTimeMillis() - UmAppDatabaseSyncService.getLastForegroundTime()
                    < UmAppDatabaseSyncService.SYNC_AFTER_BACKGROUND_LAG;

             if(appRecentlyActive ||
                     umAppDb.countPendingLocalChanges(UmAccountManager.getActivePersonUid(
                             getApplicationContext()), umAppDb.getDeviceBits()) > 0) {
                 queueSyncWorker(appRecentlyActive ? 1 : 15, TimeUnit.MINUTES);
             }
        }


        return Result.success();
    }
}
