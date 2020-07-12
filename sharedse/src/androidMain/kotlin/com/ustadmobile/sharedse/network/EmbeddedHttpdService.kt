package com.ustadmobile.sharedse.network

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import org.kodein.di.DIAware
import org.kodein.di.android.di
import org.kodein.di.instance

@Deprecated("Use httpd from dependency injection instead")
class EmbeddedHttpdService : Service(), DIAware {

    override val di by di()

    private val mBinder = LocalServiceBinder()

    val httpd: EmbeddedHTTPD by instance()

    inner class LocalServiceBinder : Binder() {

        fun getHttpd(): EmbeddedHTTPD {
            return httpd
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()

//        val appDatabase = UmAccountManager.getActiveDatabase(applicationContext)
//        val appRepository = UmAccountManager.getRepositoryForActiveAccount(applicationContext)
//        httpd = EmbeddedHTTPD(0, applicationContext, appDatabase = appDatabase,
//                repository =  appRepository)
//        httpd.addRoute("$ANDROID_ASSETS_PATH(.)+", AndroidAssetsHandler::class.java,
//                applicationContext)
//        httpd.UmAppDatabase_AddUriMapping(UmAccountManager.getActiveDatabase(this),
//                Gson(), "/test/", false, "/rest/UmAppDatabase")
//        try {
//            httpd.start()
//            UMLog.l(UMLog.INFO, 0, "Started embedded HTTP server on port: ${httpd.listeningPort}")
//        } catch (e: IOException) {
//            UMLog.l(UMLog.CRITICAL, 0, "Could not start httpd server")
//            throw RuntimeException("Could not start httpd server", e)
//        }

    }

    override fun onDestroy() {
        httpd.stop()
        super.onDestroy()
    }

    companion object {

        val ANDROID_ASSETS_PATH = "/android-assets/"
    }
}
