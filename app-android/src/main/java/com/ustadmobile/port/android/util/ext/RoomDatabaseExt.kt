package com.ustadmobile.port.android.util.ext

import androidx.room.InvalidationTracker
import androidx.room.RoomDatabase
import com.ustadmobile.door.DoorDatabaseReplicateWrapper
import com.ustadmobile.door.DoorDatabaseRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED

suspend fun <R> RoomDatabase.waitUntil2(tableNames: Set<String>, timeout: Long,
                                        getter: suspend () -> R?,
                                        checker: (R?) -> Boolean) : R? {
    val changeChannel = Channel<Boolean>(UNLIMITED)

    val invalidationObserver: InvalidationTracker.Observer = object: InvalidationTracker.Observer(tableNames.toTypedArray()) {
        override fun onInvalidated(tables: MutableSet<String>) {
            changeChannel.trySend(true)
        }
    }

    val realDb = if(this is DoorDatabaseReplicateWrapper) {
        this.realDatabase
    }else if(this is DoorDatabaseRepository) {
        this.db
    }else {
        this
    }

    withContext(Dispatchers.Main) {
        realDb.invalidationTracker.addObserver(invalidationObserver)
    }

    changeChannel.trySend(true)

    try {
        return withTimeout(timeout) {
            var result: R? = null
            for(obj in changeChannel) {
                result = getter()
                if(checker(result))
                    break
            }

            result
        }
    }finally {
        withContext(Dispatchers.Main) {
            invalidationTracker.removeObserver(invalidationObserver)
            changeChannel.close()
        }
    }
}

fun <R> RoomDatabase.waitUntil2Blocking(tableNames: Set<String>, timeout: Long,
                                        getter: suspend () -> R?,
                                        checker: (R?) -> Boolean) : R? = runBlocking {
    waitUntil2(tableNames, timeout, getter, checker)
}
