package com.ustadmobile.core.contentjob

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.withDoorTransactionAsync
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class DummyContentJobItemTransactionRunner(
    private val db: UmAppDatabase
) : ContentJobItemTransactionRunner {

    private val mutex = Mutex()

    override suspend fun <R> withContentJobItemTransaction(block: suspend (UmAppDatabase) -> R): R {
        return mutex.withLock {
            db.withDoorTransactionAsync(UmAppDatabase::class, block)
        }
    }
}