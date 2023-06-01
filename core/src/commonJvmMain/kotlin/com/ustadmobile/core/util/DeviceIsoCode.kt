package com.ustadmobile.core.util

import java.util.Locale

actual fun deviceIsoCode(): String {

    val locale = Locale.getDefault()
    return locale.country
}