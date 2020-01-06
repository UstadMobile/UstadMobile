package com.ustadmobile.core.util.ext

import java.util.*

actual fun String.base64StringToByteArray() = Base64.getDecoder().decode(this)