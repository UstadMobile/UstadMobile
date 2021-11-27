package com.ustadmobile.core.networkmanager

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import androidx.core.net.ConnectivityManagerCompat
import com.ustadmobile.core.torrent.CommunicationManagerListener
import com.ustadmobile.core.torrent.UstadCommunicationManager
import com.ustadmobile.core.util.getLocalIpAddress
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference

class ConnectionManager(
        private val context: Context,
        di: DI
)  {

    private val communicationManagerRef: AtomicReference<UstadCommunicationManager?> = AtomicReference(null)

    private val listeners : MutableList<CommunicationManagerListener> = CopyOnWriteArrayList()

    fun requireCommunicationManager(): UstadCommunicationManager {
        return communicationManagerRef.get() ?: throw IllegalStateException("no connectivity ref")
    }

    private val cm: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun start(){

        val builder: NetworkRequest.Builder = NetworkRequest.Builder()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            cm.registerDefaultNetworkCallback(networkCallback)
        } else {
            cm.registerNetworkCallback(
                    builder.build(), networkCallback
            )
        }
    }

    fun addCommunicationManagerListener(listener: CommunicationManagerListener){
        listeners += listener
        start()
    }

    fun stop() {
        cm.unregisterNetworkCallback(networkCallback)
    }

    private fun stopCommunicationManager(){
        val oldManager: UstadCommunicationManager = communicationManagerRef.get() ?: return
        oldManager.stop()
        communicationManagerRef.set(null)
    }

    private fun fireNewCommunicationManagerEvent(meteredNetwork: Boolean){
        listeners.forEach {
            it.onCommunicationManagerChanged(meteredNetwork)
        }
    }

    private fun isMeteredConnection(): Boolean {
        return ConnectivityManagerCompat.isActiveNetworkMetered(cm)
    }


    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            stopCommunicationManager()
            val isMetered = isMeteredConnection()
            val manager = UstadCommunicationManager(di.direct.instance())
                    .also {
                        communicationManagerRef.set(it)
                    }
            manager.start(getLocalIpAddress(), null)
            fireNewCommunicationManagerEvent(isMetered)
        }

        override fun onLost(network: Network) {
            stopCommunicationManager()
        }

        override fun onUnavailable() {
            stopCommunicationManager()
        }
    }

}