package com.ustadmobile.door

/**
 * Altering the SQLite sequence numbers is not enough.
 */
expect class DoorSqlitePrimaryKeyManager(repo: DoorDatabaseSyncRepository) {

    suspend fun getAndIncrementSqlitePk(tableId: Int, increment: Int): Long

}