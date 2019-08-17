package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerWithContentEntry
import kotlin.js.JsName

@Dao
@UmRepository
abstract class ContainerDao : BaseDao<Container> {

    @Insert
    abstract fun insert(containerList: List<Container>): Array<Long>

    @Query("Select Container.* FROM Container " +
            "WHERE Container.containerContentEntryUid = :contentEntry " +
            "AND Container.cntNumEntries = (SELECT COUNT(ceUid) FROM ContainerEntry WHERE ceContainerUid = Container.containerUid) " +
            "ORDER BY Container.lastModified DESC LIMIT 1")
    @JsName("getMostRecentDownloadedContainerForContentEntryAsync")
    abstract suspend fun getMostRecentDownloadedContainerForContentEntryAsync(contentEntry: Long): Container?

    @Query("Select Container.* FROM Container " +
            "WHERE Container.containerContentEntryUid = :contentEntry " +
            "ORDER BY Container.lastModified DESC LIMIT 1")
    @JsName("getMostRecentContainerForContentEntry")
    abstract fun getMostRecentContainerForContentEntry(contentEntry: Long): Container?

    @Query("SELECT Container.fileSize FROM Container " +
            "WHERE Container.containerContentEntryUid = :contentEntryUid " +
            "ORDER BY Container.lastModified DESC LIMIT 1")
    @JsName("getFileSizeOfMostRecentContainerForContentEntry")
    abstract fun getFileSizeOfMostRecentContainerForContentEntry(contentEntryUid: Long): Long


    @Query("SELECT * FROM Container WHERE containerUid = :uid")
    @JsName("findByUid")
    abstract fun findByUid(uid: Long): Container?

    @Query("SELECT recent.* " +
            "FROM Container recent LEFT JOIN Container old " +
            "ON (recent.containerContentEntryUid = old.containerContentEntryUid " +
            "AND recent.lastModified < old.lastModified) " +
            "WHERE old.containerUid IS NULL " +
            "AND recent.containerContentEntryUid IN (:contentEntries)")
    @JsName("findRecentContainerToBeMonitoredWithEntriesUid")
    abstract suspend fun findRecentContainerToBeMonitoredWithEntriesUid(contentEntries: List<Long>): List<Container>

    @Query("Select Container.* FROM Container " +
            "WHERE Container.containerContentEntryUid = :contentEntryUid " +
            "ORDER BY Container.lastModified DESC")
    @JsName("findFilesByContentEntryUid")
    abstract suspend fun findFilesByContentEntryUid(contentEntryUid: Long): List<Container>


    @Query("SELECT Container.* FROM Container " +
            "LEFT JOIN ContentEntry ON ContentEntry.contentEntryUid = containerContentEntryUid " +
            "WHERE ContentEntry.publik")
    @JsName("findAllPublikContainers")
    abstract fun findAllPublikContainers(): List<Container>

    @Query("SELECT * From Container WHERE Container.containerUid = :containerUid LIMIT 1")
    @JsName("findByUidAsync")
    abstract suspend fun findByUidAsync(containerUid: Long): Container?

    @Query("UPDATE Container " +
            "SET cntNumEntries = (SELECT COUNT(*) FROM ContainerEntry WHERE ceContainerUid = Container.containerUid)," +
            "fileSize = (SELECT SUM(ContainerEntryFile.ceCompressedSize) AS totalSize FROM ContainerEntry " +
            "JOIN ContainerEntryFile ON ContainerEntry.ceCefUid = ContainerEntryFile.cefUid " +
            "WHERE ContainerEntry.ceContainerUid = Container.containerUid) " +
            "WHERE containerUid = :containerUid")
    @JsName("updateContainerSizeAndNumEntries")
    abstract fun updateContainerSizeAndNumEntries(containerUid: Long)

    @Query("UPDATE Container SET fileSize = " +
            "(SELECT SUM(ContainerEntryFile.ceCompressedSize) AS totalSize " +
            "FROM ContainerEntry JOIN ContainerEntryFile ON " +
            "ContainerEntry.ceCefUid = ContainerEntryFile.cefUid " +
            "WHERE ContainerEntry.ceContainerUid = Container.containerUid)")
    abstract suspend fun updateFileSizeForAllContainers()

    @Query("SELECT Container.containerUid FROM Container " +
            "WHERE Container.containerUid = :containerUid " +
            "AND (SELECT COUNT(*) FROM ContainerEntry WHERE ceContainerUid = Container.containerUid) = Container.cntNumEntries")
    @JsName("findLocalAvailabilityByUid")
    abstract fun findLocalAvailabilityByUid(containerUid: Long): Long

    @Query("SELECT * FROM Container WHERE Container.containerUid = :containerUid")
    @JsName("findAllWithId")
    abstract fun findAllWithId(containerUid: Long): List<Container>

    @Query("SELECT Container.*, ContentEntry.entryId, ContentEntry.sourceUrl FROM Container " +
            "LEFT JOIN ContentEntry ON Container.containerContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntry.publisher LIKE '%Khan Academy%' AND Container.mimeType = 'video/mp4'")
    @JsName("findKhanContainers")
    abstract fun findKhanContainers(): List<ContainerWithContentEntry>

    @Query("DELETE FROM Container WHERE containerUid = :containerUid")
    @JsName("deleteByUid")
    abstract fun deleteByUid(containerUid: Long)

    @Query("UPDATE Container SET mimeType = :mimeType WHERE Container.containerUid = :containerUid")
    @JsName("updateMimeType")
    abstract fun updateMimeType(mimeType: String, containerUid: Long)

    @Query("Select Container.* FROM Container " +
            "WHERE Container.containerContentEntryUid = :contentEntry " +
            "ORDER BY Container.lastModified DESC LIMIT 1")
    abstract suspend fun getMostRecentContainerForContentEntryAsync(contentEntry: Long): Container?

}
