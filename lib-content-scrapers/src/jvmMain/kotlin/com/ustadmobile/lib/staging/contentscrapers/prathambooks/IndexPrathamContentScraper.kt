package com.ustadmobile.lib.contentscrapers.prathambooks

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContainerDao
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao
import com.ustadmobile.core.db.dao.LanguageDao
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.ShrinkerUtil
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.sharedse.util.UmZipUtils

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.jsoup.nodes.Element

import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.ProtocolException
import java.net.URISyntaxException
import java.net.URL
import java.util.ArrayList
import java.util.HashMap

import com.ustadmobile.lib.contentscrapers.ContentScraperUtil.deleteETagOrModified

import com.ustadmobile.lib.contentscrapers.ScraperConstants.EPUB_EXT
import com.ustadmobile.lib.contentscrapers.ScraperConstants.ROOT
import com.ustadmobile.lib.contentscrapers.ScraperConstants.USTAD_MOBILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING
import com.ustadmobile.lib.contentscrapers.ScraperConstants.ZIP_EXT
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY
import com.ustadmobile.lib.staging.contentscrapers.replaceMeWithDi
import java.util.function.Consumer


/**
 * Storyweaver has an api for all their epub books.
 * To download each book, i need to have a cookie session id
 * I get session by logging in the website and entering the credentials and retrieving the cookie
 * To get the total number of books,
 * hit the api with just 1 book request and in the json, the total number of books is stored in metadata.hits
 * Call the api again with the request for all books
 * create the url to get the epub, open the url connection and add the cookie session
 *
 *
 * If IOException is thrown, might be because the session expired so login again.
 * otherwise file is downloaded in its folder
 */

class IndexPrathamContentScraper {
    internal var prefixUrl = "https://storyweaver.org.in/api/v1/books-search?page="

    internal var prefixEPub = "https://storyweaver.org.in/v0/stories/download-story/"

    internal var signIn = "https://storyweaver.org.in/api/v1/users/sign_in"

    private lateinit var gson: Gson
    private lateinit var contentEntryDao: ContentEntryDao
    private lateinit var contentParentChildJoinDao: ContentEntryParentChildJoinDao
    private lateinit var prathamParentEntry: ContentEntry
    private lateinit var languageDao: LanguageDao
    private lateinit var containerDao: ContainerDao
    private lateinit var db: UmAppDatabase
    private lateinit var repository: UmAppDatabase
    private lateinit var containerDir: File

    @Throws(IOException::class, URISyntaxException::class)
    fun findContent(destinationDir: File, containerDir: File) {

        destinationDir.mkdirs()
        containerDir.mkdirs()
        this.containerDir = containerDir
        ContentScraperUtil.setChromeDriverLocation()
        val cookie = loginPratham()


        //db = UmAppDatabase.getInstance(Any(), replaceMeWithDi())
        repository = db //db!!.getRepository("https://localhost", "")
        contentEntryDao = repository!!.contentEntryDao
        contentParentChildJoinDao = repository!!.contentEntryParentChildJoinDao
        containerDao = repository!!.containerDao
        languageDao = repository!!.languageDao

        val englishLang = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, "English")


        val masterRootParent = ContentScraperUtil.createOrUpdateContentEntry(ROOT, USTAD_MOBILE,
                ROOT, USTAD_MOBILE, LICENSE_TYPE_CC_BY, englishLang.langUid, null,
                "", false, "", "",
                "", "", 0, contentEntryDao!!)


