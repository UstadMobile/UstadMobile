package com.ustadmobile.core.util.digest

import java.security.MessageDigest

actual fun Digester(algoName: String) : Digester {
    return DigesterJvm(MessageDigest.getInstance(algoName))
}
