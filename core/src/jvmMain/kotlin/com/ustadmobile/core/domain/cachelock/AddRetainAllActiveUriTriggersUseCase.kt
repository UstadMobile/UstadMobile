package com.ustadmobile.core.domain.cachelock

import com.ustadmobile.core.util.ext.truncate
import com.ustadmobile.door.DoorDbType
import com.ustadmobile.lib.db.entities.CacheLockJoin.Companion.STATUS_PENDING_DELETE
import com.ustadmobile.lib.db.entities.CacheLockJoin.Companion.STATUS_PENDING_CREATION
import com.ustadmobile.lib.db.entities.CacheLockJoin.Companion.TYPE_SERVER_RETENTION
import com.ustadmobile.lib.db.entities.ContentEntryPicture2
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmissionFile
import com.ustadmobile.lib.db.entities.CourseBlockPicture
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


    fun triggersForEntityV1(
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
                    uriFieldNames.forEach { fieldName ->
                        // function name is limited to 63 bytes Create cache lock join...
                        val createFnName = "retain_c_clj_${tableId}_${fieldName.truncate(24, null)}"
                        val deleteFnName = "retain_d_clj_${tableId}_${fieldName.truncate(24, null)}"

                        add("""
                            CREATE OR REPLACE FUNCTION $createFnName() RETURNS TRIGGER AS ${'$'}${'$'}
                            BEGIN
                            INSERT INTO CacheLockJoin(cljTableId, cljEntityUid, cljUrl, cljLockId, cljStatus, cljType)
                            VALUES($tableId, NEW.$entityUidFieldName, NEW.$fieldName, 0, $STATUS_PENDING_CREATION, $TYPE_SERVER_RETENTION);
                            RETURN NEW;
                            END ${'$'}${'$'} LANGUAGE plpgsql
                        """)

                        //Delete cache lock join
                        add("""
                            CREATE OR REPLACE FUNCTION $deleteFnName() RETURNS TRIGGER AS ${'$'}${'$'}
                            BEGIN
                            UPDATE CacheLockJoin 
                               SET cljStatus = $STATUS_PENDING_DELETE
                             WHERE cljTableId = $tableId
                               AND cljEntityUid = OLD.$entityUidFieldName
                               AND cljUrl = OLD.$fieldName;
                            RETURN OLD;
                            END ${'$'}${'$'} LANGUAGE plpgsql   
                        """)

                        add("""
                            CREATE TRIGGER ${createFnName}_ins_t
                            AFTER INSERT ON $tableName
                            FOR EACH ROW
                            WHEN (NEW.$fieldName IS NOT NULL)
                            EXECUTE FUNCTION $createFnName();
                        """)

                        add("""
                            CREATE TRIGGER ${createFnName}_upd_t
                            AFTER UPDATE ON $tableName
                            FOR EACH ROW
                            WHEN (NEW.$fieldName IS DISTINCT FROM OLD.$fieldName AND OLD.$fieldName IS NOT NULL)
                            EXECUTE FUNCTION $createFnName();
                        """)


                        add("""
                            CREATE TRIGGER ${deleteFnName}_upd_t
                            AFTER UPDATE ON $tableName
                            FOR EACH ROW
                            WHEN (NEW.$fieldName IS DISTINCT FROM OLD.$fieldName AND NEW.$fieldName IS NOT NULL)
                            EXECUTE FUNCTION $deleteFnName();
                        """)
                    }
                }
            }
        }
    }

    operator fun invoke(dbType: Int): List<String> {
        return buildList {
            addAll(
                triggersForEntityV1(
                    dbType = dbType,
                    tableName = "PersonPicture",
                    tableId = PersonPicture.TABLE_ID,
                    entityUidFieldName = "personPictureUid",
                    uriFieldNames = listOf("personPictureUri", "personPictureThumbnailUri")
                )
            )
            addAll(
                triggersForEntityV1(
                    dbType = dbType,
                    tableName = "CoursePicture",
                    tableId = CoursePicture.TABLE_ID,
                    entityUidFieldName = "coursePictureUid",
                    uriFieldNames = listOf("coursePictureUri", "coursePictureThumbnailUri")
                )
            )
            addAll(
                triggersForEntityV1(
                    dbType  = dbType,
                    tableName = "CourseAssignmentSubmissionFile",
                    tableId = CourseAssignmentSubmissionFile.TABLE_ID,
                    entityUidFieldName = "casaUid",
                    uriFieldNames = listOf("casaUri"),
                )
            )
            addAll(
                triggersForEntityV1(
                    dbType  = dbType,
                    tableName = "CourseBlockPicture",
                    tableId = CourseBlockPicture.TABLE_ID,
                    entityUidFieldName = "cbpUid",
                    uriFieldNames = listOf("cbpPictureUri", "cbpThumbnailUri"),
                )
            )
            addAll(
                triggersForEntityV1(
                    dbType  = dbType,
                    tableName = "ContentEntryPicture2",
                    tableId = ContentEntryPicture2.TABLE_ID,
                    entityUidFieldName = "cepUid",
                    uriFieldNames = listOf("cepPictureUri", "cepThumbnailUri"),
                )
            )
        }

    }



}