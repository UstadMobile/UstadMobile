package com.ustadmobile.core.util

import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface

fun getLocalIpAddress(): InetAddress?{
    NetworkInterface.getNetworkInterfaces().toList().forEach { network ->
        network.inetAddresses.toList().forEach { inetAddress ->
            if(!inetAddress.isLoopbackAddress && inetAddress is Inet4Address){
                return inetAddress
            }
        }
    }
    return null
}