package com.ustadmobile.lib.contentscrapers.habaybna

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.abztract.YoutubeScraper
import java.io.File
import java.lang.Exception

class HabVideoScraper(containerDir: File, db: UmAppDatabase, contentEntryUid: Long, sqiUid: Int) : YoutubeScraper(containerDir, db, contentEntryUid, sqiUid) {


    override fun scrapeUrl(sourceUrl: String) {

        try {
            scrapeYoutubeLink(sourceUrl)
        } catch (e: Exception) {
            hideContentEntry()
            throw e
        }

    }


}