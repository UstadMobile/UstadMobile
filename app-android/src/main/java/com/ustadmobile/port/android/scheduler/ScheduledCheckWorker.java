package com.ustadmobile.port.android.scheduler;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.scheduler.ScheduledCheckRunner;
import com.ustadmobile.lib.db.entities.ScheduledCheck;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ScheduledCheckWorker extends Worker {

    public static final String ARG_SCHEDULE_CHECK_UID = "uid";

    public ScheduledCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        UmAppDatabase db = UmAppDatabase.getInstance(getApplicationContext());
        UmAppDatabase repo = UmAccountManager.getRepositoryForActiveAccount(getApplicationContext());
        ScheduledCheck check = db.getScheduledCheckDao().findByUid(getInputData().getLong(
                ARG_SCHEDULE_CHECK_UID, 0));
        if(check != null) {
            ScheduledCheckRunner checkRunner = new ScheduledCheckRunner(check, db, repo);
            checkRunner.run();
            return Result.success();
        }else {
            return Result.failure();
        }
    }
}
