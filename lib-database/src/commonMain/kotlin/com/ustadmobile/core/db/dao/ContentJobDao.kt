package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ConnectivityStatus
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


    fun findMeteredAllowedLiveData(contentJobId: Long, dbType: Int): DoorLiveData<Boolean> {
        return when {
            dbType == 1 -> findMeteredAllowedLiveDataSqlite(contentJobId) //SQLite
            else -> findMeteredAllowedLiveDataPostgres(contentJobId)
        }
    }

    @Query("""
        SELECT COALESCE((SELECT ContentJob.cjIsMeteredAllowed
          FROM ContentJob
         WHERE cjUid = :contentJobId
         LIMIT 1), 0)
    """)
    abstract fun findMeteredAllowedLiveDataSqlite(contentJobId: Long): DoorLiveData<Boolean>

    @Query("""
        SELECT COALESCE((SELECT ContentJob.cjIsMeteredAllowed
          FROM ContentJob
         WHERE cjUid = :contentJobId
         LIMIT 1), FALSE)   /* nothing */
    """)
    abstract fun findMeteredAllowedLiveDataPostgres(contentJobId: Long): DoorLiveData<Boolean>

    @Query("""
        UPDATE ContentJob 
           SET cjIsMeteredAllowed = :meteredAllowed
         WHERE cjUid IN (SELECT cjiJobUid 
                           FROM ContentJobItem
                          WHERE cjiContentEntryUid = :contentEntryUid
                             OR cjiParentContentEntryUid = :contentEntryUid)
    """)
    abstract suspend fun updateMeteredAllowedForEntry(contentEntryUid: Long, meteredAllowed: Boolean)

    /**
     *  This query is only called when connectivity IS needed, so there is no need for the job item id.
     *  It's only purpose is to check if the connectivity is acceptable for the job
     *  e.g. connectivity == unmetered or (connectivity == metered and meteredNetworkAllowed == true)
     */
    @Query("""
          WITH ConnectivityStateCte(state) AS 
             (SELECT COALESCE(
                     (SELECT connectivityState 
                        FROM ConnectivityStatus 
                       LIMIT 1), 0))
   
           SELECT COALESCE((SELECT 1 
            FROM ContentJob 
           WHERE cjUid = :jobId
             AND (cjIsMeteredAllowed 
             AND (SELECT state FROM ConnectivityStateCte) = ${ConnectivityStatus.STATE_METERED})
			  OR (SELECT state FROM ConnectivityStateCte) = ${ConnectivityStatus.STATE_UNMETERED}),0)
    """)
    abstract suspend fun isConnectivityAcceptableForJob(jobId: Long): Boolean

}