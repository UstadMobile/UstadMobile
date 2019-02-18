package com.ustadmobile.port.android.netwokmanager;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.sharedse.networkmanager.DeleteJobTaskRunner;

import java.io.File;
import java.util.Hashtable;
import java.util.List;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static com.ustadmobile.port.sharedse.controller.DownloadDialogPresenter.ARG_DOWNLOAD_SET_UID;

public class DeleteTaskRunnerAndroid extends DeleteJobTaskRunner {

    private long downloadSetUid = 0L;

    DeleteTaskRunnerAndroid(Object context, Hashtable args) {
        super(context, args);
    }

    @Override
    public void run() {
        Bundle bundle = UMAndroidUtil.mapToBundle(args);
        if(bundle != null && bundle.getString(ARG_DOWNLOAD_SET_UID) != null)
        downloadSetUid = Long.parseLong(bundle.getString(ARG_DOWNLOAD_SET_UID));
        OneTimeWorkRequest deleteWorkRequest =
                new OneTimeWorkRequest.Builder(DeleteTaskWorker.class).build();
        WorkManager.getInstance().enqueue(deleteWorkRequest);
    }

   public class DeleteTaskWorker extends Worker{

        public DeleteTaskWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NonNull
        @Override
        public Result doWork() {

            UmAppDatabase umAppDatabase = UmAppDatabase.getInstance(getApplicationContext());
            List<Long> setItemsUid = umAppDatabase.getDownloadSetItemDao()
                    .findBySetUid(downloadSetUid);
            List<String> destinationFiles = umAppDatabase.getDownloadJobItemDao()
                    .getDestinationFiles(setItemsUid);

            for(String destination: destinationFiles){
                new File(destination).delete();
            }

            umAppDatabase.getDownloadSetDao().cleanupUnused(downloadSetUid);

            return Result.success();
        }
    }
}
