package com.ustadmobile.sharedse.network

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.GROUP_ALERT_SUMMARY
import androidx.core.app.NotificationManagerCompat
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.lib.util.copyOnWriteListOf
import com.ustadmobile.port.sharedse.R
import java.util.concurrent.atomic.AtomicInteger
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.lib.db.entities.DownloadJob
import kotlinx.coroutines.*
import org.kodein.di.*
import org.kodein.di.android.di
import java.lang.IllegalArgumentException

/**
 * This services monitors the download job statuses and act accordingly
 */
class DownloadNotificationService : Service(), DIAware {

    override val di: DI by di()

    private lateinit var mNotificationManager: NotificationManagerCompat

    private val notificationIdRef = AtomicInteger(9)

    private val impl: UstadMobileSystemImpl by instance()

    private var stopped = false

    private var foregroundActive: Boolean = false

    private var summaryNotificationHolder: SummaryNotificationHolder? = null

    private val activeDownloadJobNotifications: MutableList<DownloadJobNotificationHolder> = copyOnWriteListOf()

    private val activeDeleteJobNotifications: MutableList<DeleteNotificationHolder> = copyOnWriteListOf()

    open inner class NotificationHolder2(var contentTitle: String, var contentText: String,
                                         val notificationId: Int = notificationIdRef.incrementAndGet()) {

        val builder: NotificationCompat.Builder

        init {
            builder = createNotificationBuilder()
        }

        /**
         * Setup the notificationcompat.builde rwith common options required for all notifications we are using
         */
        private fun createNotificationBuilder(): NotificationCompat.Builder {
            val intent = Intent()
            val mNotificationPendingIntent = PendingIntent.getActivity(
                    this@DownloadNotificationService, 0, intent, 0)
            val builder = NotificationCompat.Builder(this@DownloadNotificationService,
                    NOTIFICATION_CHANNEL_ID)
            builder.setPriority(NotificationCompat.PRIORITY_LOW)
                    .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                    .setWhen(System.currentTimeMillis())
                    //TODO: set the color
                    //.setColor(ContextCompat.getColor(this, R.color.primary))
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true)
                    .setContentIntent(mNotificationPendingIntent)
                    .setDefaults(Notification.DEFAULT_SOUND)

            if (canCreateGroupedNotification()) {
                builder.setGroupAlertBehavior(GROUP_ALERT_SUMMARY)
                        .setGroup(NOTIFICATION_GROUP_KEY)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setSmallIcon(R.drawable.ic_file_download_white_24dp)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            }

            return builder
        }


        internal fun build(): Notification {
            val notification = builder.build()
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                notification.defaults = 0
                notification.sound = null
            }

