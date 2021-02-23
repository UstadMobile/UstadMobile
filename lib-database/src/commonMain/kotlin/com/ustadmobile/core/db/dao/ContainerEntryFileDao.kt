package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorDbType
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.lib.db.entities.ContainerEntryFile

@Dao
abstract class ContainerEntryFileDao : BaseDao<ContainerEntryFile> {

    //TODO: split this to handle very large queries
    @Query("SELECT ContainerEntryFile.* FROM ContainerEntryFile WHERE cefMd5 IN (:md5Sums)")
    abstract fun findEntriesByMd5Sums(md5Sums: List<String>): List<ContainerEntryFile>

    @Transaction
    open fun findEntriesByMd5SumsSafe(md5Sums: List<String>, db: UmAppDatabase): List<ContainerEntryFile> {
        return if (db.dbType() == DoorDbType.SQLITE) {
            val chunkedList = md5Sums.chunked(90)
            val mutableList = mutableListOf<ContainerEntryFile>()
            chunkedList.forEach {
                findEntriesByMd5Sums(it).map { entryFile -> mutableList.add(entryFile) }
            }
            mutableList.toList()
        } else {
            findEntriesByMd5Sums(md5Sums)
        }
    }

    @Query("SELECT ContainerEntryFile.* FROM ContainerEntryFile WHERE cefUid IN (:uidList)")
    abstract fun findEntriesByUids(uidList: List<Long>): List<ContainerEntryFile>

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

    @Query("SELECT ContainerEntryFile.* from ContainerEntryFile WHERE NOT EXISTS (SELECT ContainerEntry.ceCefUid FROM ContainerEntry WHERE ContainerEntryFile.cefUid = ContainerEntry.ceCefUid) LIMIT 100")
    abstract fun findZombieEntries(): List<ContainerEntryFile>

    @Delete
    abstract fun deleteListOfEntryFiles(entriesToDelete: List<ContainerEntryFile>)

    @Query("SELECT ContainerEntryFile.* FROM ContainerEntryFile WHERE cefMd5 = :md5Sum")
    abstract suspend fun findEntryByMd5Sum(md5Sum: String): ContainerEntryFile?

    companion object {

        const val ENDPOINT_CONCATENATEDFILES = "ConcatenatedContainerFiles"

        const val ENDPOINT_CONCATENATEDFILES2 = "ConcatenatedContainerFiles2"

    }

}
