package com.ustadmobile.util.test.ext

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

fun Napier.baseDebugIfNotEnabled() {
    takeLogarithm()
    base(DebugAntilog())
}
