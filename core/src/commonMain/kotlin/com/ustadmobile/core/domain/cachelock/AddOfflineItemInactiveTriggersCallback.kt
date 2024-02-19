package com.ustadmobile.core.domain.cachelock

import com.ustadmobile.door.DoorDatabaseCallbackStatementList
import com.ustadmobile.door.DoorSqlDatabase
import com.ustadmobile.lib.db.entities.CacheLockJoin

/**
 * Handle when an OfflineItem is made inactive (e.g. the user has unselected an item). This will
 * Update the CacheLockJoin table to set any related CacheLockJoin entities to the status of
 * PENDING_DELETE . This will be picked up by UpdateCacheLockJoinUseCase which will remove the
 * retention locks from the cache.
 */
class AddOfflineItemInactiveTriggersCallback: DoorDatabaseCallbackStatementList {

    override fun onCreate(db: DoorSqlDatabase): List<String> {
        return listOf(
            """
                CREATE TRIGGER IF NOT EXISTS offline_item_inactive_trig 
                AFTER UPDATE ON OfflineItem
                FOR EACH ROW WHEN NEW.oiActive = 0 AND OLD.oiActive = 1
                BEGIN 
                UPDATE CacheLockJoin
                   SET cljStatus = ${CacheLockJoin.STATUS_PENDING_DELETE}
                 WHERE cljOiUid = NEW.oiUid;  
                END
            """
        )
    }

    override fun onOpen(db: DoorSqlDatabase): List<String> {
        return emptyList()
    }
}