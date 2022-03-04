package com.ustadmobile.core.contentjob

import com.ustadmobile.core.db.UmAppDatabase

/**
 * Concurrent updates to ContentJobItem can cause a transaction deadlock on postgres. Therefor
 * all updates to ContentJobItem need to be done in a Mutex (e.g. progress, etc) when there is a
 * possibility that multiple ContentJobs are running concurrently (which is normal).
 *
 * This is normally implemented by ContentJobRunner, but lighter implementations can be used for
 * testing purposes.
 */
interface ContentJobItemTransactionRunner {

    /**
     * Run a database transaction inside a mutex to ensure one-at-a-time modifications to
     * ContentJobItem
     *
     * @param block transaction block to run
     */
    suspend fun <R> withContentJobItemTransaction(block: suspend (UmAppDatabase) -> R): R

}
