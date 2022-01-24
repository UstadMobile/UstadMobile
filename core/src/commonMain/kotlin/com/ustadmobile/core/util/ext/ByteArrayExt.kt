package com.ustadmobile.core.util.ext

/**
 * To ensure consistency, Base64 encoding **must** be done with NO_WRAP
 */
expect fun ByteArray.encodeBase64(): String
