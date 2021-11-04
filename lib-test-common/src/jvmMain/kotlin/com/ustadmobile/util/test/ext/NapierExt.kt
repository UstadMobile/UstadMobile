package com.ustadmobile.util.test.ext

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.LogLevel
import io.github.aakira.napier.Napier

fun Napier.baseDebugIfNotEnabled() {
    if(!isEnable(LogLevel.DEBUG, null)) {
        base(DebugAntilog())
    }
}
