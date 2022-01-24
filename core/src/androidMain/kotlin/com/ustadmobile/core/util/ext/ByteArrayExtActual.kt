package com.ustadmobile.core.util.ext

import android.util.Base64

//Android automatically appends a newline character to the encoded string result. This needs to be
// removed to ensure consistency with JVM.
actual fun ByteArray.encodeBase64() = Base64.encodeToString(this, Base64.NO_WRAP).trim()
