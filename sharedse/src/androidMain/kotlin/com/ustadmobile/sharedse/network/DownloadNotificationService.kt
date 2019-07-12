package com.ustadmobile.sharedse.network

import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.GROUP_ALERT_SUMMARY
import androidx.core.app.NotificationManagerCompat
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.OnDownloadJobItemChangeListener
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.lib.db.entities.DownloadJobItemStatus
import com.ustadmobile.port.sharedse.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * This services monitors the download job statuses and act accordingly
 */
class DownloadNotificationService : Service(), OnDownloadJobItemChangeListener {

    private val mNetworkServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mNetworkServiceBound.set(true)

            networkManagerBle = (service as NetworkManagerBleAndroidService.LocalServiceBinder)
                    .service.networkManagerBle
            networkManagerBle?.addDownloadChangeListener(this@DownloadNotificationService)
            val activeDownloadManagers = networkManagerBle?.activeDownloadJobItemManagers!!
            for (manager in activeDownloadManagers) {
                if (manager.rootItemStatus != null && manager.rootContentEntryUid == manager.rootItemStatus!!.contentEntryUid) {
                    onDownloadJobItemChange(manager.rootItemStatus, manager.downloadJobUid)
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mNetworkServiceBound.set(false)
            if (networkManagerBle != null) {
//                networkManagerBle!!.removeDownloadChangeListener(this@DownloadNotificationService)
                networkManagerBle = null
            }
        }
    }

    private val mNetworkServiceBound = AtomicBoolean(false)

    private var networkManagerBle: NetworkManagerBle? = null

    private var totalBytesToBeDownloaded: Long = 0

    private var totalBytesDownloadedSoFar: Long = 0

    private var mNotificationManager: NotificationManagerCompat? = null

    private val downloadJobIdToNotificationMap = HashMap<Int, NotificationHolder>()

    private val notificationIdRef = AtomicInteger(9)

    private var timeLastUpdate: Long = 0

    private val MAX_UPDATE_TIME_DELAY = TimeUnit.SECONDS.toMillis(2)

    private var umAppDatabase: UmAppDatabase? = null

    private var impl: UstadMobileSystemImpl? = null

    private val serviceInstanceId = SERVICE_ID_COUNTER.incrementAndGet()

    private val stopped = AtomicBoolean(false)


