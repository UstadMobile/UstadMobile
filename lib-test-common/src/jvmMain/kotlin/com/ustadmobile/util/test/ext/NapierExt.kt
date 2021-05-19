package com.ustadmobile.util.test.ext

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

fun Napier.baseDebugIfNotEnabled() {
    if(!isEnable(Napier.Level.DEBUG, null)) {
        base(DebugAntilog())
    }
}
