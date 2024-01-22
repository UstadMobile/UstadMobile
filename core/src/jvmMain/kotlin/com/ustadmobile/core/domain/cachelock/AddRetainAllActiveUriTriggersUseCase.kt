package com.ustadmobile.core.domain.cachelock

import com.ustadmobile.door.DoorDbType
import com.ustadmobile.lib.db.entities.CacheLockJoin.Companion.STATUS_PENDING_DELETE
import com.ustadmobile.lib.db.entities.CacheLockJoin.Companion.STATUS_PENDING_CREATION
import com.ustadmobile.lib.db.entities.CacheLockJoin.Companion.TYPE_SERVER_RETENTION
import com.ustadmobile.lib.db.entities.CoursePicture
import com.ustadmobile.lib.db.entities.PersonPicture

/**
 * Create triggers that are used on the server side to retain all urls currently referenced by
 * entities like PersonPicture, CoursePicture, assignment submission, etc.
 *
 * On the client side, unless the user choose otherwise, any data referenced by such tables can be
 * eligible for eviction from the cache after it has been uploaded to the server.
 *
 * On the server side: we need to ensure that referenced urls are not evicted from the cache, and
 * we must only drop the retentionlock on the cache if/when the entity is updated and the url is
 * changed (e.g. the old url is no longer active).
 *
 * See additional notes on UpdateCacheLockJoinUseCase
 */
class AddRetainAllActiveUriTriggersUseCase {


    fun triggersForEntity(
        dbType: Int,
        tableName: String,
        tableId: Int,
        entityUidFieldName: String,
        uriFieldNames: List<String>,
    ): List<String> {
        return buildList {
            when(dbType) {
                DoorDbType.SQLITE -> {
                    uriFieldNames.forEach { uriFieldName ->
                        add(
                            """
                        CREATE TRIGGER IF NOT EXISTS Retain_${tableName}_Ins_$uriFieldName
                        AFTER INSERT ON $tableName
                        FOR EACH ROW WHEN NEW.$uriFieldName IS NOT NULL
                        BEGIN
                        INSERT OR REPLACE INTO CacheLockJoin(cljTableId, cljEntityUid, cljUrl, cljLockId, cljStatus, cljType)
                        VALUES($tableId, NEW.${entityUidFieldName}, NEW.${uriFieldName}, 0, $STATUS_PENDING_CREATION, $TYPE_SERVER_RETENTION);
                        END
                    """
                        )
                    }

                    uriFieldNames.forEach { uriFieldName ->
                        add(
                            """
                    CREATE TRIGGER IF NOT EXISTS Retain_${tableName}_Upd_${uriFieldName}_New
                    AFTER UPDATE ON $tableName
                    FOR EACH ROW WHEN NEW.${uriFieldName} != OLD.${uriFieldName} AND NEW.${uriFieldName} IS NOT NULL
                    BEGIN
                        INSERT OR REPLACE INTO CacheLockJoin(cljTableId, cljEntityUid, cljUrl, cljLockId, cljStatus, cljType)
                        VALUES($tableId, NEW.${entityUidFieldName}, NEW.${uriFieldName}, 0, $STATUS_PENDING_CREATION, $TYPE_SERVER_RETENTION);
                    END   
                """)
                        add(
                            """
                    CREATE TRIGGER IF NOT EXISTS Retain_${tableName}_Upd_${uriFieldName}_Old
                    AFTER UPDATE ON $tableName
                    FOR EACH ROW WHEN NEW.${uriFieldName} != OLD.${uriFieldName} AND OLD.${uriFieldName} IS NOT NULL
                    BEGIN
                        UPDATE CacheLockJoin 
                           SET cljStatus = $STATUS_PENDING_DELETE
                         WHERE cljTableId = $tableId
                           AND cljEntityUid = OLD.$entityUidFieldName
                           AND cljUrl = OLD.$uriFieldName;
                    END        
                    """.trimIndent()
                        )
                    }

                    uriFieldNames.forEach { uriFieldName ->
                        add("""
                    CREATE TRIGGER IF NOT EXISTS Retain_${tableName}_Del_${uriFieldName}
                    AFTER DELETE ON $tableName
                    FOR EACH ROW WHEN OLD.${uriFieldName} IS NOT NULL
                    BEGIN
                        UPDATE CacheLockJoin 
                           SET cljStatus = $STATUS_PENDING_DELETE
                         WHERE cljTableId = $tableId
                           AND cljEntityUid = OLD.$entityUidFieldName
                           AND cljUrl = OLD.$uriFieldName;
                    END       
                """.trimIndent())
                    }
                }
                DoorDbType.POSTGRES -> {
                    //Pending
                }
            }
        }
    }

    operator fun invoke(dbType: Int): List<String> {
        return buildList {
            addAll(
                triggersForEntity(
                    dbType = dbType,
                    tableName = "PersonPicture",
                    tableId = PersonPicture.TABLE_ID,
                    entityUidFieldName = "personPictureUid",
                    uriFieldNames = listOf("personPictureUri", "personPictureThumbnailUri")
                )
            )
            addAll(
                triggersForEntity(
                    dbType = dbType,
                    tableName = "CoursePicture",
                    tableId = CoursePicture.TABLE_ID,
                    entityUidFieldName = "coursePictureUid",
                    uriFieldNames = listOf("coursePictureUri", "coursePictureThumbnailUri")
                )
            )
        }

    }



}