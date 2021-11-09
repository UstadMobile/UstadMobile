package com.ustadmobile.core.networkmanager

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import com.ustadmobile.core.torrent.UstadCommunicationManager
import com.ustadmobile.core.util.getLocalIpAddress
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import java.util.concurrent.atomic.AtomicReference

class ConnectionManager(
        val context: Context,
        di: DI
)  {

    private val communicationManagerRef: AtomicReference<UstadCommunicationManager?> = AtomicReference(null)

    fun requireCommunicationManager(): UstadCommunicationManager {
        return communicationManagerRef.get() ?: throw IllegalStateException("no connectivity ref")
    }

    fun start(){
        val cm: ConnectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val builder: NetworkRequest.Builder = NetworkRequest.Builder()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            cm.registerDefaultNetworkCallback(networkCallback)
        } else {
            cm.registerNetworkCallback(
                    builder.build(), networkCallback
            )
        }
    }

    fun stop() {
        val cm: ConnectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.unregisterNetworkCallback(networkCallback)
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            val manager = communicationManagerRef.get() ?: UstadCommunicationManager(di.direct.instance())
            communicationManagerRef.set(manager)
            if(!manager.isRunning){
                manager.start(getLocalIpAddress())
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            communicationManagerRef.get()?.stop()
            communicationManagerRef.set(null)
        }

        override fun onUnavailable() {
            super.onUnavailable()
            communicationManagerRef.get()?.stop()
            communicationManagerRef.set(null)
        }
        
    }


}