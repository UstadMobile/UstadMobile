package com.ustadmobile.core.networkmanager

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
import com.github.aakira.napier.Napier
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.lib.util.copyOnWriteListOf
import java.util.concurrent.atomic.AtomicInteger
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.lib.db.entities.ContainerImportJob
import com.ustadmobile.lib.db.entities.DownloadJob
import kotlinx.coroutines.*
import org.kodein.di.*
import org.kodein.di.android.di
import kotlin.IllegalArgumentException
import com.ustadmobile.core.R

/**
 * This services monitors the download job statuses and displays progress to the user. When pending
 * jobs are complete, the service will stop and the notification will disappear.
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

    private val activeImportJobNotifications: MutableList<ImportJobNotificationHolder> = copyOnWriteListOf()

    abstract inner class NotificationHolder2(var contentTitle: String, var contentText: String,
                                         val notificationId: Int = notificationIdRef.incrementAndGet()) {

        private val intent = Intent()
        private val mNotificationPendingIntent : PendingIntent = PendingIntent.getActivity(
            this@DownloadNotificationService, 0, intent, 0)
        private val startTime = System.currentTimeMillis()

        /**
         * Setup the notificationcompat.builde rwith common options required for all notifications we are using
         */
        protected fun createNotificationBuilder(): NotificationCompat.Builder {
            val builder = NotificationCompat.Builder(this@DownloadNotificationService,
                    NOTIFICATION_CHANNEL_ID)
            builder.setPriority(NotificationCompat.PRIORITY_LOW)
                    .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                    .setWhen(startTime)
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

            builder.setSmallIcon(R.drawable.ic_file_download_white_24dp)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)


            return builder
        }

        /**
         * Child classes must implement this themselves to return a notification. This should build
         * a new notification object using the createNotificationBuilder function.
         */
        abstract fun build(): Notification

        @Suppress("DEPRECATION")
        internal fun doNotify() {
            UMLog.l(UMLog.DEBUG, 0, "DownloadNotification: holder $this sending notification: ")
            val notification = build().apply {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    //this older version is not using a notification channel
                    defaults = 0
                    sound = null
                }
            }
            mNotificationManager.notify(notificationId, notification)
        }

        /**
         * Create action buttons
         * @param downloadJobId Identifies the job on which buttons will appear
         * @param actionTag action tags for the prnding intent
         * @param actionLabel button label text
         * @return constructed action button
         */
        @Deprecated("Use addDownloadAction extension function")
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

        fun NotificationCompat.Builder.addDownloadAction(downloadJobId: Int, endpoint: String,
                                                                actionTag: String, actionLabel: String) : NotificationCompat.Builder{
            val actionIntent = Intent(this@DownloadNotificationService,
                DownloadNotificationService::class.java)
            actionIntent.putExtra(EXTRA_DOWNLOADJOBUID, downloadJobId)
            actionIntent.putExtra(EXTRA_ENDPOINT, endpoint)
            actionIntent.action = actionTag
            val actionPendingIntent = PendingIntent.getService(this@DownloadNotificationService,
                0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            addAction(NotificationCompat.Action(0, actionLabel, actionPendingIntent))
            return this
        }

        internal open fun cancel() {
            mNotificationManager.cancel(notificationId)
            checkIfCompleteAfterDelay()
        }

    }

    inner class DownloadJobNotificationHolder(val downloadJobUid: Int, private val endpoint: Endpoint,
                                              notifyAfterInit: Boolean = true) : NotificationHolder2(
            impl.getString(MessageID.loading, applicationContext),
            impl.getString(MessageID.waiting, applicationContext)), DoorObserver<DownloadJob?> {

        var bytesSoFar: Long = 0
        var totalBytes: Long = 0
        var status: Int = 0

        lateinit var downloadJobLiveData: DoorLiveData<DownloadJob?>

        init {
            GlobalScope.launch(Dispatchers.Main) {
                val db: UmAppDatabase = on(endpoint).direct.instance(tag = TAG_DB)
                val downloadJobTitleInDb = db.downloadJobDao.getEntryTitleByJobUidAsync(downloadJobUid)
                        ?: ""
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
                status = t.djStatus


                contentText = String.format(impl.getString(
                        MessageID.download_downloading_placeholder, this@DownloadNotificationService),
                        UMFileUtil.formatFileSize(bytesSoFar),
                        UMFileUtil.formatFileSize(totalBytes))

                doNotify()
                summaryNotificationHolder?.updateSummary()

                if(t.djStatus >= JobStatus.COMPLETE_MIN) {
                    cancel()
                }
            }
        }

        override fun build(): Notification {
            val progress = (bytesSoFar.toDouble() / totalBytes * 100).toInt()
            val systemImpl: UstadMobileSystemImpl = di.direct.instance()
            val context = this@DownloadNotificationService
            return createNotificationBuilder()
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setProgress(MAX_PROGRESS_VALUE, progress, false)
                .apply {
                    if(status == JobStatus.PAUSED) {
                        addDownloadAction(downloadJobUid, endpoint.url, ACTION_RESUME_DOWNLOAD,
                            systemImpl.getString(MessageID.download_continue_btn_label, context))
                    }else {
                        addDownloadAction(downloadJobUid, endpoint.url, ACTION_PAUSE_DOWNLOAD,
                            systemImpl.getString(MessageID.download_pause_download, context))
                    }
                }
                .addDownloadAction(downloadJobUid, endpoint.url, ACTION_CANCEL_DOWNLOAD,
                    systemImpl.getString(MessageID.cancel, context))
                .build()
        }

        override fun cancel() {
            downloadJobLiveData.removeObserver(this)
            activeDownloadJobNotifications.remove(this)
            super.cancel()
        }
    }

    inner class DeleteNotificationHolder(val downloadJobItemUid: Int, val endpoint: Endpoint)
        : NotificationHolder2(impl.getString(MessageID.deleting, applicationContext),
            impl.getString(MessageID.deleting, applicationContext)) {

        internal var progress: Int = 0

        init {
            GlobalScope.launch {
                val db: UmAppDatabase = on(endpoint).direct.instance(tag = TAG_DB)
                val downloadJobTitleInDb = db.downloadJobItemDao.getEntryTitleByDownloadJobItemUidAsync(downloadJobItemUid) ?: ""
                contentTitle = downloadJobTitleInDb
                doNotify()
            }
        }

        override fun build(): Notification {
            return createNotificationBuilder()
                .setProgress(MAX_PROGRESS_VALUE, progress, false)
                .setContentTitle(contentTitle)
                .build()
        }

        override fun cancel() {
            activeDeleteJobNotifications.remove(this)
            super.cancel()
        }
    }

    inner class ImportJobNotificationHolder(val importJobUid: Long, val endpoint: Endpoint) : NotificationHolder2(
            impl.getString(MessageID.loading, applicationContext),
            impl.getString(MessageID.processing, applicationContext)), DoorObserver<ContainerImportJob?> {

        var bytesSoFar: Long = 0
        var totalBytes: Long = 0

        lateinit var importJobLiveData: DoorLiveData<ContainerImportJob?>

        init {
            GlobalScope.launch(Dispatchers.Main) {
                val db: UmAppDatabase = on(endpoint).direct.instance(tag = TAG_DB)
                val importJobTitleEntry = db.containerImportJobDao.getTitleOfEntry(importJobUid)
                        ?: ""
                contentTitle = importJobTitleEntry
                doNotify()

                importJobLiveData = db.containerImportJobDao.getImportJobLiveData(importJobUid)
                importJobLiveData.observeForever(this@ImportJobNotificationHolder)
            }

        }

        override fun onChanged(t: ContainerImportJob?) {
            if(t != null) {
                bytesSoFar = t.cijBytesSoFar
                totalBytes = t.cijContentLength

                contentText = if(t.cijImportCompleted) impl.getString(
                        MessageID.uploading, this@DownloadNotificationService)
                else
                    impl.getString(
                            MessageID.processing, this@DownloadNotificationService)

                Napier.d(tag = "NotificationService", message = "container import changed desc is $contentText")

                doNotify()

                if(t.cijJobStatus >= JobStatus.COMPLETE_MIN) {
                    activeImportJobNotifications.remove(this)
                    mNotificationManager.cancel(notificationId)
                    cancel()
                }
            }
        }

        override fun build(): Notification {
            val progress = (bytesSoFar.toDouble() / totalBytes * 100).toInt()
            return createNotificationBuilder()
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setProgress(MAX_PROGRESS_VALUE, progress, false)
                .build()
        }
    }


    inner class SummaryNotificationHolder() : NotificationHolder2(
            impl.getString(MessageID.downloading, applicationContext),
            impl.getString(MessageID.downloading, applicationContext)) {

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

            doNotify()
        }

        override fun build(): Notification {
            return createNotificationBuilder()
                .setGroupSummary(true)
                .setStyle(NotificationCompat.InboxStyle()
                    .setBigContentTitle(contentTitle)
                    .setSummaryText(contentText)
                    .also { inboxStyle ->
                        activeDownloadJobNotifications.forEach {
                            inboxStyle.addLine("${it.contentTitle} - ${it.contentText}")
                        }
                    })
                .build()
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


        var foregroundNotificationHolder: NotificationHolder2? = summaryNotificationHolder

        //If summary notification holder is null, and it can be created, we need to create it.
        if (summaryNotificationHolder == null && canCreateGroupedNotification()){
            summaryNotificationHolder = SummaryNotificationHolder()
            foregroundNotificationHolder = summaryNotificationHolder
        }

        val downloadJobUid = intentExtras?.getInt(EXTRA_DOWNLOADJOBUID) ?: -1
        val importJobUid = intentExtras?.getLong(EXTRA_IMPORTJOB_UID) ?: -1
        val downloadJobItemUid = intentExtras?.getInt(EXTRA_DOWNLOADJOBITEMUID) ?: -1
        val endpointUrl = intentExtras?.getString(EXTRA_ENDPOINT)
        val endpoint: Endpoint? = if(endpointUrl != null) Endpoint(endpointUrl) else null
        var createdNotificationHolder: NotificationHolder2? = null


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

                createdNotificationHolder = downloadJobNotificationHolder
            }

            ACTION_DOWNLOADJOBITEM_STARTED -> {
                val endpointVal = endpoint ?: throw IllegalArgumentException("DownloadNotificationService: ACTION_DOWNLOADJOBITEM_STARTED intent without endpoint")
                val downloadJobNotificationHolder = activeDownloadJobNotifications
                    .firstOrNull {it.downloadJobUid == downloadJobUid }
                    ?: DownloadJobNotificationHolder(downloadJobUid, endpointVal).also {
                        activeDownloadJobNotifications.add(it)
                    }
                createdNotificationHolder = downloadJobNotificationHolder
            }

            ACTION_PAUSE_DOWNLOAD -> {
                val endpointVal = endpoint ?: throw IllegalArgumentException("DownloadNotificationService: ACTION_PAUSEDOWNLOAD intent without endpoint")
                GlobalScope.launch {
                    val containerDownloadManager: ContainerDownloadManager = di.on(endpointVal).direct.instance()
                    containerDownloadManager.pause(downloadJobUid)
                }
            }

            ACTION_RESUME_DOWNLOAD -> {
                val endpointVal = endpoint ?: throw IllegalArgumentException("DownloadNotificationService: ACTION_RESUME_DOWNLOAD intent without endpoint")
                GlobalScope.launch {
                    val containerDownloadManager: ContainerDownloadManager = di.on(endpointVal).direct.instance()
                    containerDownloadManager.enqueue(downloadJobUid)
                }
            }

            ACTION_CANCEL_DOWNLOAD -> {
                val endpointVal = endpoint ?: throw IllegalArgumentException("DownloadNotificationService: ACTION_CANCEL_DOWNLOAD intent without endpoint")
                GlobalScope.launch {
                    val containerDownloadManager: ContainerDownloadManager = di.on(endpointVal).direct.instance()
                    containerDownloadManager.cancel(downloadJobUid)
                }
            }

            ACTION_DELETE_DOWNLOAD -> {
                val endpointVal = endpoint ?: throw IllegalArgumentException("ACTION_DELETE_DOWNLOAD requires EXTRA_ENDPOINT")
                val deleteNotificationHolder = DeleteNotificationHolder(downloadJobItemUid, endpointVal)
                activeDeleteJobNotifications.add(deleteNotificationHolder)
                createdNotificationHolder = deleteNotificationHolder

                GlobalScope.launch {
                    val containerDownloadManager: ContainerDownloadManager by on(endpointVal).instance()
                    containerDownloadManager.deleteDownloadJobItem(downloadJobItemUid){ progress ->
                        deleteNotificationHolder.progress = progress
                        deleteNotificationHolder.doNotify()
                    }
                    deleteNotificationHolder.cancel()
                }
            }

            ACTION_PREPARE_IMPORT ->{
                val endpointVal = endpoint ?: throw IllegalArgumentException("ACTION_PREPARE_IMPORT requires EXTRA_ENDPOINT")

                val importNotificationHolder = ImportJobNotificationHolder(importJobUid, endpointVal)
                activeImportJobNotifications.add(importNotificationHolder)
                createdNotificationHolder = importNotificationHolder
            }
        }

        if(!foregroundActive && foregroundNotificationHolder != null) {
            startForeground(foregroundNotificationHolder.notificationId,
                foregroundNotificationHolder.build())
            foregroundActive = true
        }else {
            createdNotificationHolder?.doNotify()
        }

        return START_STICKY
    }

    private fun isEmpty(): Boolean =
            activeDownloadJobNotifications.isEmpty()
            && activeDeleteJobNotifications.isEmpty()
            && activeImportJobNotifications.isEmpty()

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

    private fun canCreateGroupedNotification(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    }

    companion object {

        const val ACTION_PAUSE_DOWNLOAD = "ACTION_PAUSE_DOWNLOAD"

        const val ACTION_RESUME_DOWNLOAD = "ACTION_RESUME_DOWNLOAD"

        const val ACTION_CANCEL_DOWNLOAD = "ACTION_CANCEL_DOWNLOAD"

        const val ACTION_DELETE_DOWNLOAD = "ACTION_DELETE_DOWNLOAD"

        const val ACTION_PREPARE_DOWNLOAD = "ACTION_PREPARE_DOWNLOAD"

        const val ACTION_PREPARE_IMPORT = "ACTION_PREPARE_IMPORT"

        const val ACTION_DOWNLOADJOBITEM_STARTED = "ACTION_DOWNLOADJOBITEM_STARTED"

        const val EXTRA_ENDPOINT = "EXTRA_ENDPOINT"

        const val EXTRA_DOWNLOADJOBUID = "EXTRA_DOWNLOADJOBUID"

        const val EXTRA_IMPORTJOB_UID = "EXTRA_IMPORTJOBUID"

        const val EXTRA_DOWNLOADJOBITEMUID = "EXTRA_DOWNLOADJOBITEMUID"

        const val EXTRA_CONTENT_ENTRY_UID = "EXTRA_CONTENT_ENTRY_UID"

        const val NOTIFICATION_CHANNEL_ID = "UM_NOTIFICATION_CHANNEL_ID"

        const val NOTIFICATION_GROUP_KEY = "com.android.example.UstadMobile"

        const val MAX_PROGRESS_VALUE = 100
    }
}
