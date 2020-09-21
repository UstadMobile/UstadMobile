package com.ustadmobile.lib.contentscrapers.khanacademy

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.abztract.Indexer
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import org.kodein.di.DI

@ExperimentalStdlibApi
class KhanYoutubeChannelIndexer(parentContentEntryUid: Long, runUid: Int, sqiUid: Int, contentEntryUid: Long, endpoint: Endpoint, di: DI) : Indexer(parentContentEntryUid, runUid, sqiUid, contentEntryUid, endpoint, di) {

    private var playlistCount = 0
    private lateinit var parentEntry: ContentEntry

    override fun indexUrl(sourceUrl: String) {

        val khanEntry = getKhanEntry(englishLang, contentEntryDao)

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, parentContentEntry, khanEntry, 12)

        parentEntry = createKangLangEntry("ps", "Pashto", "https://ps.khanacademy.org/", db)
        hideContentEntry(parentEntry.contentEntryUid)

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, khanEntry, parentEntry, 0)

        createEntryAndQueue("https://www.youtube.com/playlist?list=PLiCgDNH6P2K7zuKn0DHPZFCNAjL-dfA6B", "لومړۍ ریاضي (Early Math)")
        createEntryAndQueue("https://www.youtube.com/playlist?list=PLiCgDNH6P2K4R2kjpTP7WLVWh3aPqLT69", "ریاضي (Math)")
        createEntryAndQueue("https://www.youtube.com/playlist?list=PLiCgDNH6P2K4AVG28ujDcV5SGGeOa2TdE", "دوهم تولګی (Second Grade)")
        createEntryAndQueue("https://www.youtube.com/playlist?list=PLiCgDNH6P2K6xn9NxPrXIvO2MIvXGwwYl", "دریم ټولګی (Third Grade)")
        createEntryAndQueue("https://www.youtube.com/playlist?list=PLiCgDNH6P2K51zAikguRwCsKx7g0SU01-", "څلورم ټولګی (Fourth Grade)")

        setIndexerDone(true, 0)
    }

    fun createEntryAndQueue(sourceUrl: String, title: String){

        val playlist = ContentScraperUtil.createOrUpdateContentEntry(sourceUrl.substringAfter("="), title,
                sourceUrl, ScraperConstants.KHAN, ContentEntry.LICENSE_TYPE_CC_BY_NC, parentEntry.primaryLanguageUid, null,
                "", false, "", "",
                "", "", 0, contentEntryDao)

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, parentEntry, playlist, playlistCount++)

        createQueueItem(sourceUrl, playlist, ScraperTypes.KHAN_PLAYLIST_INDEXER, ScrapeQueueItem.ITEM_TYPE_INDEX, parentEntry.contentEntryUid)
    }
    override fun close() {

    }
}