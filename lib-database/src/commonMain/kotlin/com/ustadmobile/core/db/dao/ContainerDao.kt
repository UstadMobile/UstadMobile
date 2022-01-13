package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerUidAndMimeType
import com.ustadmobile.lib.db.entities.ContainerWithContentEntry
import com.ustadmobile.door.SyncNode
import com.ustadmobile.lib.db.entities.UserSession

@Dao
@Repository
abstract class ContainerDao : BaseDao<Container> {

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

    @Query(SELECT_ACTIVE_RECENT_CONTAINER)
    abstract fun getMostRecentContainerForContentEntry(contentEntry: Long): Container?

    @Query(SELECT_ACTIVE_RECENT_CONTAINER)
    abstract fun getMostRecentContainerForContentEntryLive(contentEntry: Long) : DoorLiveData<Container?>

    @Query("SELECT Container.fileSize FROM Container " +
            "WHERE Container.containerContentEntryUid = :contentEntryUid " +
            "ORDER BY Container.cntLastModified DESC LIMIT 1")
    abstract fun getFileSizeOfMostRecentContainerForContentEntry(contentEntryUid: Long): Long


    @Query("SELECT * FROM Container WHERE containerUid = :uid")
    abstract fun findByUid(uid: Long): Container?

    @Query("""
        SELECT(COALESCE((
               SELECT fileSize
                 FROM Container
                WHERE containerUid = :uid), 1))
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
    abstract fun hasContainerWithFilesToDownload(contentEntryUid: Long): DoorLiveData<Boolean>

    @Query("""
          SELECT EXISTS(SELECT 1
                          FROM Container
                     LEFT JOIN ContentJobItem
                               ON ContentJobItem.cjiContainerUid = Container.containerUid  
                         WHERE Container.containerContentEntryUid = :contentEntryUid
                           AND ContentJobItem.cjiRecursiveStatus = ${JobStatus.COMPLETE}
                           AND EXISTS (SELECT ContainerEntry.ceUid 
                                         FROM ContainerEntry
                                        WHERE ContainerEntry.ceContainerUid = Container.containerUid)   
                      ORDER BY cntLastModified DESC LIMIT 1)
    """)
    abstract fun hasContainerWithFilesToDelete(contentEntryUid: Long): DoorLiveData<Boolean>


    @Query("""
          SELECT EXISTS(SELECT 1
                          FROM Container
                     LEFT JOIN ContentJobItem
                               ON ContentJobItem.cjiContainerUid = Container.containerUid  
                         WHERE Container.containerContentEntryUid = :contentEntryUid
                           AND EXISTS (SELECT ContainerEntry.ceUid 
                                         FROM ContainerEntry
                                        WHERE ContainerEntry.ceContainerUid = Container.containerUid)   
                      ORDER BY cntLastModified DESC LIMIT 1)
    """)
    abstract fun hasContainerWithFilesToOpen(contentEntryUid: Long): DoorLiveData<Boolean>

    @Query("""
         SELECT (SELECT MAX(cntLastModified) 
                   FROM Container 
                  WHERE containerContentEntryUid = :contentEntryUid) > (COALESCE((
                  
                        SELECT cntLastModified 
                          FROM Container 
                         WHERE Container.containerContentEntryUid = :contentEntryUid
                           AND EXISTS (SELECT ContainerEntry.ceUid 
                                         FROM ContainerEntry
                                        WHERE ContainerEntry.ceContainerUid = Container.containerUid) 
                      ORDER BY cntLastModified DESC), 9223372036854775807))
    """)
    abstract fun hasContainerWithFilesToUpdate(contentEntryUid: Long): DoorLiveData<Boolean>

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


    @Query("""
            SELECT Container.containerUid, Container.mimeType
              FROM Container
             WHERE Container.containerContentEntryUid = :contentEntryUid
               AND EXISTS (SELECT ContainerEntry.ceUid 
                             FROM ContainerEntry
                            WHERE ContainerEntry.ceContainerUid = Container.containerUid)     
          ORDER BY Container.cntLastModified DESC LIMIT 1
    """)
    abstract suspend fun findContainerWithMimeTypeWithFilesByContentEntryUid(contentEntryUid: Long): ContainerUidAndMimeType?

    @Query("SELECT Container.* FROM Container " +
            "LEFT JOIN ContentEntry ON ContentEntry.contentEntryUid = containerContentEntryUid " +
            "WHERE ContentEntry.publik")
    abstract fun findAllPublikContainers(): List<Container>

    @Query("SELECT * From Container WHERE Container.containerUid = :containerUid LIMIT 1")
    abstract suspend fun findByUidAsync(containerUid: Long): Container?

    @Query(UPDATE_SIZE_AND_NUM_ENTRIES_SQL)
    abstract fun updateContainerSizeAndNumEntries(containerUid: Long)

    @Query(UPDATE_SIZE_AND_NUM_ENTRIES_SQL)
    abstract suspend fun updateContainerSizeAndNumEntriesAsync(containerUid: Long)

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

    @Query(SELECT_ACTIVE_RECENT_CONTAINER)
    abstract suspend fun getMostRecentContainerForContentEntryAsync(contentEntry: Long): Container?

    @Query("""
        SELECT COALESCE((
                SELECT containerUid 
                  FROM Container
                 WHERE containerContentEntryUid = :contentEntryUid), 0)
    """)
    abstract suspend fun getMostRecentContainerUidForContentEntryAsync(contentEntryUid: Long): Long

    @Query("Select Container.containerUid, Container.mimeType FROM Container " +
            "WHERE Container.containerContentEntryUid = :contentEntry " +
            "ORDER BY Container.cntLastModified DESC LIMIT 1")
    abstract suspend fun getMostRecentContaineUidAndMimeType(contentEntry: Long): ContainerUidAndMimeType?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replaceList(entries: List<Container>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertWithReplace(container : Container)

    companion object{
        private const val SELECT_ACTIVE_RECENT_CONTAINER = "Select Container.* FROM Container " +
                "WHERE Container.containerContentEntryUid = :contentEntry " +
                "ORDER BY Container.cntLastModified DESC LIMIT 1"

        private const val UPDATE_SIZE_AND_NUM_ENTRIES_SQL = """
            UPDATE Container 
               SET cntNumEntries = COALESCE(
                   (SELECT COUNT(*) 
                      FROM ContainerEntry 
                     WHERE ceContainerUid = Container.containerUid), 0),
                   fileSize = COALESCE(
                   (SELECT SUM(ContainerEntryFile.ceCompressedSize) AS totalSize 
                      FROM ContainerEntry
                      JOIN ContainerEntryFile ON ContainerEntry.ceCefUid = ContainerEntryFile.cefUid
                     WHERE ContainerEntry.ceContainerUid = Container.containerUid), 0) 
                     
             WHERE containerUid = :containerUid
        """
    }

}
