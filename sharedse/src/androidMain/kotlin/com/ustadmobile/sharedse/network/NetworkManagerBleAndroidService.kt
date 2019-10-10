package com.ustadmobile.sharedse.network

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
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
