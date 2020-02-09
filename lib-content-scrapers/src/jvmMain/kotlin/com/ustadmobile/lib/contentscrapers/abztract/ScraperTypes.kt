import com.ustadmobile.lib.contentscrapers.ddl.*
import com.ustadmobile.lib.contentscrapers.habaybna.HabFrontPageIndexer
import com.ustadmobile.lib.contentscrapers.habaybna.HabPlaylistIndexer
import com.ustadmobile.lib.contentscrapers.habaybna.HabVideoScraper
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanFrontPageIndexer
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanLiteIndexer
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanLiteVideoScraper

data class IndexerMap(var clazz: Class<*>, var defaultUrl: String?)

object ScraperTypes {

    const val DDL_PAGES_INDEXER = "ddlPageIndexer"

    const val DDL_FRONT_PAGE_INDEXER = "ddlFrontPageIndexer"

    const val DDL_LIST_INDEXER = "ddlListIndexer"

    const val DDL_SUBJECT_INDEXER = "ddlSubjectIndexer"

    const val HAB_FRONT_PAGE_INDEXER = "habFrontPageIndexer"

    const val HAB_PLAYLIST_INDEXER = "habPlaylistIndexer"

    const val KHAN_FRONT_PAGE_INDEXER = "khanFrontPageIndexer"

    const val KHAN_LITE_INDEXER = "khanLiteIndexer"

    val indexerTypeMap = mapOf(
            DDL_FRONT_PAGE_INDEXER to IndexerMap(DdlFrontPageIndexer::class.java, "https://www.ddl.af/"),
            DDL_SUBJECT_INDEXER to IndexerMap(DdlSubjectIndexer::class.java, null),
            DDL_PAGES_INDEXER to IndexerMap(DdlPageIndexer::class.java, null),
            DDL_LIST_INDEXER to IndexerMap(DdlListIndexer::class.java, null),
            HAB_FRONT_PAGE_INDEXER to IndexerMap(HabFrontPageIndexer::class.java, "https://www.habaybna.net/"),
            HAB_PLAYLIST_INDEXER to IndexerMap(HabPlaylistIndexer::class.java, null),
            KHAN_FRONT_PAGE_INDEXER to IndexerMap(KhanFrontPageIndexer::class.java, "https://www.khanacademy.org/"),
            KHAN_LITE_INDEXER to IndexerMap(KhanLiteIndexer::class.java, null))


    const val DDL_ARTICLE_SCRAPER = "ddlArticleScraper"

    const val HAB_YOUTUBE_SCRAPER = "habVideoScraper"

    const val KHAN_LITE_VIDEO_SCRAPER = "khanLiteVideoScraper"

    val scraperTypeMap = mapOf(
            DDL_ARTICLE_SCRAPER to DdlContentScraper::class.java,
            HAB_YOUTUBE_SCRAPER to HabVideoScraper::class.java,
            KHAN_LITE_VIDEO_SCRAPER to KhanLiteVideoScraper::class.java)

}