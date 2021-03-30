package com.ustadmobile.util.test.ext

import com.github.aakira.napier.DebugAntilog
import com.github.aakira.napier.LogLevel
import com.github.aakira.napier.Napier

fun Napier.baseDebugIfNotEnabled() {
    if(!isEnable(LogLevel.DEBUG, null)) {
        base(DebugAntilog())
    }
}
