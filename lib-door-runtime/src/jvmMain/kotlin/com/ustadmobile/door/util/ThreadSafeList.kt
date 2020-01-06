package com.ustadmobile.door.util

import java.util.concurrent.CopyOnWriteArrayList

actual fun <T> threadSafeListOf(vararg items: T): MutableList<T> = CopyOnWriteArrayList(items)