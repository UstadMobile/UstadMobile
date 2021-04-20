package com.ustadmobile.stats.db

import androidx.room.Dao
import androidx.room.Insert
import com.ustadmobile.lib.db.entities.UstadCentralReportRow

@Dao
abstract class UstadCentralReportRowDao {

    @Insert
    abstract fun insertList(list: List<UstadCentralReportRow>)

}