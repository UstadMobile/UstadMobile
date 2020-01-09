package com.ustadmobile.core.util.ext

import java.util.*

actual fun ByteArray.encodeBase64() = Base64.getEncoder().encodeToString(this)
