package com.ustadmobile.stats.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.lib.db.entities.UstadCentralReportRow

@Dao
abstract class UstadCentralReportRowDao {

    @Insert
    abstract fun insertList(list: List<UstadCentralReportRow>)

    @Query("""
        SELECT UstadCentralReportRow.*
          FROM UstadCentralReportRow
         WHERE disaggregationKey = :key
    """)
    abstract fun findByDisaggregationKey(key: Int): List<UstadCentralReportRow>

}