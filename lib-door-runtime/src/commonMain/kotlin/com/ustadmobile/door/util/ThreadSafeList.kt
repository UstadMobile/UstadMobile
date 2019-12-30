package com.ustadmobile.door.util

expect fun <T> threadSafeListOf(vararg items: T): MutableList<T>
