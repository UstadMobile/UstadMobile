package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.db.entities.ScrapeRun

@Dao
abstract class ScrapeRunDao : BaseDao<ScrapeRun> {

    @Query("SELECT scrapeRunUid From ScrapeRun WHERE scrapeType = :scrapeType LIMIT 1")
    abstract fun findPendingRunIdByScraperType(scrapeType: String): Int

    companion object {

        const val SCRAPE_TYPE_KHAN = "khan"

        const val SCRAPE_TYPE_VOA = "voa"

        const val SCRAPE_TYPE_EDRAAK = "edraak"

        const val SCRAPE_TYPE_GDL = "gdl"

        const val SCRAPE_TYPE_CK12 = "ck12"
    }
}
