package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.db.entities.ContainerEntryFile

@UmDao
@Dao
abstract class ContainerEntryFileDao : BaseDao<ContainerEntryFile> {

    //TODO: split this to handle very large queries
    @Query("SELECT ContainerEntryFile.* FROM ContainerEntryFile WHERE cefMd5 IN (:md5Sums)")
    abstract fun findEntriesByMd5Sums(md5Sums: List<String>): List<ContainerEntryFile>

    @Query("UPDATE ContainerEntryFile SET cefPath = :path WHERE cefUid = :cefUid")
    abstract fun updateFilePath(cefUid: Long, path: String)

    @Query("SELECT * FROM ContainerEntryFile WHERE cefUid = :uid")
    abstract fun findByUid(uid: Long): ContainerEntryFile?

    @Query("SELECT SUM(ContainerEntryFile.ceCompressedSize) FROM " +
            "ContainerEntry " +
            "JOIN ContainerEntryFile ON ContainerEntry.ceCefUid = ContainerEntryFile.cefUid " +
            "WHERE ContainerEntry.ceContainerUid = :containerUid")
    abstract fun sumContainerFileEntrySizes(containerUid: Long): Long


    @Query("SELECT * FROM ContainerEntryFile " +
            "WHERE compression = 0 AND " +
            "NOT EXISTS(SELECT * FROM ContainerEntry " +
            "WHERE ceCefUid = ContainerEntryFile.cefUid AND (ContainerEntry.cePath LIKE '%.webm' OR ContainerEntry.cePath LIKE '%.mp4')) LIMIT 100")
    abstract suspend fun getAllFilesForCompression(): List<ContainerEntryFile>

    @Query("UPDATE ContainerEntryFile SET compression = :compression, ceCompressedSize = :ceCompressedSize WHERE cefUid = :cefUid")
    abstract fun updateCompressedFile(compression: Int, ceCompressedSize: Long, cefUid: Long)

}
