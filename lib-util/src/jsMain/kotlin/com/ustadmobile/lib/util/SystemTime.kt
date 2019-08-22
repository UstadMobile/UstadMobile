package com.ustadmobile.lib.util

import kotlin.js.Date

actual fun getSystemTimeInMillis(): Long {
    return Date().getTime().toLong()
}