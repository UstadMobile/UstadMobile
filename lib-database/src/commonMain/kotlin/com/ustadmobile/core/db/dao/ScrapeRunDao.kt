package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.Dao
import androidx.room.Query
import com.ustadmobile.lib.db.entities.ScrapeRun

@Dao
abstract class ScrapeRunDao : BaseDao<ScrapeRun> {

    companion object {

        const val SCRAPE_TYPE_KHAN = "khan"

        const val SCRAPE_TYPE_VOA = "voa"

        const val SCRAPE_TYPE_EDRAAK = "edraak"

        const val SCRAPE_TYPE_GDL = "gdl"

        const val SCRAPE_TYPE_CK12 = "ck12"

        const val SCRAPE_TYPE_DDL = "ddl"
    }
}
