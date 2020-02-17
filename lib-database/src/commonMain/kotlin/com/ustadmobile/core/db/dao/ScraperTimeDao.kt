package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.db.entities.ScraperTime

@Dao
abstract class ScraperTimeDao : BaseDao<ScraperTime> {

    @Query("UPDATE ScraperTime set time = :time")
    abstract fun updateTime(time: Long)

    @Query("SELECT * FROM ScraperTime LIMIT 1")
    abstract fun getTime(): ScraperTime?

}