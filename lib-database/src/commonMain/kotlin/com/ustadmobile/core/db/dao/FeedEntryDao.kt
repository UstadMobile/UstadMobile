package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.lib.db.entities.FeedEntry

@Dao
abstract class FeedEntryDao {

    @Query("""
        SELECT FeedEntry.*
          FROM FeedEntry
         WHERE FeedEntry.fePersonUid = :personUid
    """)
    abstract fun findByPersonUidAsDataSource(personUid: Long): DataSource.Factory<Int, FeedEntry>

    @Query("""
        SELECT FeedEntry.*
          FROM FeedEntry
         WHERE FeedEntry.fePersonUid = :personUid
    """)
    abstract suspend fun findByPersonAsList(personUid: Long): List<FeedEntry>

    @Insert
    abstract fun insertList(entityList: List<FeedEntry>)

    @Insert
    abstract suspend fun insertListAsync(entityList: List<FeedEntry>)

}