    /**
     * Holder class for the entire notification
     */
    inner class NotificationHolder internal constructor(internal val notificationId: Int, contentTitle: String, val builder: NotificationCompat.Builder) {

        internal var downloadProgress = 0

        internal var jobTitle: String? = null
            private set

        init {
            this.jobTitle = contentTitle
        }

        internal fun setContentTitle(contentTitle: String?) {
            this.jobTitle = contentTitle
            builder.setContentTitle(contentTitle)
        }

        internal fun build(): Notification {
            val notification = builder.build()
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                notification.defaults = 0
                notification.sound = null
            }

            return notification
        }
    }

    override fun onCreate() {
        super.onCreate()
        mNotificationManager = NotificationManagerCompat.from(this)
        createChannel()

        umAppDatabase = UmAppDatabase.getInstance(this)

        //bind to network service
        val networkServiceIntent = Intent(applicationContext,
                NetworkManagerBleAndroidService::class.java)
        bindService(networkServiceIntent, mNetworkServiceConnection, Context.BIND_AUTO_CREATE)

        impl = UstadMobileSystemImpl.instance
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent != null && intent.action != null && intent.extras != null) {
            val action = intent.action
            val downloadJobId = intent.extras!!.getInt(JOB_ID_TAG)
            val notificationHolder = downloadJobIdToNotificationMap[downloadJobId]

            when (action) {
                ACTION_START_FOREGROUND_SERVICE -> {
                    timeLastUpdate = System.currentTimeMillis()
                    val contentTitle = impl!!.getString(MessageID.downloading,
                            applicationContext)
                    val notification = createNotification(GROUP_SUMMARY_ID.toLong(),
                            notificationIdRef.get(), contentTitle, "", "",
                            canCreateGroupedNotification())
                    startForeground(notificationIdRef.get(), notification.build())
                }

                ACTION_STOP_FOREGROUND_SERVICE -> stopForegroundService()

                ACTION_PAUSE_DOWNLOAD -> if (notificationHolder != null) {
                    GlobalScope.launch {
                        umAppDatabase!!.downloadJobDao
                                .updateJobAndItems(downloadJobId, JobStatus.PAUSED,
                                        JobStatus.PAUSING)
                    }
                }else{
                    stopForegroundService()
                }

                ACTION_CANCEL_DOWNLOAD -> if (notificationHolder != null) {
                    GlobalScope.launch {
                        umAppDatabase!!.downloadJobDao
                                .updateJobAndItems(downloadJobId, JobStatus.CANCELED,
                                        JobStatus.CANCELLING)
                    }
                }else{
                    stopForegroundService()
                }
            }
        }
        return START_STICKY
    }

    @Synchronized
    override fun onDownloadJobItemChange(status: DownloadJobItemStatus?, downloadJobUid: Int) {
        val notificationHolder = downloadJobIdToNotificationMap[downloadJobUid]
        val isRunning = status != null && status.status >= JobStatus.RUNNING_MIN && status.status <= JobStatus.RUNNING_MAX

        if(status != null){
            if (notificationHolder == null) {
                UMLog.l(UMLog.VERBOSE, 699,
                        "Service #" + serviceInstanceId +
                                " Creating new notification for download #" + downloadJobUid)
                totalBytesToBeDownloaded += status.totalBytes
                val notificationId = notificationIdRef.incrementAndGet()
                val contentTitle = String.format(impl!!.getString(
                        MessageID.download_downloading_placeholder, this),
                        UMFileUtil.formatFileSize(status.bytesSoFar),
                        UMFileUtil.formatFileSize(status.totalBytes))
                val holder = createNotification(downloadJobUid.toLong(), notificationId,
                        "", contentTitle, contentTitle, false)
                downloadJobIdToNotificationMap[downloadJobUid] = holder

                GlobalScope.launch {
                    val title = umAppDatabase!!.downloadJobDao.getEntryTitleByJobUidAsync(downloadJobUid)
                    holder.setContentTitle(title)
                    mNotificationManager!!.notify(notificationId, holder.build())
                }

            } else if (status.status >= JobStatus.COMPLETE_MIN) {
                //job has completed and notification needs to be removed
                val notification = downloadJobIdToNotificationMap[downloadJobUid]
                if (notification != null) {
                    mNotificationManager!!.cancel(notification.notificationId)
                    downloadJobIdToNotificationMap.remove(downloadJobUid)
                    if (downloadJobIdToNotificationMap.isEmpty()) {
                        UMLog.l(UMLog.INFO, 699, "DownloadNotificationService: Stop")
                        stopForegroundService()
                    }
                } else {
                    UMLog.l(UMLog.ERROR, 699, "Cannot find notification for download!")
                }
            } else {
                totalBytesDownloadedSoFar += status.bytesSoFar
                val progress = (status.bytesSoFar.toDouble() / status.totalBytes * 100).toInt()
                val timeCurrentUpdate = System.currentTimeMillis()
                notificationHolder.downloadProgress = progress

                if (timeCurrentUpdate - timeLastUpdate!! < MAX_UPDATE_TIME_DELAY
                        && notificationHolder.downloadProgress > 0 && isRunning)
                    return

                timeLastUpdate = timeCurrentUpdate
                val contentTitle = String.format(impl!!.getString(
                        MessageID.download_downloading_placeholder, this),
                        UMFileUtil.formatFileSize(status.bytesSoFar),
                        UMFileUtil.formatFileSize(status.totalBytes))
                updateDownloadJobNotification(downloadJobUid.toLong(), progress, contentTitle,
                        notificationHolder.jobTitle, notificationHolder.jobTitle)
                updateDownloadSummary()
            }
        }
    }

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
        mNotificationChannel.enableVibration(true)
        mNotificationChannel.setSound(null, null)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(mNotificationChannel)
    }

    /**
     * Create action buttons
     * @param downloadJobId Identifies the job on which buttons will appear
     * @param actionTag action tags for the prnding intent
     * @param actionLabel button label text
     * @return constructed action button
     */
    private fun createAction(downloadJobId: Long, actionTag: String,
                             actionLabel: String): NotificationCompat.Action {
        val actionIntent = Intent(this, DownloadNotificationService::class.java)
        actionIntent.putExtra(JOB_ID_TAG, downloadJobId.toInt())
        actionIntent.action = actionTag
        val actionPendingIntent = PendingIntent.getService(this,
                0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Action(0, actionLabel, actionPendingIntent)
    }

    /**
     * Construct a notification object
     * @param downloadJobId Identifies the job in which notification belongs
     * @param notificationId Notification Id
     * @param contentTitle Notification content title
     * @param contentText Notification content text
     * @param contentSubText Notification content sub text
     * @param isGroupSummary Flag to indicate if the notification will act as a group summary or not.
     * @return constructed notification object
     */
    private fun createNotification(downloadJobId: Long, notificationId: Int, contentTitle: String,
                           contentText: String, contentSubText: String,
                           isGroupSummary: Boolean): NotificationHolder {

        val intent = Intent()
        val mNotificationPendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val builder = NotificationCompat.Builder(this,
                NOTIFICATION_CHANNEL_ID)
        builder.setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setWhen(System.currentTimeMillis())
                //TODO: set the color
                //.setColor(ContextCompat.getColor(this, R.color.primary))
                .setOngoing(true)
                .setGroupAlertBehavior(GROUP_ALERT_SUMMARY)
                .setAutoCancel(true)
                .setContentIntent(mNotificationPendingIntent)
                .setDefaults(Notification.DEFAULT_SOUND)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.ic_file_download_white_24dp)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }

        val notificationHolder = NotificationHolder(notificationId, contentTitle,
                builder)
        if (isGroupSummary) {
            val inboxStyle = NotificationCompat.InboxStyle()
                    .setBigContentTitle(contentTitle)
                    .setSummaryText(contentSubText)

            builder.setGroupSummary(true)
                    .setStyle(inboxStyle)
        } else {
            builder.setProgress(MAX_PROGRESS_VALUE, 0, true)
                    .addAction(createAction(downloadJobId,
                            ACTION_CANCEL_DOWNLOAD, impl!!.getString(MessageID.download_cancel_label,
                            applicationContext)))
                    .addAction(createAction(downloadJobId,
                            ACTION_PAUSE_DOWNLOAD, impl!!.getString(MessageID.download_pause_download,
                            applicationContext)))
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setSubText(contentSubText)
        }

        builder.setGroup(NOTIFICATION_GROUP_KEY)

        return notificationHolder
    }


    /**
     * Update download job notification
     * @param downloadJobId Id to indicate which job is the notification for
     * @param progress current download progress
     * @param contentTitle Notification content title
     * @param contentText Notification content text
     * @param contentSubText Notification content sub text
     */
    private fun updateDownloadJobNotification(downloadJobId: Long, progress: Int, contentTitle: String,
                                              contentText: String?, contentSubText: String?) {
        val notificationHolder = downloadJobIdToNotificationMap[downloadJobId.toInt()]
        if (notificationHolder != null) {
            val builder = notificationHolder.builder
            builder.setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setSubText(contentSubText)
                    .setProgress(MAX_PROGRESS_VALUE, progress, false)
            mNotificationManager!!.notify(notificationHolder.notificationId,
                    notificationHolder.builder.build())
        }
    }

    /**
     * Update summary notification to show progress as the sum of all download notifications
     */
    private fun updateDownloadSummary() {
        val notificationHolder = downloadJobIdToNotificationMap[GROUP_SUMMARY_ID]
        if (notificationHolder != null) {
            val summaryLabel = impl!!.getString(MessageID.download_downloading_placeholder,
                    applicationContext)
            val title = String.format(summaryLabel,
                    UMFileUtil.formatFileSize(totalBytesDownloadedSoFar),
                    UMFileUtil.formatFileSize(totalBytesToBeDownloaded))
            totalBytesDownloadedSoFar = 0L
            notificationHolder.builder.setSubText(title)
            mNotificationManager!!.notify(notificationHolder.notificationId,
                    notificationHolder.builder.build())
        }
    }


    /**
     * Stop foreground service
     */
    private fun stopForegroundService() {
        if (!stopped.getAndSet(true)) {
            networkManagerBle?.removeDownloadChangeListener(this)

            downloadJobIdToNotificationMap.clear()
            stopForeground(true)
            stopSelf()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (mNetworkServiceBound.get())
            unbindService(mNetworkServiceConnection)
    }

    private fun canCreateGroupedNotification(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    }

    companion object {

        const val ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE"

        const val ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE"

        const val ACTION_PAUSE_DOWNLOAD = "ACTION_PAUSE_DOWNLOAD"

        const val ACTION_CANCEL_DOWNLOAD = "ACTION_CANCEL_DOWNLOAD"

        const val NOTIFICATION_CHANNEL_ID = "UM_NOTIFICATION_CHANNEL_ID"

        const val JOB_ID_TAG = "UM_JOB_ID"

        const val NOTIFICATION_GROUP_KEY = "com.android.example.UstadMobile"

        const val MAX_PROGRESS_VALUE = 100

        const val GROUP_SUMMARY_ID = -1

        private val SERVICE_ID_COUNTER = AtomicInteger(0)
    }
}
