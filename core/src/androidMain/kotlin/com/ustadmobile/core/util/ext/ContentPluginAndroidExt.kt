package com.ustadmobile.core.util.ext

import android.content.Context
import android.net.wifi.WifiManager
import com.ustadmobile.core.contentjob.ContentPlugin

actual suspend fun ContentPlugin.withWifiLock(context: Any, block: suspend () -> Unit){
    var lock: WifiManager.WifiLock? = null
    try{
        val wifiManager = (context as Context).applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        lock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "ContainerDownload")
        lock.acquire()
        block.invoke()
    }finally {
        lock?.release()
    }


}