package com.ustadmobile.core.util.ext

import android.util.Base64

actual fun String.base64StringToByteArray() = Base64.decode(this, Base64.NO_WRAP)
