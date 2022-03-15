package com.ustadmobile.lib.util

actual fun <T> copyOnWriteListOf(vararg items: T) = mutableListOf(*items)