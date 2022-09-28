package com.ustadmobile.core.contentjob

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ustadmobile.core.R
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.RateLimitedLiveData
import com.ustadmobile.core.util.ext.toStatusString
import com.ustadmobile.door.lifecycle.Observer
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import org.kodein.di.on

class ContentJobRunnerWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    val di: DI by closestDI(context)

    private val systemImpl: UstadMobileSystemImpl by di.instance()

    private val notificationManager = getSystemService(applicationContext, NotificationManager::class.java) as NotificationManager

    private fun ContentJobRunner.ContentJobResult.toWorkerResult(): Result {
        return when(this.status) {
            JobStatus.FAILED -> Result.failure()
            JobStatus.COMPLETE -> Result.success()
            else -> Result.retry()
        }
    }

    override suspend fun doWork(): Result {
        val endpointStr = inputData.getString(ContentJobManager.KEY_ENDPOINT)
            ?: throw IllegalStateException("No endpoint")
        val endpoint = Endpoint(endpointStr)

        val jobId = inputData.getLong(ContentJobManager.KEY_CONTENTJOB_UID, 0)

        val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

        val job = db.contentJobDao.findByUid(jobId) ?: throw IllegalStateException("No job found")

        val notification = createNotification(job)

        val liveData = RateLimitedLiveData(db, listOf("ContentJobItem"), 1000) {
                 db.contentJobItemDao.findRootJobItemByJobId(jobId)
        }

        val jobObserver = Observer<ContentJobItem?> {
            when(it?.cjiRecursiveStatus){
                JobStatus.COMPLETE, JobStatus.FAILED, JobStatus.PARTIAL_FAILED -> {
                    notification.setProgress(100, 100, false)
                }
                else ->{
                    notification.setProgress(it?.cjiRecursiveTotal?.toInt() ?: 100,
                            it?.cjiRecursiveProgress?.toInt() ?: 0,
                            false)
                }
            }
            notification.setContentText(it.toStatusString(systemImpl, applicationContext))
            setForegroundAsync(ForegroundInfo(jobId.toInt(), notification.build()))
        }

        try {

            withContext(doorMainDispatcher()) {
                liveData.observeForever(jobObserver)
            }

            setForeground(ForegroundInfo(jobId.toInt(), notification.build()))

            return ContentJobRunner(jobId, endpoint, di).runJob().toWorkerResult()
        } catch(c: CancellationException) {
            return Result.failure()
        }finally {
            withContext(doorMainDispatcher()) {
                liveData.removeObserver(jobObserver)
            }
        }
    }

    override fun isRunInForeground(): Boolean {
        return true
    }

    private fun createNotification(job: ContentJob): NotificationCompat.Builder {

        val intent = WorkManager.getInstance(applicationContext)
                .createCancelPendingIntent(id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        return NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(job.cjNotificationTitle)
                .setOngoing(true)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_file_download_white_24dp)
                .addAction(android.R.drawable.ic_delete,
                        systemImpl.getString(MessageID.cancel, applicationContext),
                        intent)
    }




    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val mNotificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_ID, NotificationManager.IMPORTANCE_LOW)

        mNotificationChannel.vibrationPattern = longArrayOf(0)
        mNotificationChannel.enableVibration(false)
        mNotificationChannel.setSound(null, null)

        notificationManager.createNotificationChannel(mNotificationChannel)
    }

    companion object {

        const val NOTIFICATION_CHANNEL_ID = "UM_NOTIFICATION_CHANNEL_ID"

    }


}