            return notification
        }

        internal fun doNotify() {
            UMLog.l(UMLog.DEBUG, 0, "DownloadNotification: holder $this sending notification: ")
            mNotificationManager.notify(notificationId, build())

        }

        /**
         * Create action buttons
         * @param downloadJobId Identifies the job on which buttons will appear
         * @param actionTag action tags for the prnding intent
         * @param actionLabel button label text
         * @return constructed action button
         */
        internal fun createAction(downloadJobId: Int, actionTag: String,
                                  actionLabel: String): NotificationCompat.Action {
            val actionIntent = Intent(this@DownloadNotificationService,
                    DownloadNotificationService::class.java)
            actionIntent.putExtra(EXTRA_DOWNLOADJOBUID, downloadJobId)
            actionIntent.action = actionTag
            val actionPendingIntent = PendingIntent.getService(this@DownloadNotificationService,
                    0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            return NotificationCompat.Action(0, actionLabel, actionPendingIntent)
        }
    }

    inner class DownloadJobNotificationHolder(val downloadJobUid: Int, endpoint: Endpoint,
                                              notifyAfterInit: Boolean = true) : NotificationHolder2(
            impl.getString(MessageID.loading, applicationContext),
            impl.getString(MessageID.waiting, applicationContext)), DoorObserver<DownloadJob?> {

        var bytesSoFar: Long = 0
        var totalBytes: Long = 0

        lateinit var downloadJobLiveData: DoorLiveData<DownloadJob?>

        init {
            builder.setProgress(MAX_PROGRESS_VALUE, 0, false)
                    .addAction(createAction(downloadJobUid,
                            ACTION_CANCEL_DOWNLOAD, impl.getString(MessageID.download_cancel_label,
                            applicationContext)))
                    .addAction(createAction(downloadJobUid,
                            ACTION_PAUSE_DOWNLOAD, impl.getString(MessageID.download_pause_download,
                            applicationContext)))
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)

            GlobalScope.launch(Dispatchers.Main) {
                val db: UmAppDatabase = on(endpoint).direct.instance(tag = TAG_DB)
                val downloadJobTitleInDb = db.downloadJobDao.getEntryTitleByJobUidAsync(downloadJobUid)
                        ?: ""
                builder.setContentTitle(downloadJobTitleInDb)
                contentTitle = downloadJobTitleInDb
                if (notifyAfterInit)
                    doNotify()

                val containerDownloadManager: ContainerDownloadManager = di.on(endpoint).direct.instance()
                downloadJobLiveData = containerDownloadManager.getDownloadJob(downloadJobUid)
                downloadJobLiveData.observeForever(this@DownloadJobNotificationHolder)
            }
        }

        override fun onChanged(t: DownloadJob?) {
            if(t != null) {
                bytesSoFar = t.bytesDownloadedSoFar
                totalBytes = t.totalBytesToDownload


                val progress = (bytesSoFar.toDouble() / totalBytes * 100).toInt()
                builder.setProgress(MAX_PROGRESS_VALUE, progress, false)
                contentText = String.format(impl.getString(
                        MessageID.download_downloading_placeholder, this@DownloadNotificationService),
                        UMFileUtil.formatFileSize(bytesSoFar),
                        UMFileUtil.formatFileSize(totalBytes))
                builder.setContentText(contentText)

                doNotify()
                summaryNotificationHolder?.updateSummary()

                if(t.djStatus >= JobStatus.COMPLETE_MIN) {
                    activeDownloadJobNotifications.remove(this)
                    mNotificationManager.cancel(notificationId)
                    downloadJobLiveData.removeObserver(this)
                    checkIfCompleteAfterDelay()
                }
            }
        }
    }

    inner class DeleteNotificationHolder(val downloadJobUid: Int, val endpoint: Endpoint) : NotificationHolder2(impl.getString(MessageID.deleting, applicationContext), impl.getString(MessageID.deleting, applicationContext)) {
        init {
            builder.setContentTitle(contentTitle)
                    .setContentText(contentText)

            GlobalScope.launch {
                val db: UmAppDatabase = on(endpoint).direct.instance(tag = TAG_DB)
                val downloadJobTitleInDb = db.downloadJobDao.getEntryTitleByJobUidAsync(downloadJobUid) ?: ""
                builder.setContentTitle(downloadJobTitleInDb)
                contentTitle = downloadJobTitleInDb
                doNotify()

            }
        }
    }

    inner class SummaryNotificationHolder() : NotificationHolder2(
            impl.getString(MessageID.downloading, applicationContext),
            impl.getString(MessageID.downloading, applicationContext)) {
        init {
            val inboxStyle = NotificationCompat.InboxStyle()
                    .setBigContentTitle(contentTitle)
                    .setSummaryText(contentText)
            builder.setGroupSummary(true)
                    .setStyle(inboxStyle)
        }

        fun updateSummary() {
            val totalBytes = activeDownloadJobNotifications.fold(0L, { count, jobNotification ->
                count + jobNotification.totalBytes
            })
            val bytesSoFar = activeDownloadJobNotifications.fold(0L, { count, jobNotification ->
                count + jobNotification.bytesSoFar
            })


            contentTitle = String.format(impl.getString(MessageID.download_summary_title,
                    applicationContext), activeDownloadJobNotifications.size)

            val summaryLabel = impl.getString(MessageID.download_downloading_placeholder,
                    applicationContext)
            contentText = String.format(summaryLabel,
                    UMFileUtil.formatFileSize(bytesSoFar),
                    UMFileUtil.formatFileSize(totalBytes))


            builder.setStyle(NotificationCompat.InboxStyle()
                    .setBigContentTitle(contentTitle)
                    .setSummaryText(contentText)
                    .also { inboxStyle ->
                        activeDownloadJobNotifications.forEach {
                            inboxStyle.addLine("${it.contentTitle} - ${it.contentText}")
                        }
                    })

            doNotify()
        }

    }

    fun checkIfCompleteAfterDelay(){
        GlobalScope.launch {
            delay(2000)
            if (isEmpty()) {
                Handler(Looper.getMainLooper()).post {
                    stopForegroundService()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        mNotificationManager = NotificationManagerCompat.from(this)
        createChannel()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val intentAction = intent?.action
        val intentExtras = intent?.extras

        if (intentAction == null)
            return START_STICKY

        var foregroundNotificationHolder = null as NotificationHolder2?

        if (intentAction in listOf(ACTION_DOWNLOADJOBITEM_STARTED, ACTION_PREPARE_DOWNLOAD) && !foregroundActive) {
            if (canCreateGroupedNotification()) {
                summaryNotificationHolder = SummaryNotificationHolder()
                foregroundNotificationHolder = summaryNotificationHolder
            }
        }

        val downloadJobUid = intentExtras?.getInt(EXTRA_DOWNLOADJOBUID) ?: -1
        val endpointUrl = intentExtras?.getString(EXTRA_ENDPOINT)
        val endpoint: Endpoint? = if(endpointUrl != null) Endpoint(endpointUrl) else null

        when (intentAction) {
            ACTION_PREPARE_DOWNLOAD -> {
                val endpointVal = endpoint ?: throw IllegalArgumentException("ACTION_PREPARE_DOWNLOAD requires EXTRA_ENDPOINT")

                val downloadJobNotificationHolder = activeDownloadJobNotifications
                                .firstOrNull {it.downloadJobUid == downloadJobUid }
                        ?: DownloadJobNotificationHolder(downloadJobUid, endpointVal).also {
                            activeDownloadJobNotifications.add(it)
                        }


                GlobalScope.launch {
                    val downloadJobPreparer = DownloadJobPreparer(downloadJobUid = downloadJobUid)
                    val containerDownloadManager: ContainerDownloadManager by on(endpointVal).instance()
                    val appDatabase: UmAppDatabase by on(endpointVal).instance(tag = TAG_DB)
                    val appDatabaseRepo: UmAppDatabase by on(endpointVal).instance(tag = TAG_REPO)

                    downloadJobPreparer.prepare(containerDownloadManager,
                            appDatabase = appDatabase, appDatabaseRepo = appDatabaseRepo,
                            onProgress = {})
                    containerDownloadManager.enqueue(downloadJobUid)
                }


                if (!foregroundActive && foregroundNotificationHolder == null) {
                    UMLog.l(UMLog.DEBUG, 0, "DownloadNotification: offered preparer notification as foreground holder")
                    foregroundNotificationHolder = downloadJobNotificationHolder
                } else {
                    UMLog.l(UMLog.DEBUG, 0, "DownloadNotification: preparer to doNotify")
                    downloadJobNotificationHolder.doNotify()
                }
            }

            ACTION_DOWNLOADJOBITEM_STARTED -> {
//                var downloadJobNotificationHolder = activeDownloadJobNotifications
//                        .firstOrNull { it.downloadJobUid == downloadJobUid }
//                if (downloadJobNotificationHolder == null) {
//                    downloadJobNotificationHolder = DownloadJobNotificationHolder(downloadJobUid)
//                    activeDownloadJobNotifications.add(downloadJobNotificationHolder)
//                }
//
//                if (!foregroundActive && foregroundNotificationHolder == null) {
//                    foregroundNotificationHolder = downloadJobNotificationHolder
//                } else {
//                    UMLog.l(UMLog.DEBUG, 0, "DownloadNotification: Starting notification for new download: $downloadJobUid")
//                    downloadJobNotificationHolder.doNotify()
//                }
            }

            ACTION_PAUSE_DOWNLOAD -> {
                GlobalScope.launch {
                    //networkManagerDeferred.await().containerDownloadManager.pause(downloadJobUid)
                }
            }

            ACTION_CANCEL_DOWNLOAD -> {
                GlobalScope.launch {
                    //networkManagerDeferred.await().containerDownloadManager.cancel(downloadJobUid)
                }
            }

            ACTION_DELETE_DOWNLOAD -> {
                val endpointVal = endpoint ?: throw IllegalArgumentException("ACTION_DELETE_DOWNLOAD requires EXTRA_ENDPOINT")
                var deleteNotificationHolder = DeleteNotificationHolder(downloadJobUid, endpointVal)
                activeDeleteJobNotifications.add(deleteNotificationHolder)

                if (!foregroundActive && foregroundNotificationHolder == null) {
                    foregroundNotificationHolder = deleteNotificationHolder
                } else {
                    deleteNotificationHolder.doNotify()
                }

                GlobalScope.async {
                    val containerDownloadManager: ContainerDownloadManager by on(endpointVal).instance()
                    containerDownloadManager.deleteDownloadJob(downloadJobUid){
                        deleteNotificationHolder.builder.setProgress(MAX_PROGRESS_VALUE, it, false)
                        deleteNotificationHolder.doNotify()
                    }
                    activeDeleteJobNotifications.remove(deleteNotificationHolder)
                    mNotificationManager.cancel(deleteNotificationHolder.notificationId)
                    checkIfCompleteAfterDelay()
                }

            }
        }

        if (!foregroundActive && foregroundNotificationHolder != null) {
            UMLog.l(UMLog.DEBUG, 0, "DownloadNotification: startForeground using $foregroundNotificationHolder")
            startForeground(foregroundNotificationHolder.notificationId,
                    foregroundNotificationHolder.build())
        }


        return START_STICKY
    }

    private fun isEmpty(): Boolean = activeDownloadJobNotifications.isEmpty() && activeDeleteJobNotifications.isEmpty()

    /**
     * Create a channel for the notification
     */
    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val mNotificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH)
        mNotificationChannel.vibrationPattern = longArrayOf(0)
        mNotificationChannel.enableVibration(false)
        mNotificationChannel.setSound(null, null)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(mNotificationChannel)
    }


    /**
     * Stop foreground service
     */
    private fun stopForegroundService() {
        if (!stopped) {
            foregroundActive = false

            stopForeground(true)
            stopSelf()
        }
    }


    @ExperimentalCoroutinesApi
    override fun onDestroy() {
        super.onDestroy()
    }

    private fun canCreateGroupedNotification(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    }

    companion object {

        const val ACTION_PAUSE_DOWNLOAD = "ACTION_PAUSE_DOWNLOAD"

        const val ACTION_CANCEL_DOWNLOAD = "ACTION_CANCEL_DOWNLOAD"

        const val ACTION_DELETE_DOWNLOAD = "ACTION_DELETE_DOWNLOAD"

        const val ACTION_PREPARE_DOWNLOAD = "ACTION_PREPARE_DOWNLOAD"

        const val ACTION_DOWNLOADJOBITEM_STARTED = "ACTION_DOWNLOADJOBITEM_STARTED"

        const val EXTRA_ENDPOINT = "EXTRA_ENDPOINT"

        const val EXTRA_DOWNLOADJOBUID = "EXTRA_DOWNLOADJOBUID"

        const val EXTRA_DOWNLOADJOBITEMUID = "EXTRA_DOWNLOADJOBITEMUID"

        const val EXTRA_CONTENT_ENTRY_UID = "EXTRA_CONTENT_ENTRY_UID"

        const val NOTIFICATION_CHANNEL_ID = "UM_NOTIFICATION_CHANNEL_ID"

        const val NOTIFICATION_GROUP_KEY = "com.android.example.UstadMobile"

        const val MAX_PROGRESS_VALUE = 100
    }
}
