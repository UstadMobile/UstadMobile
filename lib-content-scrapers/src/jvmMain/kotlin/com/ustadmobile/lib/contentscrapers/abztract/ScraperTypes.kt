
import com.ustadmobile.lib.contentscrapers.abztract.UrlScraper
import com.ustadmobile.lib.contentscrapers.abztract.YoutubeChannelIndexer
import com.ustadmobile.lib.contentscrapers.abztract.YoutubePlaylistIndexer
import com.ustadmobile.lib.contentscrapers.abztract.YoutubeScraper
import com.ustadmobile.lib.contentscrapers.ddl.*
import com.ustadmobile.lib.contentscrapers.googledrive.GoogleDriveScraper
import com.ustadmobile.lib.contentscrapers.habaybna.HabFrontPageIndexer
import com.ustadmobile.lib.contentscrapers.khanacademy.*

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

    const val KHAN_FULL_INDEXER = "khanFullIndexer"

    const val KHAN_TOPIC_INDEXER = "khanTopicIndexer"

    const val KHAN_CHANNEL_INDEXER = "khanChannelIndexer"

    const val KHAN_PLAYLIST_INDEXER = "khanPlaylistIndexer"

    const val YOUTUBE_CHANNEL_INDEXER = "youtubeChannelIndexer"

    const val YOUTUBE_PLAYLIST_INDEXER = "youtubePlaylistIndexer"

    const val APACHE_INDEXER = "apacheIndexer"

    const val FOLDER_INDEXER = "folderIndexer"


    val indexerTypeMap = mapOf(
            DDL_FRONT_PAGE_INDEXER to IndexerMap(DdlFrontPageIndexer::class.java, "https://www.ddl.af/"),
            DDL_SUBJECT_INDEXER to IndexerMap(DdlSubjectIndexer::class.java, null),
            DDL_PAGES_INDEXER to IndexerMap(DdlPageIndexer::class.java, null),
            DDL_LIST_INDEXER to IndexerMap(DdlListIndexer::class.java, null),
            HAB_FRONT_PAGE_INDEXER to IndexerMap(HabFrontPageIndexer::class.java, "https://www.habaybna.net/"),
            KHAN_FRONT_PAGE_INDEXER to IndexerMap(KhanFrontPageIndexer::class.java, "https://www.khanacademy.org/"),
            KHAN_LITE_INDEXER to IndexerMap(KhanLiteIndexer::class.java, null),
            KHAN_FULL_INDEXER to IndexerMap(KhanFullIndexer::class.java, null),
            KHAN_TOPIC_INDEXER to IndexerMap(KhanTopicIndexer::class.java, null),
            KHAN_CHANNEL_INDEXER to IndexerMap(KhanYoutubeChannelIndexer::class.java, null),
            YOUTUBE_CHANNEL_INDEXER to IndexerMap(YoutubeChannelIndexer::class.java, null),
            YOUTUBE_PLAYLIST_INDEXER to IndexerMap(YoutubePlaylistIndexer::class.java, null))


    const val DDL_ARTICLE_SCRAPER = "ddlArticleScraper"

    const val KHAN_LITE_VIDEO_SCRAPER = "khanLiteVideoScraper"

    const val KHAN_FULL_VIDEO_SCRAPER = "khanFullVideoScraper"

    const val KHAN_FULL_EXERCISE_SCRAPER = "khanFullExerciseScraper"

    const val KHAN_FULL_ARTICLE_SCRAPER = "khanFullArticleScraper"

    const val YOUTUBE_VIDEO_SCRAPER = "youtubeVideoScraper"

    const val URL_SCRAPER = "urlScraper"

    const val FOLDER_SCRAPER = "folderScraper"

    const val GOOGLE_DRIVE_SCRAPE = "googleDriveScraper"

    val scraperTypeMap = mapOf(
            DDL_ARTICLE_SCRAPER to DdlContentScraper::class.java,
            KHAN_FULL_VIDEO_SCRAPER to KhanVideoScraper::class.java,
            KHAN_FULL_ARTICLE_SCRAPER to KhanArticleScraper::class.java,
            KHAN_FULL_EXERCISE_SCRAPER to KhanExerciseScraper::class.java,
            KHAN_LITE_VIDEO_SCRAPER to KhanLiteVideoScraper::class.java,
            YOUTUBE_VIDEO_SCRAPER to YoutubeScraper::class.java,
            URL_SCRAPER to UrlScraper::class.java,
            GOOGLE_DRIVE_SCRAPE to GoogleDriveScraper::class.java)

}