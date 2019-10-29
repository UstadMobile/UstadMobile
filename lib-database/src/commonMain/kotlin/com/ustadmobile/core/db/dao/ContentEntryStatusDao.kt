package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ContentEntryStatus
import com.ustadmobile.lib.db.entities.DownloadJobItemStatus
import kotlin.js.JsName

@Dao
abstract class ContentEntryStatusDao : BaseDao<ContentEntryStatus> {

   /* fun refresh() {
        println("Update content entry status")
    }*/

    @Query("DELETE FROM ContentEntryStatus")
    @JsName("deleteAllAsync")
    abstract suspend fun deleteAllAsync()

    @Query("SELECT * FROM ContentEntryStatus WHERE cesUid = :contentEntryUid")
    abstract suspend fun findByUidAsync(contentEntryUid : Long): ContentEntryStatus?

    @Query("UPDATE ContentEntryStatus SET bytesDownloadSoFar = :bytesDownloadSoFar " + "WHERE cesUid = :contentEntryUid")
    @JsName("updateLeafBytesDownloaded")
    abstract fun updateLeafBytesDownloaded(contentEntryUid: Long, bytesDownloadSoFar: Long)


    @Query("UPDATE ContentEntryStatus SET downloadStatus = :downloadStatus WHERE cesUid = :contentEntryUid")
    @JsName("updateDownloadStatus")
    abstract fun updateDownloadStatus(contentEntryUid: Long, downloadStatus: Int)

    @Query("SELECT * FROM ContentEntryStatus WHERE invalidated")
    @JsName("findAllInvalidated")
    abstract fun findAllInvalidated(): List<ContentEntryStatus>

    @Query("Select * FROM ContentEntryStatus where cesUid = :parentUid")
    @JsName("findContentEntryStatusByUid")
    abstract fun findContentEntryStatusByUid(parentUid: Long): DoorLiveData<ContentEntryStatus?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JsName("insertOrAbort")
    abstract fun insertOrAbort(statusList: List<ContentEntryStatus>)

    @Query("DELETE FROM ContentEntryStatus WHERE cesUid = :cesUid")
    @JsName("deleteByContentEntryUid")
    abstract fun deleteByContentEntryUid(cesUid: Long)

    @Transaction
    open fun updateDownloadStatusByList(statusList: List<DownloadJobItemStatus>) {
        for (status in statusList) {
            updateDownloadStatus(status.contentEntryUid, status.status)
        }
    }


}
