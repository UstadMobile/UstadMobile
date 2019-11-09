package com.ustadmobile.sharedse.network

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import androidx.annotation.MainThread
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import kotlinx.coroutines.newSingleThreadContext
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import com.ustadmobile.lib.util.RunWhenReadyManager
import com.ustadmobile.core.impl.UmAccountManager

/**
 * Wrapper class for NetworkManagerBleCommon. A service is required as this encapsulates
 * peer discovery processes and the http server that should continue running
 * regardless of which activity is active.
 *
 * Note: The Network Manager object itself will not be ready until this service binds to the
 * httpd service. This may or may not be done when an onBind returns. Use runWhenNetworkManagerReady
 * for any function that requires the networkmanager to be initialized.
 *
 * @author Kileha3
 */
class NetworkManagerBleAndroidService : Service() {

    private val mBinder = this.LocalServiceBinder()

    @Volatile
    var networkManagerBle: NetworkManagerBle? = null
        private set

    @Volatile
    private var httpd: EmbeddedHTTPD? = null

    private var umAppDatabase: UmAppDatabase? = null

    private var mBadNodeExecutorService: ScheduledExecutorService? = null

    private val runWhenReadyManager = RunWhenReadyManager()

    private val mHttpdServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val serviceHttpd =(service as EmbeddedHttpdService.LocalServiceBinder).getHttpd()
            httpd = serviceHttpd
            handleHttpdServiceBound(serviceHttpd)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            httpd = null
            runWhenReadyManager.ready = false
        }
    }

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

    @MainThread
    fun runWhenNetworkManagerReady(block: () -> Unit) = runWhenReadyManager.runWhenReady(block)

    private fun handleHttpdServiceBound(embeddedHTTPD: EmbeddedHTTPD) {
        val createdNetworkManager = NetworkManagerBle(this,
                newSingleThreadContext("NetworkManager-SingleThread"), embeddedHTTPD,
                UmAppDatabase.getInstance(this))
        networkManagerBle = createdNetworkManager
        createdNetworkManager.onCreate()
        runWhenReadyManager.ready = true
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onDestroy() {
        super.onDestroy()
        if (httpd != null)
            unbindService(mHttpdServiceConnection)

        mBadNodeExecutorService!!.shutdown()
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we won't be dealing with IPC.
     *
     * Note that the NetworkManagerBle object won't be ready until this service has been bound to
     * the httpd service. Use runWhenNetworkManagerReady for any calls that require the networkManager
     * itself.
     */
    inner class LocalServiceBinder : Binder() {
        val service: NetworkManagerBleAndroidService
            get() = this@NetworkManagerBleAndroidService

    }

}
