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
        REPLACE INTO SiteTrkr(siteFk, siteVersionId, siteDestination)
         SELECT Site.siteUid AS siteFk,
                Site.siteLct AS siteVersionId,
                :newNodeId AS siteDestination
           FROM Site
          WHERE Site.siteLct != COALESCE(
                (SELECT siteVersionId
                   FROM SiteTrkr
                  WHERE siteFk = Site.siteUid
                    AND siteDestination = :newNodeId), 0) 
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([Site::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)


}