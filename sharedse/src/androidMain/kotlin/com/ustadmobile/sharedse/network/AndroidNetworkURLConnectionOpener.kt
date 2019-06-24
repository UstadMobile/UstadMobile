package com.ustadmobile.sharedse.network

import android.annotation.TargetApi
import android.net.Network

import com.ustadmobile.port.sharedse.networkmanager.URLConnectionOpener

import java.io.IOException
import java.net.URL
import java.net.URLConnection

/**
 * Created by mike on 2/8/18.
 */
@TargetApi(21)
class AndroidNetworkURLConnectionOpener(private val network: Network) : URLConnectionOpener {

    @Throws(IOException::class)
    override fun openConnection(url: URL): URLConnection {
        return network.openConnection(url)
    }
}
