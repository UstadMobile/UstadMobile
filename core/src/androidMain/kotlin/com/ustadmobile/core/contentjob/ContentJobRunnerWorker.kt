package com.ustadmobile.core.contentjob

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.work.*
import com.ustadmobile.core.R
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.RateLimitedLiveData
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.lang.IllegalStateException

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

        val notification = createNotification()

        val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

        GlobalScope.launch(Dispatchers.Main) {

            try {

                val liveData = RateLimitedLiveData<ContentJobItem?>(db, listOf("ContentJobItem"), 1000) {
                    db.contentJobItemDao.findByJobId(jobId)
                }
                val jobObserver = DoorObserver<ContentJobItem?> {
                    notification.setProgress(100, it?.cjiItemProgress?.toInt() ?: 0, false)
                    if(it?.cjiItemProgress?.toInt() == 100){
                        notification.setContentTitle("Download Complete")
                    }
                    setForegroundAsync(ForegroundInfo(jobId.toInt(), notification.build()))
                }

                launch(Dispatchers.Main) {
                    liveData.observeForever(jobObserver)
                }
            }catch (e: Exception){
                println(e.message)
            }
        }

        setForeground(ForegroundInfo(jobId.toInt(), notification.build()))

        val jobResult =  ContentJobRunner(jobId, endpoint, di).runJob().toWorkerResult()

        return jobResult
    }

    override fun isRunInForeground(): Boolean {
        return true
    }

    private fun createNotification(): NotificationCompat.Builder {

        val intent = WorkManager.getInstance(applicationContext)
                .createCancelPendingIntent(id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        return NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Downloading")
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
                NOTIFICATION_CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH)

        mNotificationChannel.vibrationPattern = longArrayOf(0)
        mNotificationChannel.enableVibration(false)
        mNotificationChannel.setSound(null, null)

        notificationManager.createNotificationChannel(mNotificationChannel)
    }

    companion object {

        const val NOTIFICATION_CHANNEL_ID = "UM_NOTIFICATION_CHANNEL_ID"

    }


}