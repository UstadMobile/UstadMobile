package com.ustadmobile.port.android.netwokmanager;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.DownloadSet;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.sharedse.networkmanager.DeleteJobTaskRunner;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static com.ustadmobile.port.sharedse.controller.DownloadDialogPresenter.ARG_DOWNLOAD_SET_UID;

/**
 * Android implementation of {@link DeleteJobTaskRunner} which delete DownloadSet, DownloadSetItem,
 * DownloadJob and DownloadJobItems
 *
 * <b>Note: Operation Flow</b>
 * <p>
 * - Once {@link DeleteJobTaskRunner#run()} is called, it create a work request to delete all
 * DownloadSet, DownloadSetItem, DownloadJob and DownloadJobItems associated with download set Uid
 * passed as an argument
 * @author kileha3
 *
 * @see DeleteJobTaskRunner
 */

public class DeleteJobTaskRunnerAndroid extends DeleteJobTaskRunner {

    public static final String TAG = "DeleteJobTaskWorker";

    DeleteJobTaskRunnerAndroid(Object context, HashMap<String , String> args) {
        super(context, args);
    }

    @Override
    public void run() {

        Data.Builder requestData = new  Data.Builder();
        if(args != null && args.get(ARG_DOWNLOAD_SET_UID) != null){
            requestData.putLong(ARG_DOWNLOAD_SET_UID,
                    Long.parseLong(Objects.requireNonNull(args.get(ARG_DOWNLOAD_SET_UID))));

        }
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(DeleteJobTaskWorker.class)
                .addTag(TAG)
                .setInputData(requestData.build())
                .build();
        WorkManager.getInstance().enqueue(request);
    }


    /**
     * Worker class which execute delete task
     */
    public static class DeleteJobTaskWorker extends Worker {

        private long downloadSetUid;

        public DeleteJobTaskWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
            downloadSetUid = workerParams.getInputData().getLong(ARG_DOWNLOAD_SET_UID,0L);
        }

        @NonNull
        @Override
        public Result doWork() {

            UmAppDatabase umAppDatabase = UmAppDatabase.getInstance(getApplicationContext());

            List<Long> downloadSetItemUids = umAppDatabase.getDownloadSetItemDao()
                    .findBySetUid(downloadSetUid);

            List<String> destinationFiles = umAppDatabase.getDownloadJobItemDao()
                    .getDestinationFiles(downloadSetItemUids);

            DownloadSet downloadSet = umAppDatabase.getDownloadSetDao()
                    .findByUid((int) downloadSetUid);
            if(downloadSet == null)
                return Result.success();

            umAppDatabase.getContentEntryStatusDao()
                    .deleteByFileUids(downloadSet.getDsRootContentEntryUid());

            for(String destinationPath: destinationFiles){
                File destinationFile = new File(destinationPath);
                if(destinationFile.exists()) destinationFile.delete();
            }

            umAppDatabase.getDownloadSetDao().cleanupUnused(downloadSetUid);

            if(downloadSetItemUids.isEmpty())
                return Result.success();

            return Result.success();
        }
    }

}
