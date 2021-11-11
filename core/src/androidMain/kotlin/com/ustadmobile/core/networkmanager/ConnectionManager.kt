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
        val context: Context,
        di: DI
)  {

    private val communicationManagerRef: AtomicReference<UstadCommunicationManager?> = AtomicReference(null)

    private val listeners : MutableList<CommunicationManagerListener> = CopyOnWriteArrayList()

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

    fun addCommunicationManagerListener(listener: CommunicationManagerListener){
        listeners += listener
    }

    fun removeCommunicationManagerListener(listener: CommunicationManagerListener){
        listeners -= listener
    }

    fun stop() {
        val cm: ConnectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.unregisterNetworkCallback(networkCallback)
    }

    fun removeOldCommunicationManager(meteredNetwork: Boolean){
        val oldManager: UstadCommunicationManager = communicationManagerRef.get() ?: return
        oldManager.stop()
        communicationManagerRef.set(null)
        listeners.forEach {
            it.onCommunicationManagerChanged(null, meteredNetwork)
        }
    }

    fun isMeteredConnection(): Boolean {
        val cm: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        return ConnectivityManagerCompat.isActiveNetworkMetered(cm)
    }


    private val networkCallback = object : ConnectivityManager.NetworkCallback() {


        override fun onAvailable(network: Network) {
            val isMetered = isMeteredConnection()
            removeOldCommunicationManager(isMetered)
            val manager = UstadCommunicationManager(di.direct.instance())
                    .also {
                        communicationManagerRef.set(it)
                    }
            manager.start(getLocalIpAddress())
            listeners.forEach {
                it.onCommunicationManagerChanged(manager, isMetered)
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            removeOldCommunicationManager(isMeteredConnection())
        }

        override fun onUnavailable() {
            super.onUnavailable()
            removeOldCommunicationManager(isMeteredConnection())
        }
    }

}