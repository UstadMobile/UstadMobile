package com.ustadmobile.util.test.ext

import com.github.aakira.napier.DebugAntilog
import com.github.aakira.napier.Napier

fun Napier.baseDebugIfNotEnabled() {
    if(!Napier.isEnable(Napier.Level.DEBUG, null)) {
        Napier.base(DebugAntilog())
    }
}
