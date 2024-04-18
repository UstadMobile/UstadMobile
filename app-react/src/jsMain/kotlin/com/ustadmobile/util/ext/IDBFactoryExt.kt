package com.ustadmobile.util.ext

import kotlinx.coroutines.CompletableDeferred
import web.events.EventHandler
import web.idb.IDBFactory

/**
 * Delete the IndexedDb with a given name, wrapped as a suspend function.
 */
suspend fun IDBFactory.deleteDatabaseAsync(name: String) {
    val completable = CompletableDeferred<Unit>()
    val request = deleteDatabase(name)
    request.onsuccess = EventHandler { completable.complete(Unit) }
    request.onerror = EventHandler {
        completable.completeExceptionally(Exception("Error deleting database: $name"))
    }
    completable.await()
}
