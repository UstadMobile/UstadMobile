package com.ustadmobile.lib.contentscrapers.ytindexer

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.abztract.YoutubeScraper
import org.kodein.di.DI
import java.io.File

@ExperimentalStdlibApi
class ChildYoutubeScraper(contentEntryUid: Long, sqiUid: Int, endpoint: Endpoint, di: DI) : YoutubeScraper(contentEntryUid, sqiUid, 0, endpoint, di) {
    override fun scrapeUrl(sourceUrl: String) {

    }
}
