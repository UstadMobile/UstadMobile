package com.ustadmobile.core.networkmanager

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import androidx.core.net.ConnectivityManagerCompat
import org.kodein.di.DI

class ConnectionManager(
        private val context: Context,
        di: DI
)  {

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

    fun stop() {
        cm.unregisterNetworkCallback(networkCallback)
    }


    private fun isMeteredConnection(): Boolean {
        return ConnectivityManagerCompat.isActiveNetworkMetered(cm)
    }


    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            val isMetered = isMeteredConnection()
        }

        override fun onLost(network: Network) {

        }

        override fun onUnavailable() {

        }
    }

}