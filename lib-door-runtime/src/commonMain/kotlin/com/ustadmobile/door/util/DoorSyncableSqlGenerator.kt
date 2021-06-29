package com.ustadmobile.door.util

/**
 * Functions that generate Syncable SQL live here. They can be used by the annotation processor
 * itself and by migrations.
 *
 * Functions will end with the V(number) and will never be changed. If the SQL being generated is
 * updated, a new function will be added with an incremented function version number in the name.
 * This is required to ensure migrations remain consistent.
 */
object DoorSyncableSqlGenerator {

    /**
     * Generates the SQL for adding a SQLite Insert trigger for a syncable entity.
     */
    fun generateSyncableEntityInsertTriggersSqliteV1(tableName: String, tableId: Int, pkFieldName: String,
                                                   primaryCsnFieldName: String, localCsnFieldName: String) : List<String>{
        return listOf(
            """
            CREATE TRIGGER INS_LOC_${tableId}
            AFTER INSERT ON $tableName
            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
                NEW.$localCsnFieldName = 0)
            BEGIN
                UPDATE $tableName
                SET $primaryCsnFieldName = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = $tableId)
                WHERE $pkFieldName = NEW.$pkFieldName;
                
                UPDATE SqliteChangeSeqNums
                SET sCsnNextPrimary = sCsnNextPrimary + 1
                WHERE sCsnTableId = $tableId;
            END
            """,
            """
            CREATE TRIGGER INS_PRI_${tableId}
            AFTER INSERT ON $tableName
            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
                NEW.$primaryCsnFieldName = 0)
            BEGIN
                UPDATE $tableName
                SET $primaryCsnFieldName = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = $tableId)
                WHERE $pkFieldName = NEW.$pkFieldName;
                
                UPDATE SqliteChangeSeqNums
                SET sCsnNextPrimary = sCsnNextPrimary + 1
                WHERE sCsnTableId = $tableId;
                
                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
                SELECT ${tableId}, NEW.${pkFieldName}, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
            END
        """
        )
    }
}