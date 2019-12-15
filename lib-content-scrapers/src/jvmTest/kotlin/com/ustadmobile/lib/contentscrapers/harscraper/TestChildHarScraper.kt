package com.ustadmobile.lib.contentscrapers.harscraper

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.abztract.HarScraper
import java.io.File

class TestChildHarScraper(containerDir: File, db: UmAppDatabase, contentEntryUid: Long) : HarScraper(containerDir, db, contentEntryUid) {

    override fun isContentUpdated(): Boolean {
       return true
    }

    override fun scrapeUrl(url: String, tmpLocation: File) {
        startHarScrape(url, null){
            isContentUpdated()
        }
    }


}