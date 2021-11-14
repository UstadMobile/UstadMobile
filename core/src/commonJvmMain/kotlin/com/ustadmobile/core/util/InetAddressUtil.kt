package com.ustadmobile.core.util

import java.net.InetAddress
import java.net.NetworkInterface

fun getLocalIpAddress(): Array<InetAddress> {
    val acceptableAddress = mutableListOf<InetAddress>()
    NetworkInterface.getNetworkInterfaces().toList().forEach { network ->
        acceptableAddress.addAll(network.inetAddresses.toList().filter { address -> !address.isLoopbackAddress })
    }
    return acceptableAddress.toTypedArray()
}