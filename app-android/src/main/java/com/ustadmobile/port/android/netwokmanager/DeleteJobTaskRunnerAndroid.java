package com.ustadmobile.port.android.netwokmanager;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.sharedse.networkmanager.DeleteJobTaskRunner;

import java.io.File;
import java.util.Hashtable;
import java.util.List;

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

    DeleteJobTaskRunnerAndroid(Object context, Hashtable args) {
        super(context, args);
    }

    @Override
    public void run() {
        Bundle bundle = UMAndroidUtil.mapToBundle(args);
        Data.Builder requestData = new  Data.Builder();
        if(bundle != null && bundle.getString(ARG_DOWNLOAD_SET_UID) != null){
            requestData.putLong(ARG_DOWNLOAD_SET_UID,Long.parseLong(bundle.getString(ARG_DOWNLOAD_SET_UID)));

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

            for(String destinationPath: destinationFiles){
                File destinationFile = new File(destinationPath);
                if(destinationFile.exists()) destinationFile.delete();
            }

            umAppDatabase.getDownloadSetDao().cleanupUnused(downloadSetUid);

            umAppDatabase.getContentEntryFileStatusDao().deleteByFileUids(umAppDatabase
                    .getDownloadJobItemDao().getContainerUids(downloadSetItemUids));

            return Result.success();
        }
    }

}
