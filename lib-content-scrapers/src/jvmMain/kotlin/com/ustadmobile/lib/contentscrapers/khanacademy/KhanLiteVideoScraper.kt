package com.ustadmobile.lib.contentscrapers.khanacademy

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.abztract.Scraper
import java.io.File

class KhanLiteVideoScraper(containerDir: File, db: UmAppDatabase, contentEntryUid: Long) : Scraper(containerDir, db, contentEntryUid) {


    override fun scrapeUrl(sourceUrl: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun close() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getMp4LowUrl(videoId: String): String {
        return "https://cdn.kastatic.org/ka-youtube-converted/$videoId.mp4-low/$videoId-low.mp4"
    }

    fun getMp4Url(videoId: String): String {
        return "https://cdn.kastatic.org/ka-youtube-converted/$videoId.mp4/$videoId.mp4"
    }

}