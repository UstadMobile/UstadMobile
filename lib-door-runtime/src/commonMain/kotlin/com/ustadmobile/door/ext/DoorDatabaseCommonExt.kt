package com.ustadmobile.door.ext

import com.ustadmobile.door.DoorDatabase
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.SyncableDoorDatabase
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

/**
 * Extension property that will be true if this database is both syncable and the primary (eg. server)
 * instance, false otherwise
 */
val DoorDatabase.syncableAndPrimary: Boolean
        get() = (this as? SyncableDoorDatabase)?.master ?: false

/**
 * If this DoorDatabase represents a repository, then run the given block on the repository first
 * with a timeout. If the operation times out (e.g. due to network issues), then the operation will
 * be retried on the database. This can be useful to lookup values from the repo if possible with a
 * fallback to using the local database if this takes too long.
 *
 * If this DoorDatabase instance represents a database itself, then the operation will run
 * immediately on the database (without timeout).
 *
 * Example:
 * val entity = myRepo.withRepoTimeout(5000) {
 *     it.someDao.someQuery()
 * }
 *
 * @param timeMillis the timeout (in milliseconds)
 * @param block a function block that represents the function to run. The supplied parameter must be
 * used as the database for the fallback to running from the
 */
@Suppress("UNCHECKED_CAST")
suspend fun <T : DoorDatabase, R> T.withRepoTimeout(timeMillis: Long, block: suspend (T) -> R) : R {
    if(this is DoorDatabaseRepository) {
        try {
            return withTimeout(timeMillis) {
                block(this@withRepoTimeout)
            }
        }catch(e: TimeoutCancellationException) {
            return block(this.db as T)
        }
    }else {
        return block(this)
    }
}
