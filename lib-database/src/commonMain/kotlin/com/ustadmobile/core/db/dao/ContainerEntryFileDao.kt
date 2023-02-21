package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.lib.db.entities.ContainerEntryFileUidAndPath

@DoorDao
expect abstract class ContainerEntryFileDao : BaseDao<ContainerEntryFile> {

    @Insert
    abstract suspend fun insertListAsync(list: List<ContainerEntryFile>)

    @Query("SELECT ContainerEntryFile.* FROM ContainerEntryFile WHERE cefMd5 IN (:md5Sums)")
    abstract fun findEntriesByMd5Sums(md5Sums: List<String>): List<ContainerEntryFile>

    //language=RoomSql
    @Query("SELECT ContainerEntryFile.* FROM ContainerEntryFile WHERE cefMd5 IN (:md5Sums)")
    abstract suspend fun findEntriesByMd5SumsAsync(md5Sums: List<String>): List<ContainerEntryFile>

    //language=RoomSql
    @Query("SELECT ContainerEntryFile.cefMd5 FROM ContainerEntryFile WHERE cefMd5 IN (:md5Sums)")
    abstract suspend fun findExistingMd5SumsByMd5SumsAsync(md5Sums: List<String>): List<String?>


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

    @Query("""SELECT ContainerEntryFile.* 
                      FROM ContainerEntryFile 
                     WHERE NOT EXISTS (SELECT ContainerEntry.ceCefUid 
                                     FROM ContainerEntry 
                                    WHERE ContainerEntryFile.cefUid = ContainerEntry.ceCefUid) 
                     LIMIT 100""")
    abstract fun findZombieEntries(): List<ContainerEntryFile>

    @Query("""
        SELECT cefUid, cefPath
          FROM ContainerEntryFile
         WHERE NOT EXISTS 
               (SELECT ContainerEntry.ceCefUid 
                  FROM ContainerEntry 
                 WHERE ContainerEntry.ceCefUid = ContainerEntryFile.cefUid 
                 LIMIT 1)
         LIMIT :limit     
    """)
    abstract suspend fun findZombieUidsAndPath(limit: Int): List<ContainerEntryFileUidAndPath>

    @Query("""
        DELETE FROM ContainerEntryFile
              WHERE cefUid IN (:uidList) 
    """)
    abstract suspend fun deleteByUidList(uidList: List<Long>)

    @Delete
    abstract fun deleteListOfEntryFiles(entriesToDelete: List<ContainerEntryFile>)

    @Query("SELECT ContainerEntryFile.* FROM ContainerEntryFile WHERE cefMd5 = :md5Sum")
    abstract suspend fun findEntryByMd5Sum(md5Sum: String): ContainerEntryFile?

}
