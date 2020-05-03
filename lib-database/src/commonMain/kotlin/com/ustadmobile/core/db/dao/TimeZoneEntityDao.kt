package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.lib.db.entities.TimeZoneEntity

@Dao
abstract class TimeZoneEntityDao {

    @Query("SELECT * FROM TimeZoneEntity ORDER BY rawOffset")
    abstract fun findAllSortedByOffset(): DataSource.Factory<Int, TimeZoneEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replaceList(entityList: List<TimeZoneEntity>)


}