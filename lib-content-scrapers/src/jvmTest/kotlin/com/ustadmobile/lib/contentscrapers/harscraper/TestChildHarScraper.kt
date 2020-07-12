package com.ustadmobile.lib.contentscrapers.harscraper

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.abztract.HarScraper
import java.io.File

@ExperimentalStdlibApi
class TestChildHarScraper(containerDir: File, db: UmAppDatabase, contentEntryUid: Long) : HarScraper(containerDir, db, contentEntryUid, 0) {

    override fun scrapeUrl(sourceUrl: String) {
        startHarScrape(sourceUrl, null){
           true
        }
    }


}