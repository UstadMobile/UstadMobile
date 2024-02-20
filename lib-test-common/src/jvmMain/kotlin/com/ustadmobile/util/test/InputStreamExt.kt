package com.ustadmobile.util.test

import java.io.EOFException
import java.io.IOException
import java.io.InputStream


/**
 * Copy/paste from lib-cache so it can be used within test-common
 */
internal fun InputStream.skipExactly(bytesToSkip: Long) {
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