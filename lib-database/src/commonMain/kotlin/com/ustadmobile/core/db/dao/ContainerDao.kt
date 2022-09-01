package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.core.db.dao.ContainerDaoCommon.CONTAINER_READY_WHERE_CLAUSE
import com.ustadmobile.core.db.dao.ContainerDaoCommon.FROM_CONTAINER_WHERE_MOST_RECENT_AND_READY
import com.ustadmobile.core.db.dao.ContainerDaoCommon.SELECT_MOST_RECENT_READY_CONTAINER
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerUidAndMimeType
import com.ustadmobile.lib.db.entities.ContainerWithContentEntry
import com.ustadmobile.lib.db.entities.UserSession

@DoorDao
@Repository
expect abstract class ContainerDao : BaseDao<Container> {

    @Query("""
         REPLACE INTO ContainerReplicate(containerPk, containerDestination)
          SELECT DISTINCT Container.containerUid AS containerPk,
                 :newNodeId AS containerDestination
            FROM Container
           WHERE Container.cntLct != COALESCE(
                 (SELECT containerVersionId
                    FROM ContainerReplicate
                   WHERE containerPk = Container.containerUid
                     AND containerDestination = :newNodeId), 0) 
          /*psql ON CONFLICT(containerPk, containerDestination) DO UPDATE
                 SET containerPending = true
          */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([Container::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO ContainerReplicate(containerPk, containerDestination)
  SELECT DISTINCT Container.containerUid AS containerUid,
         UserSession.usClientNodeId AS containerDestination
    FROM ChangeLog
         JOIN Container
             ON ChangeLog.chTableId = ${Container.TABLE_ID}
                AND ChangeLog.chEntityPk = Container.containerUid
         JOIN UserSession ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND Container.cntLct != COALESCE(
         (SELECT containerVersionId
            FROM ContainerReplicate
           WHERE containerPk = Container.containerUid
             AND containerDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(containerPk, containerDestination) DO UPDATE
     SET containerPending = true
  */               
    """)
    @ReplicationRunOnChange([Container::class])
    @ReplicationCheckPendingNotificationsFor([Container::class])
    abstract suspend fun replicateOnChange()

    @Insert
    abstract suspend fun insertListAsync(containerList: List<Container>)

    @Query("Select Container.* FROM Container " +
            "WHERE Container.containerContentEntryUid = :contentEntry " +
            "ORDER BY Container.cntLastModified DESC LIMIT 1")
    abstract suspend fun getMostRecentDownloadedContainerForContentEntryAsync(contentEntry: Long): Container?

    @Query(SELECT_MOST_RECENT_READY_CONTAINER)
    abstract fun getMostRecentContainerForContentEntry(contentEntryUid: Long): Container?


    @Query("SELECT * FROM Container WHERE containerUid = :uid")
    abstract fun findByUid(uid: Long): Container?

    @Query("""
        SELECT(COALESCE((
               SELECT fileSize
                 FROM Container
                WHERE containerUid = :uid), 0))
    """)
    abstract suspend fun findSizeByUid(uid: Long): Long

    @Query("SELECT recent.* " +
            "FROM Container recent LEFT JOIN Container old " +
            "ON (recent.containerContentEntryUid = old.containerContentEntryUid " +
            "AND recent.cntLastModified < old.cntLastModified) " +
            "WHERE old.containerUid IS NULL " +
            "AND recent.containerContentEntryUid IN (:contentEntries)")
    abstract suspend fun findRecentContainerToBeMonitoredWithEntriesUid(contentEntries: List<Long>): List<Container>

    @Query("""Select Container.* FROM Container 
                    WHERE Container.containerContentEntryUid = :contentEntryUid
                    ORDER BY Container.cntLastModified DESC""")
    abstract suspend fun findContainersForContentEntryUid(contentEntryUid: Long): List<Container>

    @Query("""
          SELECT EXISTS(SELECT 1
                          FROM Container 
                         WHERE Container.containerContentEntryUid = :contentEntryUid
                           AND NOT EXISTS (SELECT ContainerEntry.ceUid 
                                         FROM ContainerEntry
                                        WHERE ContainerEntry.ceContainerUid = Container.containerUid)   
                      ORDER BY cntLastModified DESC LIMIT 1)
    """)
    abstract fun hasContainerWithFilesToDownload(contentEntryUid: Long): LiveData<Boolean>

    @Query("""
            SELECT Container.*
              FROM Container
             WHERE Container.containerContentEntryUid = :contentEntryUid
               AND EXISTS (SELECT ContainerEntry.ceUid 
                             FROM ContainerEntry
                            WHERE ContainerEntry.ceContainerUid = Container.containerUid)     
          ORDER BY Container.cntLastModified DESC LIMIT 1
    """)
    abstract suspend fun findContainerWithFilesByContentEntryUid(contentEntryUid: Long): Container?


    @Query("SELECT Container.* FROM Container " +
            "LEFT JOIN ContentEntry ON ContentEntry.contentEntryUid = containerContentEntryUid " +
            "WHERE ContentEntry.publik")
    abstract fun findAllPublikContainers(): List<Container>

    @Query("SELECT * From Container WHERE Container.containerUid = :containerUid LIMIT 1")
    abstract suspend fun findByUidAsync(containerUid: Long): Container?


    @Query("""
            UPDATE Container 
               SET cntNumEntries = COALESCE(
                   (SELECT COUNT(*) 
                      FROM ContainerEntry 
                     WHERE ceContainerUid = Container.containerUid), 0),
                   fileSize = COALESCE(
                   (SELECT SUM(ContainerEntryFile.ceCompressedSize) AS totalSize 
                      FROM ContainerEntry
                      JOIN ContainerEntryFile ON ContainerEntry.ceCefUid = ContainerEntryFile.cefUid
                     WHERE ContainerEntry.ceContainerUid = Container.containerUid), 0),
                   cntLct = :changeTime   
                     
             WHERE containerUid = :containerUid
        """)
    abstract suspend fun updateContainerSizeAndNumEntriesAsync(containerUid: Long, changeTime: Long)

    @Query("SELECT Container.containerUid FROM Container " +
            "WHERE Container.containerUid = :containerUid " +
            "AND (SELECT COUNT(*) FROM ContainerEntry WHERE ceContainerUid = Container.containerUid) = Container.cntNumEntries")
    abstract fun findLocalAvailabilityByUid(containerUid: Long): Long

    @Query("SELECT * FROM Container WHERE Container.containerUid = :containerUid")
    abstract fun findAllWithId(containerUid: Long): List<Container>

    @Query("SELECT Container.*, ContentEntry.entryId, ContentEntry.sourceUrl FROM Container " +
            "LEFT JOIN ContentEntry ON Container.containerContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntry.publisher LIKE '%Khan Academy%' AND Container.mimeType = 'video/mp4'")
    abstract fun findKhanContainers(): List<ContainerWithContentEntry>

    @Query("DELETE FROM Container WHERE containerUid = :containerUid")
    abstract fun deleteByUid(containerUid: Long)

    @Query("UPDATE Container SET mimeType = :mimeType WHERE Container.containerUid = :containerUid")
    abstract fun updateMimeType(mimeType: String, containerUid: Long)

    @Query(SELECT_MOST_RECENT_READY_CONTAINER)
    abstract suspend fun getMostRecentContainerForContentEntryAsync(contentEntryUid: Long): Container?

    /**
     * Used by the ContainerDownloadPlugin to find the most recent container to try and download.
     *
     */
    @Query("""
        SELECT COALESCE((
                SELECT containerUid 
                 $FROM_CONTAINER_WHERE_MOST_RECENT_AND_READY), 0)
    """)
    abstract suspend fun getMostRecentContainerUidForContentEntryAsync(contentEntryUid: Long): Long

    /**
     * Used by the ContentEntryOpener to find the most recent container for which the download has
     * been completed.
     */
    @Query("""
        SELECT Container.containerUid, Container.mimeType 
          FROM Container
         WHERE Container.containerContentEntryUid = :contentEntryUid
           AND $CONTAINER_READY_WHERE_CLAUSE
           AND (CAST(:downloadRequired AS INTEGER) = 0
                OR EXISTS (SELECT ContainerEntry.ceUid 
                             FROM ContainerEntry
                            WHERE ContainerEntry.ceContainerUid = Container.containerUid))
      ORDER BY Container.cntLastModified DESC 
         LIMIT 1
    """)
    abstract suspend fun getMostRecentAvailableContainerUidAndMimeType(
        contentEntryUid: Long,
        downloadRequired: Boolean,
    ): ContainerUidAndMimeType?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replaceList(entries: List<Container>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertWithReplace(container : Container)

    @Query("""
        SELECT COALESCE(
               (SELECT fileSize
                  FROM Container
                 WHERE containerUid = :containerUid), -1)
    """)
    abstract suspend fun getContainerSizeByUid(containerUid: Long) : Long




}
