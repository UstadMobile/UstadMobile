package com.ustadmobile.libcache.db

import com.ustadmobile.door.DoorDatabaseCallbackStatementList
import com.ustadmobile.door.DoorSqlDatabase

/**
 * When database is opened (e.g. app has restarted) any previously known neighbors should be cleared
 */
class ClearNeighborsCallback: DoorDatabaseCallbackStatementList {

    override fun onCreate(db: DoorSqlDatabase): List<String> {
        return emptyList()
    }

    override fun onOpen(db: DoorSqlDatabase): List<String> {
        return listOf("DELETE FROM NeighborCache")
    }
}