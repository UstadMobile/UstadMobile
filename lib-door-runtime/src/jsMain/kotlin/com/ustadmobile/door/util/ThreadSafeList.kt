package com.ustadmobile.door.util

actual fun <T> threadSafeListOf(vararg items: T) = mutableListOf(*items)
