package com.ustadmobile.core.impl

actual fun dumpException(e: Throwable) = console.error(e)