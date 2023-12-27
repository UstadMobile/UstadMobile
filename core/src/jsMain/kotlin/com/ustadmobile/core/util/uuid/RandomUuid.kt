package com.ustadmobile.core.util.uuid

import web.crypto.crypto

actual fun randomUuidAsString(): String {
    return crypto.randomUUID()
}
