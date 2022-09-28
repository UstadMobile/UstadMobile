package com.ustadmobile.core.db.dao

import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.UserSession
import com.ustadmobile.lib.db.entities.VerbDisplay
import com.ustadmobile.lib.db.entities.VerbEntity
import kotlin.js.JsName

@DoorDao
@Repository
expect abstract class VerbDao : BaseDao<VerbEntity> {

    @Query("""
     REPLACE INTO VerbEntityReplicate(vePk, veDestination)
      SELECT DISTINCT VerbEntity.verbUid AS vePk,
             :newNodeId AS veDestination
        FROM VerbEntity
       WHERE VerbEntity.verbLct != COALESCE(
             (SELECT veVersionId
                FROM VerbEntityReplicate
               WHERE vePk = VerbEntity.verbUid
                 AND veDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(vePk, veDestination) DO UPDATE
             SET vePending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([VerbEntity::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
    REPLACE INTO VerbEntityReplicate(vePk, veDestination)
    SELECT DISTINCT VerbEntity.verbUid AS veUid,
         UserSession.usClientNodeId AS veDestination
    FROM ChangeLog
         JOIN VerbEntity
             ON ChangeLog.chTableId = ${VerbEntity.TABLE_ID}
                AND ChangeLog.chEntityPk = VerbEntity.verbUid
         JOIN UserSession ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
    WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND VerbEntity.verbLct != COALESCE(
         (SELECT veVersionId
            FROM VerbEntityReplicate
           WHERE vePk = VerbEntity.verbUid
             AND veDestination = UserSession.usClientNodeId), 0)
    /*psql ON CONFLICT(vePk, veDestination) DO UPDATE
     SET vePending = true
    */               
    """)
    @ReplicationRunOnChange([VerbEntity::class])
    @ReplicationCheckPendingNotificationsFor([VerbEntity::class])
    abstract suspend fun replicateOnChange()

    @Query("SELECT * FROM VerbEntity WHERE urlId = :urlId")
    abstract fun findByUrl(urlId: String?): VerbEntity?

    @JsName("findByUidList")
    @Query("SELECT verbUid FROM VerbEntity WHERE verbUid IN (:uidList)")
    abstract suspend fun findByUidList(uidList: List<Long>): List<Long>

    @JsName("replaceList")
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceList(entityList: List<VerbEntity>)

   @Query("""SELECT VerbEntity.verbUid, VerbEntity.urlId, XLangMapEntry.valueLangMap AS display
        FROM VerbEntity LEFT JOIN XLangMapEntry on XLangMapEntry.verbLangMapUid = VerbEntity.verbUid WHERE 
         XLangMapEntry.verbLangMapUid NOT IN (:uidList)""")
    abstract fun findAllVerbsAscList(uidList: List<Long>): List<VerbDisplay>

    @Query("""SELECT VerbEntity.verbUid, VerbEntity.urlId, XLangMapEntry.valueLangMap AS display 
         FROM VerbEntity LEFT JOIN XLangMapEntry on XLangMapEntry.verbLangMapUid = VerbEntity.verbUid WHERE 
         VerbEntity.verbUid NOT IN (:uidList) ORDER BY display ASC""")
    abstract fun findAllVerbsAsc(uidList: List<Long>): DataSourceFactory<Int, VerbDisplay>

    @Query("""SELECT VerbEntity.verbUid, VerbEntity.urlId, XLangMapEntry.valueLangMap AS display 
         FROM VerbEntity LEFT JOIN XLangMapEntry on XLangMapEntry.verbLangMapUid = VerbEntity.verbUid WHERE 
        VerbEntity.verbUid NOT IN (:uidList) ORDER BY display DESC""")
    abstract fun findAllVerbsDesc(uidList: List<Long>): DataSourceFactory<Int, VerbDisplay>


}
