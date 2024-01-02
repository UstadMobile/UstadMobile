package com.ustadmobile.libcache.io

import java.io.EOFException
import java.io.IOException
import java.io.InputStream

/**
 * Exactly the same as skipNBytes on "newer" JDK, backported for Android. Should work with
 * desugaring... but that wasn't cooperating as expected.
 */
fun InputStream.skipExactly(bytesToSkip: Long) {
    var remaining = bytesToSkip

    while(true) {
        if(remaining > 0) {
            val numSkipped = skip(remaining)
            @Suppress("ConvertTwoComparisonsToRangeCheck")
            if(numSkipped > 0 && numSkipped <= remaining) {
                remaining -= numSkipped
                continue
            }

            if(numSkipped == 0L) {
                if(read() == -1)
                    throw EOFException()

                remaining--
                continue
            }

            throw IOException("Unable to skip exactly")
        }

        return
    }
}
