package com.ustadmobile.lib.util

import java.util.concurrent.CopyOnWriteArrayList

actual fun <T> copyOnWriteListOf(vararg items: T) = CopyOnWriteArrayList<T>(items) as MutableList<T>
