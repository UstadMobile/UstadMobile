package com.ustadmobile.stats.db

import androidx.room.Database
import com.ustadmobile.door.DoorDatabase
import com.ustadmobile.lib.db.entities.UstadCentralReportRow

@Database(entities = [UstadCentralReportRow::class], version = 1)
abstract class StatsDatabase: DoorDatabase() {

    abstract val ustadCentralReportRowDao: UstadCentralReportRowDao

}