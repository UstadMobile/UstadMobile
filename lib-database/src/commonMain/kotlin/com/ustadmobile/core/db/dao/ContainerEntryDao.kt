package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.lib.db.entities.ContainerEntry
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5

@Dao
abstract class ContainerEntryDao : BaseDao<ContainerEntry> {


    @Transaction
    open fun insertAndSetIds(containerEntryList: List<ContainerEntry>) {
        for (entry in containerEntryList) {
            entry.ceUid = insert(entry)
        }
    }

    @Insert
    abstract suspend fun insertListAsync(containerEntryList: List<ContainerEntry>)

    @Query("SELECT ContainerEntry.*, ContainerEntryFile.* " +
            "FROM ContainerEntry " +
            "LEFT JOIN ContainerEntryFile ON ContainerEntry.ceCefUid = ContainerEntryFile.cefUid " +
            "WHERE ContainerEntry.ceContainerUid = :containerUid")
    abstract fun findByContainer(containerUid: Long): List<ContainerEntryWithContainerEntryFile>

    @Query("SELECT ContainerEntry.*, ContainerEntryFile.* " +
            "FROM ContainerEntry " +
            "LEFT JOIN ContainerEntryFile ON ContainerEntry.ceCefUid = ContainerEntryFile.cefUid " +
            "WHERE ContainerEntry.ceContainerUid = :containerUid " +
            "AND ContainerEntry.cePath = :pathInContainer")
    abstract fun findByPathInContainer(containerUid: Long, pathInContainer: String): ContainerEntryWithContainerEntryFile?

    @Query("SELECT ContainerEntry.*, ContainerEntryFile.cefMd5 AS cefMd5 " +
            "FROM ContainerEntry " +
            "LEFT JOIN ContainerEntryFile ON ContainerEntry.ceCefUid = ContainerEntryFile.cefUid " +
            "WHERE ContainerEntry.ceContainerUid = :containerUid")
    abstract fun findByContainerWithMd5(containerUid: Long): List<ContainerEntryWithMd5>


    @Query("SELECT ContainerEntry.*, ContainerEntryFile.* " +
            "FROM ContainerEntry " +
            "LEFT JOIN ContainerEntryFile ON ContainerEntry.ceCefUid = ContainerEntryFile.cefUid " +
            "WHERE ContainerEntry.ceContainerUid = :containerUid")
    abstract suspend fun findByContainerAsync(containerUid: Long): List<ContainerEntryWithContainerEntryFile>


    @Query("DELETE FROM ContainerEntry WHERE ceContainerUid = :containerUid")
    abstract fun deleteByContainerUid(containerUid: Long)

    @Query("DELETE FROM ContainerEntry WHERE ceUid = :containerEntryUid")
    abstract fun deleteByContainerEntryUid(containerEntryUid: Long)

    @Delete
    abstract fun deleteList(entries: List<ContainerEntry>)

    @Query("""
            DELETE FROM ContainerEntry 
             WHERE ceContainerUid 
                IN (SELECT cjiContainerUid 
                      FROM ContentJobItem
                      JOIN ContentJob ON ContentJobItem.cjiJobUid = ContentJob.cjUid
                     WHERE ContentJob.cjUid = :jobId)""")
    abstract fun deleteContainerEntriesCreatedByJobs(jobId: Long)

    /**
     * This query can be used where we know that a ContainerEntryFile with the given md5 exists,
     * but we don't have the container entry file uid.
     *
     * COALESCE has to be used because the query will otherwise fail compile-time query checks.
     */
    @Query("""
        INSERT INTO ContainerEntry(ceContainerUid, cePath, ceCefUid) 
        SELECT :containerUid AS ceContainerUid, :path AS cePath, 
               (SELECT COALESCE(
                      (SELECT cefUid 
                         FROM ContainerEntryFile
                        WHERE cefMd5 = :md5
                        LIMIT 1), 0))  
    """)
    abstract suspend fun insertWithMd5SumsAsync(
        containerUid: Long,
        path: String,
        md5: String
    )
}
