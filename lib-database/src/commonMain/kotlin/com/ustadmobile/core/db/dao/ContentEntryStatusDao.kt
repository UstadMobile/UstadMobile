package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ContentEntryStatus
import com.ustadmobile.lib.db.entities.DownloadJobItemStatus

@Dao
abstract class ContentEntryStatusDao : BaseDao<ContentEntryStatus> {

    fun refresh() {
        println("Update content entry status")
    }

    @Query("DELETE FROM ContentEntryStatus")
    abstract suspend fun deleteAllAsync()

    @Query("UPDATE ContentEntryStatus SET bytesDownloadSoFar = :bytesDownloadSoFar " + "WHERE cesUid = :contentEntryUid")
    abstract fun updateLeafBytesDownloaded(contentEntryUid: Long, bytesDownloadSoFar: Long)


    @Query("UPDATE ContentEntryStatus SET downloadStatus = :downloadStatus WHERE cesUid = :contentEntryUid")
    abstract fun updateDownloadStatus(contentEntryUid: Long, downloadStatus: Int)

    @Query("SELECT * FROM ContentEntryStatus WHERE invalidated")
    abstract fun findAllInvalidated(): List<ContentEntryStatus>

    @Query("Select * FROM ContentEntryStatus where cesUid = :parentUid")
    abstract fun findContentEntryStatusByUid(parentUid: Long): DoorLiveData<ContentEntryStatus>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertOrAbort(statusList: List<ContentEntryStatus>)

    @Query("DELETE FROM ContentEntryStatus WHERE cesUid = :cesUid")
    abstract fun deleteByFileUids(cesUid: Long)

    @Transaction
    fun updateDownloadStatusByList(statusList: List<DownloadJobItemStatus>) {
        for (status in statusList) {
            updateDownloadStatus(status.contentEntryUid, status.status)
        }
    }


}
