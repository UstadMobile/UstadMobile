package com.ustadmobile.core.impl

import kotlin.Throwable

actual fun dumpException(e: Throwable){
    e.printStackTrace()
}