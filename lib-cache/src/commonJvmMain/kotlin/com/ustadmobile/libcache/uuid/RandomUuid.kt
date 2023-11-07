package com.ustadmobile.libcache.uuid

import java.util.UUID

actual fun randomUuid(): String {
    return UUID.randomUUID().toString()
}
