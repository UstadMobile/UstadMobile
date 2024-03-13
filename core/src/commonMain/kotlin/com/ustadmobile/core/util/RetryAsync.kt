package com.ustadmobile.core.util

import kotlinx.coroutines.delay

/**
 * Generic retry function
 */
suspend fun <R> retryAsync(
    interval: Long = 200,
    maxAttempts: Int = 10,
    block: suspend () -> R,
) : R {
    for(i in 0 until maxAttempts) {
        try {
            return block()
        }catch(e: Throwable) {
            if(i < maxAttempts - 1) {
                delay(interval)
            }else {
                throw IllegalStateException("RetryAsync: exceeded maxattempts ($maxAttempts)", e)
            }
        }
    }

    throw IllegalStateException("retryAsync: should not be here")
}