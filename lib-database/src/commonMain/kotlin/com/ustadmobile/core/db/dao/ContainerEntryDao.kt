package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.database.annotation.UmRestAccessible
import com.ustadmobile.lib.db.entities.ContainerEntry
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5

@Dao
@UmRepository
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

    @UmRestAccessible
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
}
