package com.ustadmobile.core.util.ext

import android.util.Base64

actual fun ByteArray.encodeBase64() = Base64.encodeToString(this, Base64.DEFAULT)
