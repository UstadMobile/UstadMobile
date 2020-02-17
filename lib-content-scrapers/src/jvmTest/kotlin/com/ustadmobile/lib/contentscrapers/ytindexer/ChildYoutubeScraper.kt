package com.ustadmobile.lib.contentscrapers.ytindexer

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.abztract.YoutubeScraper
import java.io.File

class ChildYoutubeScraper(containerDir: File, db: UmAppDatabase, contentEntryUid: Long, sqiUid: Int) : YoutubeScraper(containerDir, db, contentEntryUid, sqiUid) {
    override fun scrapeUrl(sourceUrl: String) {

    }
}