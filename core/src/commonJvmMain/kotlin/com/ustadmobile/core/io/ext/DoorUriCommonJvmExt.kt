package com.ustadmobile.core.io.ext

import com.ustadmobile.door.DoorUri
actual suspend fun DoorUri.isRemote(): Boolean {
    val prefix = this.uri.toString().substringBefore("//").lowercase()
    return prefix.startsWith("http:") || prefix.startsWith("https:")
}