        prathamParentEntry = ContentScraperUtil.createOrUpdateContentEntry("https://storyweaver.org.in/", "Pratham Books",
                "https://storyweaver.org.in/", PRATHAM, LICENSE_TYPE_CC_BY, englishLang.langUid, null,
                "Every Child in School & Learning Well", false, "",
                "https://prathambooks.org/wp-content/uploads/2018/04/Logo-black.png", "", "", 0, contentEntryDao!!)

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, masterRootParent, prathamParentEntry!!, 16)

        gson = GsonBuilder().disableHtmlEscaping().create()

        downloadPrathamContentList(generatePrathamUrl(1.toString()), cookie, destinationDir)

    }


    @Throws(URISyntaxException::class, IOException::class)
    private fun downloadPrathamContentList(contentUrl: URL, cookie: String, destinationDir: File) {
        var cookie = cookie

        val contentBooksList = gson!!.fromJson(IOUtils.toString(contentUrl.toURI(), UTF_ENCODING), BooksResponse::class.java)

        if (contentBooksList.data!!.isEmpty()) {
            return
        }

        var retry = 0
        UMLogUtil.logTrace("Found a new list of items: " + contentBooksList.data!!.size)
        var contentCount = 0
        while (contentCount < contentBooksList.data!!.size) {
            var connection: HttpURLConnection? = null
            var resourceFolder: File? = null
            try {

                val data = contentBooksList.data!![contentCount]

                val epubUrl = generatePrathamEPubFileUrl(data.slug)

                UMLogUtil.logTrace("Start scrape for " + data.slug!!)

                val lang = getLangCode(data.language!!)
                val langEntity = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, lang)
                resourceFolder = File(destinationDir, data.id.toString())
                resourceFolder.mkdirs()
                val contentEntry = ContentScraperUtil.createOrUpdateContentEntry(data.slug!!, data.title,
                        epubUrl.toString(), PRATHAM, LICENSE_TYPE_CC_BY, langEntity.langUid, null,
                        data.description, true, "", data.coverImage!!.sizes!![0].url,
                        "", "", 0, contentEntryDao!!)

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!,
                        prathamParentEntry!!, contentEntry, contentCount)

                connection = epubUrl.openConnection() as HttpURLConnection
                connection.setRequestProperty("Cookie", cookie)
                connection.connect()

                val content = File(resourceFolder, data.slug!! + ZIP_EXT)
                var isUpdated = ContentScraperUtil.isFileModified(connection, resourceFolder, data.id.toString())

                if (!isUpdated) {
                    contentCount++
                    continue
                }

                val tmpDir = File(UMFileUtil.stripExtensionIfPresent(content.path))
                if (ContentScraperUtil.fileHasContent(tmpDir)) {
                    FileUtils.deleteDirectory(tmpDir)
                }

                try {
                    FileUtils.copyInputStreamToFile(connection.inputStream, content)

                    UMLogUtil.logTrace("downloaded the zip: " + content.path)

                    UmZipUtils.unzip(content, resourceFolder)

                    UMLogUtil.logTrace("UnZipped the zip ")

                    val epub = File(resourceFolder, data.slug!! + EPUB_EXT)
                    val options = ShrinkerUtil.EpubShrinkerOptions()
                    options.styleElementHelper = { it ->
                        val text = it.text()
                        if (text.startsWith("@font-face") || text.startsWith(".english")) {
                            ShrinkerUtil.STYLE_OUTSOURCE_TO_LINKED_CSS
                        } else {
                            ShrinkerUtil.STYLE_DROP
                        }
                    }

                    options.styleElementHelper = { styleElement ->
                        val text = styleElement.text()
                        if (text.startsWith("@font-face") || text.startsWith(".english")) {
                            ShrinkerUtil.STYLE_OUTSOURCE_TO_LINKED_CSS
                        } else {
                            ShrinkerUtil.STYLE_DROP
                        }
                    }
                    options.editor = { document ->
                        val elements = document.select("p")
                        val elementsToRemove = ArrayList<Element>()
                        for (element in elements) {
                            if (element.text().isEmpty()) {
                                elementsToRemove.add(element)
                            }
                        }
                        elementsToRemove.forEach(Consumer<Element> { it.remove() })
                        document.head().append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable-no\" />")
                        document
                    }
                    options.linkHelper = {
                        IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.PRATHAM_CSS_HELPER), UTF_ENCODING)
                    }
                    val tmpFolder = ShrinkerUtil.shrinkEpub(epub, options)
                    UMLogUtil.logTrace("Shrunk the Epub")
                    ContentScraperUtil.insertContainer(containerDao!!, contentEntry,
                            true, ScraperConstants.MIMETYPE_EPUB,
                            tmpFolder.lastModified(), tmpFolder,
                            db, repository, containerDir)
                    UMLogUtil.logTrace("Completed: Created Container")
                    ContentScraperUtil.deleteFile(content)
                    ContentScraperUtil.deleteFile(epub)

                } catch (io: IOException) {
                    cookie = loginPratham()
                    retry++
                    deleteETagOrModified(resourceFolder, data.id.toString())
                    if (retry == 2) {
                        UMLogUtil.logError("Error for book " + data.title + " with id " + data.slug)
                        UMLogUtil.logInfo(ExceptionUtils.getStackTrace(io))
                        retry = 0
                        contentCount++
                        continue
                    }
                    contentCount--
                    contentCount++
                    continue
                } finally {
                    connection.disconnect()
                }
                retry = 0


            } catch (e: Exception) {
                UMLogUtil.logError("Error saving book " + contentBooksList.data!![contentCount].slug!!)
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                if (resourceFolder != null) {
                    deleteETagOrModified(resourceFolder, resourceFolder.name)
                }
            } finally {
                connection?.disconnect()
            }
            contentCount++

        }

        downloadPrathamContentList(generatePrathamUrl((++contentBooksList.metadata!!.page).toString()), cookie, destinationDir)

    }


    private fun getLangCode(language: String): String {
        val list = language.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return list[0]
    }

    @Throws(MalformedURLException::class)
    fun generatePrathamEPubFileUrl(resourceId: String?): URL {
        return URL(prefixEPub + resourceId + EPUB_EXT)
    }

    @Throws(MalformedURLException::class)
    fun generatePrathamUrl(number: String): URL {
        return URL("$prefixUrl$number&per_page=24")
    }

    fun loginPratham(): String {
        var conn: HttpURLConnection? = null
        var out: DataOutputStream? = null
        try {
            val selectedParams = HashMap<String, String>()
            selectedParams["api_v1_user[email]"] = GMAIL
            selectedParams["api_v1_user[password]"] = PASS
            selectedParams["api_v1_user[remember_me]"] = false.toString()
            val selectedRequestParams = ContentScraperUtil.convertMapToStringBuffer(selectedParams)
            val url = URL(signIn)
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            out = DataOutputStream(conn.outputStream)
            out.writeBytes(selectedRequestParams.toString())
            out.flush()
            out.close()
            conn.connect()
            val cookie = conn.getHeaderField("Set-Cookie")
            return cookie.substring(cookie.indexOf("_session"), cookie.indexOf(";"))
        } catch (e: ProtocolException) {
            UMLogUtil.logError("Protocol Error for login to Pratham")
        } catch (e: IOException) {
            UMLogUtil.logError("IO Error for login to Pratham")
        } finally {
            conn?.disconnect()
            out?.close()
        }

        return ""
    }

    companion object {

        private val PRATHAM = "Pratham"
        private val GMAIL = "samihmustafa@gmail.com"
        private val PASS = "reading123"

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 2) {
                System.err.println("Usage: <file destination><file container><optional log{trace, debug, info, warn, error, fatal}>")
                System.exit(1)
            }
            UMLogUtil.setLevel(if (args.size == 3) args[2] else "")
            try {
                IndexPrathamContentScraper().findContent(File(args[0]), File(args[1]))
            } catch (e: IOException) {
                UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logFatal("Exception running findContent pratham")
            } catch (e: URISyntaxException) {
                UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logFatal("Exception running findContent pratham")
            }

        }
    }


}
