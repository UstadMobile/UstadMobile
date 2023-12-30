package com.ustadmobile.core.util.uuid

import com.ustadmobile.core.wrappers.uuid.uuidv4
import com.ustadmobile.core.wrappers.window.isSecureContext
import web.crypto.crypto
import web.window.window

/**
 * In a secure context (https and localhost), we can use just use crypt.randomUUID(), however, if
 * the system is being run on a local IP address etc. for testing, we need to use the uuid package
 */
actual fun randomUuidAsString(): String {
    return if(window.isSecureContext)
        crypto.randomUUID()
    else
        uuidv4()
}
