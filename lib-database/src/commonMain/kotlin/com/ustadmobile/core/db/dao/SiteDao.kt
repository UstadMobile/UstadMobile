package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Site

@Dao
@Repository
abstract class SiteDao: BaseDao<Site>{

    @Query("SELECT * FROM Site LIMIT 1")
    abstract fun getSite(): Site?

    @Query("SELECT * FROM Site LIMIT 1")
    abstract suspend fun getSiteAsync(): Site?

    @Update
    abstract suspend fun updateAsync(workspace: Site)

    @Query("""
        REPLACE INTO SiteReplicate(sitePk, siteVersionId, siteDestination)
         SELECT Site.siteUid AS sitePk,
                Site.siteLct AS siteVersionId,
                :newNodeId AS siteDestination
           FROM Site
          WHERE Site.siteLct != COALESCE(
                (SELECT siteVersionId
                   FROM SiteReplicate
                  WHERE sitePk = Site.siteUid
                    AND siteDestination = :newNodeId), 0) 
         /*psql ON CONFLICT(sitePk, siteDestination) DO UPDATE
                SET siteProcessed = false
         */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([Site::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)


    @Query("""
        REPLACE INTO SiteReplicate(sitePk, siteVersionId, siteDestination)
         SELECT Site.siteUid AS sitePk,
                Site.siteLct AS siteVersionId,
                UserSession.usClientNodeId AS siteDestination
           FROM ChangeLog
                JOIN Site 
                    ON ChangeLog.chTableId = 189 
                       AND ChangeLog.chEntityPk = Site.siteUid
                JOIN UserSession
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
            SET siteProcessed = false
         */               
    """)
    @ReplicationRunOnChange([Site::class])
    @ReplicationCheckPendingNotificationsFor([Site::class])
    abstract suspend fun replicateOnChange()


}