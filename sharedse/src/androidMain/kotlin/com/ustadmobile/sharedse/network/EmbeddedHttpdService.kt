package com.ustadmobile.sharedse.network

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.google.gson.Gson
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase_AddUriMapping
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import java.io.IOException

class EmbeddedHttpdService : Service() {

    private lateinit var httpd: EmbeddedHTTPD

    private val mBinder = LocalServiceBinder()

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
        httpd = EmbeddedHTTPD(0, applicationContext)
        httpd.addRoute("$ANDROID_ASSETS_PATH(.)+", AndroidAssetsHandler::class.java,
                applicationContext)
        httpd.UmAppDatabase_AddUriMapping(UmAppDatabase.getInstance(this),
                Gson(), "/test/", false, "/rest/UmAppDatabase")
        try {
            httpd.start()
            UMLog.l(UMLog.INFO, 0, "Started embedded HTTP server on port: ${httpd.listeningPort}")
        } catch (e: IOException) {
            UMLog.l(UMLog.CRITICAL, 0, "Could not start httpd server")
            throw RuntimeException("Could not start httpd server", e)
        }

    }

    override fun onDestroy() {
        httpd.stop()
        super.onDestroy()
    }

    companion object {

        val ANDROID_ASSETS_PATH = "/android-assets/"
    }
}
