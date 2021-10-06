package com.ustadmobile.core.networkmanager

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import com.turn.ttorrent.client.CommunicationManager
import com.ustadmobile.core.util.getLocalIpAddress
import org.kodein.di.DI
import org.kodein.di.instance

class ConnectionManager(val context: Context,
                        di: DI)  {

    val communicationManager: CommunicationManager by di.instance()

    fun startNetworkCallback(){
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

    fun stopNetworkCallback() {
        val cm: ConnectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.unregisterNetworkCallback(networkCallback)
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            communicationManager.start(getLocalIpAddress())
        }

        override fun onUnavailable() {
            super.onUnavailable()
            communicationManager.stop()
        }
        
    }


}