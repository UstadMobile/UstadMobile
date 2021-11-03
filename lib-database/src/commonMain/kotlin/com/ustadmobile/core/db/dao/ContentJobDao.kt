package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem

@Dao
abstract class ContentJobDao {

    @Insert
    abstract suspend fun insertAsync(contentJob: ContentJob): Long

    @Query("""
        SELECT ContentJob.*
          FROM ContentJob
         WHERE cjUid = :cjUid 
    """)
    abstract fun findByUid(cjUid: Long): ContentJob?

    @Query("""
        SELECT ContentJob.*
          FROM ContentJob
         WHERE cjUid = :cjUid 
    """)
    abstract suspend fun findByUidAsync(cjUid: Long): ContentJob?

    @Query("""
        SELECT ContentJob.*
          FROM ContentJob
         WHERE cjUid = :cjUid
    """)
    abstract fun findLiveDataByUid(cjUid: Long): DoorLiveData<ContentJob?>

    @Query("""
        UPDATE ContentJob
           SET toUri = :toUri
         WHERE cjUid = :cjUid
    """)
    abstract suspend fun updateDestinationDir(cjUid: Long, toUri: String)

    @Query("""
        SELECT COALESCE((SELECT ContentJob.cjIsMeteredAllowed
          FROM ContentJob
         WHERE cjUid = :contentJobId
         LIMIT 1),0)
    """)
    abstract fun findMeteredAllowedLiveData(contentJobId: Long): DoorLiveData<Boolean>


    @Query("""
        UPDATE ContentJob 
           SET cjIsMeteredAllowed = :meteredAllowed
         WHERE cjUid IN (SELECT cjiJobUid 
                           FROM ContentJobItem
                          WHERE cjiContentEntryUid = :contentEntryUid
                             OR cjiParentContentEntryUid = :contentEntryUid)
    """)
    abstract suspend fun updateMeteredAllowedForEntry(contentEntryUid: Long, meteredAllowed: Boolean)

}