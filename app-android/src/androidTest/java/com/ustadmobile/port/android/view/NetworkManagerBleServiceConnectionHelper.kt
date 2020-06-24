package com.ustadmobile.port.android.view

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.ServiceTestRule
import com.ustadmobile.sharedse.network.NetworkManagerBle
import com.ustadmobile.sharedse.network.NetworkManagerBleAndroidService
import kotlinx.coroutines.CompletableDeferred
import org.junit.Rule

abstract class NetworkManagerBleServiceConnectionHelper{

    @JvmField
    @Rule
    val serviceRule = ServiceTestRule()

    val networkManagerBle = CompletableDeferred<NetworkManagerBle>()

    val context: Application = ApplicationProvider.getApplicationContext()

    private val bleServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            serviceRule.unbindService()
        }

        override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
            val serviceVal = (service as NetworkManagerBleAndroidService.LocalServiceBinder).service
            serviceVal.runWhenNetworkManagerReady{
                val networkManagerBleVal = serviceVal.networkManagerBle!!
                networkManagerBle.complete(networkManagerBleVal)
            }
        }

    }

    fun bindService(){
        serviceRule.bindService(Intent(context, NetworkManagerBleAndroidService::class.java),
                bleServiceConnection, Context.BIND_AUTO_CREATE)
    }
}