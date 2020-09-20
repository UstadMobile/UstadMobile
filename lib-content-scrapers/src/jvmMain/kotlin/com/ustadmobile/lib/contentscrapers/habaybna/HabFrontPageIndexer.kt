package com.ustadmobile.lib.contentscrapers.habaybna

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.ScraperConstants.HAB
import com.ustadmobile.lib.contentscrapers.abztract.Indexer
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import org.kodein.di.DI

@ExperimentalStdlibApi
class HabFrontPageIndexer(parentContentEntry: Long, runUid: Int, sqiUid: Int, contentEntryUid: Long, endpoint: Endpoint, di: DI) : Indexer(parentContentEntry, runUid, sqiUid, contentEntryUid, endpoint, di) {

    private val arabicLang = ContentScraperUtil.insertOrUpdateLanguageByTwoCode(languageDao, "ar")

    private var playlistCount = 0

    private lateinit var parentHab: ContentEntry

    override fun indexUrl(sourceUrl: String) {

        parentHab = ContentScraperUtil.createOrUpdateContentEntry(sourceUrl, HAB,
                sourceUrl, HAB, ContentEntry.LICENSE_TYPE_OTHER, arabicLang.langUid, null,
                "Free and open educational resources for Afghanistan", false, HAB,
                "https://www.expo2020dubai.com/-/media/expo2020/expo-live/global-innovators/habaybna/habaybna-logo.png",
                "", "", 0, contentEntryDao)

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, masterRootParent, parentHab, 10)

        createEntryAndQueue("https://www.youtube.com/playlist?list=PLFhWybf5UzoxBVZtZc7tvt8ET5PnRtLYa", "سهى الطبال")
        createEntryAndQueue("https://www.youtube.com/playlist?list=PLFhWybf5Uzowd6hrJuumTVxaJJGRcBdzd", "رنا شعبان")
        createEntryAndQueue("https://www.youtube.com/playlist?list=PLFhWybf5Uzow0P7JQT4ObF3d1jR_Mg7Ym", "رانيا الصايغ")
        createEntryAndQueue("https://www.youtube.com/playlist?list=PLFhWybf5UzoyuW6kiro3Y4KtFmQpzxrLD", "نزار سرايجي - أخصائي علاج باللعب")
        createEntryAndQueue("https://www.youtube.com/playlist?list=PLFhWybf5UzozMUWro7bcx44tgZAhSBIod", "صلاح حديدي - أخصائي النطق واللغة")
        createEntryAndQueue("https://www.youtube.com/playlist?list=PLFhWybf5UzoxNLSQYBqurFu_ZOQw30wDO", "هناء أبوعطية - أخصائية العلاج الوظيفي")
        createEntryAndQueue("https://www.youtube.com/playlist?list=PLFhWybf5Uzoz_FEWSHLXv5lNIpYib2UH3", "زينات أبو شنب")
        createEntryAndQueue("https://www.youtube.com/playlist?list=PLFhWybf5UzowVPdatEhRjPcUfcJ2kE80C", "هالة إبراهيم - إستشارية في الشؤون التربوية الخاصة")

        setIndexerDone(true, 0)
    }

    fun createEntryAndQueue(sourceUrl: String, title: String){

        val playlist = ContentScraperUtil.createOrUpdateContentEntry(sourceUrl.substringAfter("="), title,
                sourceUrl, HAB, ContentEntry.LICENSE_TYPE_OTHER, arabicLang.langUid, null,
                "", false, "", "",
                "", "", 0, contentEntryDao)

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, parentHab, playlist, playlistCount++)

        createQueueItem(sourceUrl, playlist, ScraperTypes.HAB_PLAYLIST_INDEXER, ScrapeQueueItem.ITEM_TYPE_INDEX, parentHab.contentEntryUid)
    }

    override fun close() {

    }

}