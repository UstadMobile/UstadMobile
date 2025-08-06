package com.ustadmobile.libcache.db

import com.ustadmobile.door.DoorDatabaseCallbackStatementList
import com.ustadmobile.door.DoorSqlDatabase

/**
 * When a new entry is added to the Cache the DistributedCacheHashTable needs to inform neighbors.
 *
 * The generated trigger allows DistributedCacheHashTable to simply monitor the NewCacheEntry table
 */
class AddNewEntryTriggerCallback: DoorDatabaseCallbackStatementList {

    override fun onCreate(db: DoorSqlDatabase): List<String> {
        return listOf(ADD_TRIGGER_SQL)
    }

    override fun onOpen(db: DoorSqlDatabase): List<String> {
        //do nothing
        return emptyList()
    }

    companion object {

        const val ADD_TRIGGER_SQL = """
            CREATE TRIGGER NewCacheEntryTrigger 
            AFTER INSERT ON CacheEntry
            BEGIN
               INSERT OR REPLACE INTO NewCacheEntry(cacheEntryKey, nceUrl) VALUES(NEW.key, NEW.url);
            END
        """

    }
}