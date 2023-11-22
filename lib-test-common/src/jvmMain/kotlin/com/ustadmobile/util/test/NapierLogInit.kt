package com.ustadmobile.util.test

import com.ustadmobile.core.util.NapierAntilogJvm
import io.github.aakira.napier.Napier
import java.util.logging.Level

private var napierInitDone = false

/**
 * Initialize Napier logging if not already done. Calling this repeatedly within the same JVM run
 * causes log entries to be repeated. Napier.takeLogarithm does not seem to work.
 */
fun initNapierLog() {
    if(!napierInitDone) {
        Napier.base(NapierAntilogJvm(Level.FINEST))
        napierInitDone = true
    }
}

