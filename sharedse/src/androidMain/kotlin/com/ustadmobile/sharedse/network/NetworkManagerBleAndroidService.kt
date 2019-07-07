package com.ustadmobile.sharedse.network

import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.networkmanager.OnDownloadJobItemChangeListener
import com.ustadmobile.lib.db.entities.DownloadJobItemStatus
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.port.sharedse.util.AsyncServiceManager
import com.ustadmobile.port.sharedse.util.AsyncServiceManager.Companion.STATE_STARTING
import com.ustadmobile.sharedse.network.DownloadNotificationService.Companion.ACTION_START_FOREGROUND_SERVICE
import com.ustadmobile.sharedse.network.DownloadNotificationService.Companion.ACTION_STOP_FOREGROUND_SERVICE
import com.ustadmobile.sharedse.network.DownloadNotificationService.Companion.GROUP_SUMMARY_ID
import com.ustadmobile.sharedse.network.DownloadNotificationService.Companion.JOB_ID_TAG
import kotlinx.coroutines.newSingleThreadContext
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Wrapper class for NetworkManagerBleCommon. A service is required as this encapsulates
 * peer discovery processes and the http server that should continue running
 * regardless of which activity is active.
 *
 * @author Kileha3
 */
class NetworkManagerBleAndroidService : Service() {

    private val mBinder = this.LocalServiceBinder()

    private val managerAndroidBleRef = AtomicReference<NetworkManagerBle?>()

    private val mHttpServiceBound = AtomicBoolean(false)

    private val httpdRef = AtomicReference<EmbeddedHTTPD>()

    private var umAppDatabase: UmAppDatabase? = null

    private var mBadNodeExecutorService: ScheduledExecutorService? = null

    private lateinit var notificationServiceIntent: Intent


    private val notificationExecutor = Executors.newSingleThreadScheduledExecutor()

    private val notificationServiceManager = object : AsyncServiceManager(JobStatus.STOPPED,
            { runnable, delay -> notificationExecutor.schedule(runnable, delay, TimeUnit.MILLISECONDS) }){

        override fun start() {
            UMLog.l(UMLog.INFO, 699, "Starting foreground notification")
            notificationServiceIntent = Intent(applicationContext,
                    DownloadNotificationService::class.java)
            notificationServiceIntent.action = ACTION_START_FOREGROUND_SERVICE
            notificationServiceIntent.putExtra(JOB_ID_TAG, GROUP_SUMMARY_ID)

            val componentName: ComponentName? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(notificationServiceIntent)
            } else {
                startService(notificationServiceIntent)
            }
            notifyStateChanged(if(componentName != null) STATE_STARTED else STATE_STOPPED)
        }

        override fun stop() {
            notificationServiceIntent.action = ACTION_STOP_FOREGROUND_SERVICE
            val servicePendingIntent = PendingIntent.getService(applicationContext,
                    0, notificationServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            servicePendingIntent.send()
            notifyStateChanged(STATE_STOPPED)
        }

    }

    private val mHttpdServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mHttpServiceBound.set(true)
            httpdRef.set((service as EmbeddedHttpdService.LocalServiceBinder).getHttpd())
            handleHttpdServiceBound()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mHttpServiceBound.set(false)
        }
    }


    /**
     * @return Running instance of the NetworkManagerBleCommon
     */
    val networkManagerBle: NetworkManagerBle?
        get() = managerAndroidBleRef.get()


    private val badNodeDeletionTask = Runnable {
        val minLastSeen = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5)
        umAppDatabase!!.networkNodeDao.deleteOldAndBadNode(minLastSeen, 5)
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        umAppDatabase = UmAppDatabase.getInstance(applicationContext)

        val serviceIntent = Intent(applicationContext, EmbeddedHttpdService::class.java)
        bindService(serviceIntent, mHttpdServiceConnection, Context.BIND_AUTO_CREATE)

        mBadNodeExecutorService = Executors.newScheduledThreadPool(1)
        mBadNodeExecutorService!!.scheduleAtFixedRate(badNodeDeletionTask,
                0, 5, TimeUnit.MINUTES)
    }

    private fun handleHttpdServiceBound() {
        val managerAndroidBle = NetworkManagerBle(this,
                newSingleThreadContext("NetworkManager-SingleThread"),httpdRef.get())
        managerAndroidBleRef.set(managerAndroidBle)
        managerAndroidBle.onCreate()

        managerAndroidBle.addDownloadChangeListener(object: OnDownloadJobItemChangeListener {
            override fun onDownloadJobItemChange(status: DownloadJobItemStatus?, downloadJobUid: Int) {
                if(status != null){
                    notificationServiceManager.setEnabled(true)
                    if(status.status >= JobStatus.RUNNING_MIN && status.status <= JobStatus.RUNNING_MAX){
                        notificationExecutor.schedule({checkNotificationDownloadService()},
                                0,TimeUnit.MILLISECONDS)
                    }else{
                        val isServiceManagerActive =
                                managerAndroidBle.activeDownloadJobItemManagers.size > 1
                        notificationServiceManager.setEnabled(isServiceManagerActive)
                    }
                }
            }
        })

    }

    private fun checkNotificationDownloadService(){
        if(notificationServiceManager.state < STATE_STARTING){
            notificationExecutor.schedule({checkNotificationDownloadService()},
                    0,TimeUnit.MILLISECONDS)
        }
    }


    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mHttpServiceBound.get())
            unbindService(mHttpdServiceConnection)

        mBadNodeExecutorService!!.shutdown()

        val managerAndroidBle = managerAndroidBleRef.get()
        managerAndroidBle?.onDestroy()
        notificationServiceManager.setEnabled(false)
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we won't be dealing with IPC.
     */
    inner class LocalServiceBinder : Binder() {
        val service: NetworkManagerBleAndroidService
            get() = this@NetworkManagerBleAndroidService

    }

}
