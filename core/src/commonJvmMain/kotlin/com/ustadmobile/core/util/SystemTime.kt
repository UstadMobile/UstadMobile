package com.ustadmobile.core.util

actual fun getSystemTimeInMillis(): Long {
    return java.lang.System.currentTimeMillis()
}