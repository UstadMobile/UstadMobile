package com.ustadmobile.libcache.distributed

import com.ustadmobile.xxhashkmp.XXStringHasher
import java.net.InetAddress

fun XXStringHasher.neighborUid(ipAddress: String, udpPort: Int): Long {
    return hash("$ipAddress:$udpPort")
}

fun XXStringHasher.neighborUid(ipAddress: InetAddress, udpPort: Int): Long {
    return neighborUid(ipAddress.hostAddress, udpPort)
}
