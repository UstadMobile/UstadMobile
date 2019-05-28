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


    @Query("SELECT SUM(ContainerEntryFile.ceCompressedSize) FROM " +
            "ContainerEntry " +
            "JOIN ContainerEntryFile ON ContainerEntry.ceCefUid = ContainerEntryFile.cefUid " +
            "WHERE ContainerEntry.ceContainerUid = :containerUid")
    abstract fun sumContainerFileEntrySizes(containerUid: Long): Long

}
