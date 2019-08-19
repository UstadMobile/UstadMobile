package com.ustadmobile.port.android.view

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.sharedse.network.EmbeddedHttpdService


abstract class ContainerContentActivity: UstadBaseActivity() {

    protected var httpContainer: EmbeddedHTTPD? = null

    private val httpdServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            httpContainer = ((service as EmbeddedHttpdService.LocalServiceBinder).getHttpd())
            onHttpdConnected(httpContainer!!)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            httpContainer = null
        }
    }

    protected open fun onHttpdConnected(httpd: EmbeddedHTTPD) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val httpdServiceIntent = Intent(this, EmbeddedHttpdService::class.java)
        bindService(httpdServiceIntent, httpdServiceConnection,
                Context.BIND_AUTO_CREATE or Context.BIND_ADJUST_WITH_ACTIVITY)
    }

    override fun onDestroy() {
        super.onDestroy()

        if(httpContainer != null)
            unbindService(httpdServiceConnection)
    }
}