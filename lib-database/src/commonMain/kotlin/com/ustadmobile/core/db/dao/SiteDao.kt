package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.UserSession

@DoorDao
@Repository
expect abstract class SiteDao {

    @Query("SELECT * FROM Site LIMIT 1")
    abstract fun getSite(): Site?

    @Query("SELECT * FROM Site LIMIT 1")
    @RepoHttpAccessible
    abstract suspend fun getSiteAsync(): Site?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceAsync(site: Site): Long

    @Insert
    abstract fun insert(site: Site): Long

    @Update
    abstract suspend fun updateAsync(workspace: Site)

    @Query("""
        REPLACE INTO SiteReplicate(sitePk, siteDestination)
         SELECT DISTINCT Site.siteUid AS sitePk,
                :newNodeId AS siteDestination
           FROM Site
          WHERE Site.siteLct != COALESCE(
                (SELECT siteVersionId
                   FROM SiteReplicate
                  WHERE sitePk = Site.siteUid
                    AND siteDestination = :newNodeId), 0) 
         /*psql ON CONFLICT(sitePk, siteDestination) DO UPDATE
                SET sitePending = true
         */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([Site::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)


    @Query("""
        REPLACE INTO SiteReplicate(sitePk, siteDestination)
         SELECT DISTINCT Site.siteUid AS sitePk,
                UserSession.usClientNodeId AS siteDestination
           FROM ChangeLog
                JOIN Site 
                    ON ChangeLog.chTableId = 189 
                       AND ChangeLog.chEntityPk = Site.siteUid
                JOIN UserSession ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
          WHERE UserSession.usClientNodeId != (
                SELECT nodeClientId 
                  FROM SyncNode
                 LIMIT 1)
            AND Site.siteLct != COALESCE(
                (SELECT siteVersionId
                   FROM SiteReplicate
                  WHERE sitePk = Site.siteUid
                    AND siteDestination = UserSession.usClientNodeId), 0)     
        /*psql  ON CONFLICT(sitePk, siteDestination) DO UPDATE
            SET sitePending = true
         */               
    """)
    @ReplicationRunOnChange([Site::class])
    @ReplicationCheckPendingNotificationsFor([Site::class])
    abstract suspend fun replicateOnChange()


}