package com.ustadmobile.port.android.view

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.AsyncTask
import android.os.Bundle
import android.os.IBinder

import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.port.android.netwokmanager.EmbeddedHttpdService
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.port.sharedse.impl.http.MountedContainerResponder
import com.ustadmobile.port.sharedse.util.RunnableQueue

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Created by mike on 2/15/18.
 */

abstract class ZippedContentActivity : UstadBaseActivity() {

    private val httpdRef = AtomicReference<EmbeddedHTTPD>()

    private val httpdBound = AtomicBoolean(false)

    private val runWhenConnectedQueue = RunnableQueue()

    @Volatile
    private var mountedPath: String? = null

    private val httpdServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            httpdRef.set((service as EmbeddedHttpdService.LocalServiceBinder).httpd)
            httpdBound.set(true)
            runWhenConnectedQueue.setReady(true)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            runWhenConnectedQueue.setReady(false)
            httpdBound.set(false)
        }
    }

    private class MountZipAsyncTask(callback: UmCallback<*>, private val httpd: EmbeddedHTTPD) : AsyncTask<String, Void, String>() {

        private val callback: UmCallback<String>

        init {
            this.callback = callback as UmCallback<String>
        }

        public override fun doInBackground(vararg strings: String): String {
            val mountedUri = httpd.mountZipOnHttp(strings[0], null)
            return UMFileUtil.joinPaths(httpd.localHttpUrl,
                    mountedUri)
        }

        override fun onPostExecute(mountedPath: String) {
            callback.onSuccess(mountedPath)
        }
    }

    private class MountContainerAsyncTask(callback: UmCallback<*>, private val httpd: EmbeddedHTTPD) : AsyncTask<Long, Void, String>() {
        private val callback: UmCallback<String>

        init {
            this.callback = callback as UmCallback<String>
        }

        override fun doInBackground(vararg p0: Long?): String {
            val mountedUri = httpd.mountContainer(p0[0]!!, null)
            return UMFileUtil.joinPaths(httpd.localHttpUrl,
                    mountedUri)
        }

        override fun onPostExecute(mountedPath: String) {
            callback.onSuccess(mountedPath)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val httpdServiceIntent = Intent(this, EmbeddedHttpdService::class.java)
        bindService(httpdServiceIntent, httpdServiceConnection,
                Context.BIND_AUTO_CREATE or Context.BIND_ADJUST_WITH_ACTIVITY)
    }


    override fun onDestroy() {
        if (httpdBound.get()) {
            unbindService(httpdServiceConnection)
        }

        super.onDestroy()
    }

    fun mountZip(zipUri: String, callback: UmCallback<String>) {
        runWhenConnectedQueue.runWhenReady { MountZipAsyncTask(callback, httpdRef.get()).doInBackground(zipUri) }
    }

    fun mountContainer(containerUid: Long, callback: UmCallback<String>) {
        runWhenConnectedQueue.runWhenReady { MountContainerAsyncTask(callback, httpdRef.get()).execute(containerUid) }
    }

    fun unmountContainer(mountedUrl: String) {
        //note: use -1 so we don't chop off first ./ included in local httpurl from the mounted path
        val mountedPath = mountedUrl.substring(httpdRef.get().localHttpUrl.length - 1) + MountedContainerResponder.URI_ROUTE_POSTFIX
        httpdRef.get().unmountContainer(mountedPath)
    }

    fun unmountZipFromHttp(mountedPath: String) {
        httpdRef.get().unmountZip(mountedPath)
    }

    protected fun runWhenHttpdReady(runnable: Runnable) {
        runWhenConnectedQueue.runWhenReady(runnable)
    }


}
