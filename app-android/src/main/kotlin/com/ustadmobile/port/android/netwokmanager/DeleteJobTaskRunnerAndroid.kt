package com.ustadmobile.port.android.netwokmanager

import android.content.Context
import androidx.work.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.port.sharedse.networkmanager.DeleteJobTaskRunner
import java.util.*

/**
 * Android implementation of [DeleteJobTaskRunner] which delete DownloadSet, DownloadSetItem,
 * DownloadJob and DownloadJobItems
 *
 * **Note: Operation Flow**
 *
 *
 * - Once [DeleteJobTaskRunner.run] is called, it create a work request to delete all
 * DownloadSet, DownloadSetItem, DownloadJob and DownloadJobItems associated with download set Uid
 * passed as an argument
 * @author kileha3
 *
 * @see DeleteJobTaskRunner
 */

class DeleteJobTaskRunnerAndroid internal constructor(context: Any?, args: Map<String, String>) : DeleteJobTaskRunner(context, args) {

    override fun run() {

        val requestData = Data.Builder()
        if (args != null && args[DeleteJobTaskRunner.ARG_DOWNLOAD_JOB_UID] != null) {
            requestData.putLong(DeleteJobTaskRunner.ARG_DOWNLOAD_JOB_UID,
                    java.lang.Long.parseLong(Objects.requireNonNull<String>(args[DeleteJobTaskRunner.ARG_DOWNLOAD_JOB_UID])))

        }
        val request = OneTimeWorkRequest.Builder(DeleteJobTaskWorker::class.java)
                .addTag(TAG)
                .setInputData(requestData.build())
                .build()
        WorkManager.getInstance().enqueue(request)
    }


    /**
     * Worker class which execute delete task
     */
    class DeleteJobTaskWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

        private val downloadSetUid: Long

        init {
            downloadSetUid = workerParams.inputData.getLong(DeleteJobTaskRunner.ARG_DOWNLOAD_JOB_UID, 0L)
        }

        override fun doWork(): ListenableWorker.Result {

            val umAppDatabase = UmAppDatabase.getInstance(applicationContext)

            /*

            TODO: fix this to use downloadjob instead

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
                */

            return ListenableWorker.Result.success()
        }
    }

    companion object {

        val TAG = "DeleteJobTaskWorker"
    }

}
