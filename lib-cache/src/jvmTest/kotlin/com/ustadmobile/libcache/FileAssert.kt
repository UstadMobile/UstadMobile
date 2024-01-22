package com.ustadmobile.libcache

import java.io.File

/**
 * The temp directory might not be immediately emptied, hence apply a timeout.
 */
fun File.assertTempDirectoryIsEmptied(
    timeout: Long = 1_000,
    checkInterval: Int = 100,
) {
    var timeCount = 0
    while(timeCount < timeout) {
        if(list()!!.isEmpty())
            return
        Thread.sleep(checkInterval.toLong())
        timeCount += checkInterval
    }

    throw IllegalStateException("Asserting $this is empty: still not empty after $timeout ms")
}