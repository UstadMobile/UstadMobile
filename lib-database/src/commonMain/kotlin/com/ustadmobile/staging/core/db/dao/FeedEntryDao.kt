package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.FeedEntry

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
@Dao
abstract class FeedEntryDao : BaseDao<FeedEntry> {

    @Insert
    abstract override fun insert(entity: FeedEntry): Long

    @Update
    abstract suspend fun updateAsync(entity: FeedEntry):Int

    @Query("SELECT * FROM FeedEntry WHERE feedEntryUid = :uid")
    abstract fun findByUid(uid: Long): FeedEntry?

    @Query("SELECT * FROM FeedEntry WHERE feedEntryUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : FeedEntry?

    @Query("SELECT * FROM FeedEntry WHERE feedEntryPersonUid = :personUid AND feedEntryDone = 0" + " ORDER BY dateCreated DESC")
    abstract fun findByPersonUid(personUid: Long): DataSource.Factory<Int, FeedEntry>

    @Query("UPDATE FeedEntry SET feedEntryDone = :done WHERE feedEntryClazzLogUid = :clazzLogUid " + "AND feedEntryCheckType = :taskType")
    abstract fun markEntryAsDoneByClazzLogUidAndTaskType(clazzLogUid: Long, taskType: Int, done: Boolean)

    companion object {

        fun generateFeedEntryHash(personUid: Long, clazzLogUid: Long, alertType: Int,
                                  link: String): Long {
            var hash = clazzLogUid.hashCode()
            hash = 31 * hash + personUid.hashCode()
            hash = 31 * hash + alertType
            hash = 31 * hash + link.hashCode()
            return hash.toLong()
        }
    }
}
