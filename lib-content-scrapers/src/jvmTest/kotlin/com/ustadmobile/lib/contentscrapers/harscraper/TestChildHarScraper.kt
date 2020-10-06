package com.ustadmobile.lib.contentscrapers.harscraper

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.lib.contentscrapers.abztract.HarScraper
import org.kodein.di.DI

@ExperimentalStdlibApi
class TestChildHarScraper(contentEntryUid: Long, endpoint: Endpoint, di: DI) : HarScraper(contentEntryUid, 0, 0, endpoint, di) {

    override fun scrapeUrl(sourceUrl: String) {
        startHarScrape(sourceUrl, null){
           true
        }
    }


}
