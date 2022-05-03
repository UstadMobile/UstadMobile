package com.ustadmobile.lib.contentscrapers.africanbooks

import com.google.gson.GsonBuilder
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.lib.contentscrapers.*

import com.ustadmobile.lib.contentscrapers.ScraperConstants.TIME_OUT_SELENIUM
import com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin
import com.ustadmobile.lib.db.entities.LanguageVariant
import com.ustadmobile.lib.staging.contentscrapers.replaceMeWithDi
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.apache.commons.lang3.StringEscapeUtils
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.*
import java.net.MalformedURLException
import java.net.URL
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.time.Duration
import java.util.*


/**
 * African Storybooks support many languages. They can all be found in the source code on https://www.africanstorybook.org/booklist.php
 * by searching for scripts that contain the word "<option></option> ". Each of their supported Languages haves ids which is put in a hashmap for easier access.
 *
 *
 * African story books can all be found in https://www.africanstorybook.org/booklist.php inside a script
 * To get all the books, need to read the source line by line.
 * To get the book, the line starts with parent.bookItems and the information is between curly braces { } in the format of JSON
 * Use Gson to parse the object and add to the final list
 *
 *
 * Iterate through the list, For each book:-
 * 1. Each storybook have translations which can be found in /reader.php?id=bookId
 * By using css selector li#accordianRelatedStories div.accordion-item-content a
 * you can get each translation and add a relation in the database.
 * you need to hit 3 urls
 *
 *
 * 2. /myspace/publish/epub.php?id=bookId and /make/publish/epub.php?id=bookId
 * needs to be opened using selenium and you need to wait for them to load
 * Once loaded call the url with /read/downloadepub.php?id=bookId and downloading for the epub can start
 * Epubs can fail but a retry policy of 2 is enough to get the file.
 *
 *
 * 3. Once downloaded, some epubs have some missing information
 * Open the epub, find description and image property and updateState them
 * We also need to increase the font for the epub and this is done by modifying the css and replacing the existing
 * Move on to next epub until list is complete
 */

class AsbScraper {
    private val COVER_URL = "https://www.africanstorybook.org/illustrations/covers/"

    val africanStoryBookUrl: String
        get() {
            return "https://www.africanstorybook.org/"
        }


    @Throws(IOException::class)
    fun findContent(destinationDir: File, containerDir: File) {

        val africanBooksUrl = generateURL()

        //Replace this with DI
        lateinit var db: UmAppDatabase
        //val db = UmAppDatabase.getInstance(Any(), replaceMeWithDi())
        val repository = db //db.getRepository("https://localhost", "");
        val contentEntryDao = repository.contentEntryDao
        val contentParentChildJoinDao = repository.contentEntryParentChildJoinDao
        val categorySchemeDao = repository.contentCategorySchemaDao
        val categoryDao = repository.contentCategoryDao
        val contentCategoryJoinDao = repository.contentEntryContentCategoryJoinDao
        val languageDao = repository.languageDao
        val variantDao = repository.languageVariantDao
        val relatedEntryJoinDao = repository.contentEntryRelatedEntryJoinDao

        val containerDao = repository.containerDao


        val url = africanStoryBookUrl

        val html = Jsoup.connect(url).get()

        val langMap = HashMap<String, String>()
        val scriptList = html.getElementsByTag("script")
        for (script in scriptList) {

            for (node in script.dataNodes()) {

                if (node.wholeData.contains("<option")) {

                    val data = node.wholeData

                    val langDoc = Jsoup.parse(data.substring(data.indexOf("<option "), data.lastIndexOf("</option>") + 8))
                    val langList = langDoc.getElementsByTag("option")
                    for (lang in langList) {

                        val id = lang.attr("value")
                        val value = StringUtils.capitalize(lang.text().toLowerCase())

                        var variant = ""
                        var langValue = value
                        if (value.contains("(")) {
                            variant = StringUtils.capitalize(value.substring(value.indexOf("(") + 1, value.indexOf(")")))
                            langValue = value.substring(0, value.indexOf("(")).trim { it <= ' ' }
                        }
                        langMap[id] = langValue
                        val langEntity = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, langValue)

                        if (!variant.isEmpty()) {
                            ContentScraperUtil.insertOrUpdateLanguageVariant(variantDao, variant, langEntity)
                        }
                    }
                }
            }

        }

        val englishLang = languageDao.findByTwoCode(ScraperConstants.ENGLISH_LANG_CODE)

        val masterRootParent = ContentScraperUtil.createOrUpdateContentEntry(ScraperConstants.ROOT, ScraperConstants.USTAD_MOBILE, ScraperConstants.ROOT,
                ScraperConstants.USTAD_MOBILE, ContentEntry.LICENSE_TYPE_CC_BY, englishLang!!.langUid, null, "",
                false, "", "", "", "", 0, contentEntryDao)


        val asbParentEntry = ContentScraperUtil.createOrUpdateContentEntry("https://www.africanstorybook.org/", AFRICAN_STORY_BOOKS,
                "https://www.africanstorybook.org/", AFRICAN_STORY_BOOKS, ContentEntry.LICENSE_TYPE_CC_BY,
                englishLang.langUid, null, "Open access to picture storybooks in the languages of Africa. \n For children's literacy, enjoyment and imagination.", false, "",
                "https://www.africanstorybook.org/img/asb120.png", "", "", 0, contentEntryDao)


        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, masterRootParent, asbParentEntry, 0)

        val inputStreamOfBooks = africanBooksUrl.openStream()
        val africanBooksList = parseBooklist(inputStreamOfBooks)

        var bookObj: AfricanBooksResponse
        ContentScraperUtil.setChromeDriverLocation()
        val driver = ContentScraperUtil.setupChrome(true)
        val waitDriver = WebDriverWait(driver, TIME_OUT_SELENIUM)
        var retry = 0

        var i = 0
        while (i < africanBooksList.size) {
            //Download the EPUB itself
            bookObj = africanBooksList[i]
            val bookId = bookObj.id
            val ePubFile = File(destinationDir, "asb$bookId.epub")
            val epubUrl = generateEPubUrl(africanBooksUrl, bookId!!)
            val publishUrl = generatePublishUrl(africanBooksUrl, bookId)
            val makeUrl = generateMakeUrl(africanBooksUrl, bookId)
            val modifiedFile = File(destinationDir, bookId + ScraperConstants.LAST_MODIFIED_TXT)
            UMLogUtil.logTrace("Started with book id $bookId")
            try {

                driver.get(publishUrl.toString())
                ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver)

                driver.get(makeUrl.toString())
                ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver)

                if (bookObj.lang!!.contains(",")) {
                    bookObj.lang = bookObj.lang!!.split((",").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                }

                val langName = langMap[bookObj.lang!!]

                var variant = ""
                var langValue = langName
                if (langName != null && langName.contains("(")) {
                    variant = StringUtils.capitalize(langName!!.substring(langName!!.indexOf("(") + 1, langName!!.indexOf(")")))
                    langValue = langName!!.substring(0, langName!!.indexOf("(")).trim { it <= ' ' }
                }

                val language = languageDao.findByName(langValue!!)

                var languageVariant: LanguageVariant? = null
                if (language != null && variant.isNotEmpty()) {
                    languageVariant = ContentScraperUtil.insertOrUpdateLanguageVariant(variantDao, variant, language)
                }
                val sourceUrl = epubUrl.path + (if ((epubUrl.query != null && epubUrl.query.isNotEmpty())) "?" + epubUrl.query else "")

                val childEntry = ContentScraperUtil.createOrUpdateContentEntry(sourceUrl, bookObj.title, sourceUrl, AFRICAN_STORY_BOOKS, ContentEntry.LICENSE_TYPE_CC_BY,
                        if (language != null) language!!.langUid else 0L, if (languageVariant == null) null else languageVariant!!.langVariantUid, bookObj.summary, true, bookObj.author, getCoverUrl(bookId),
                        "", "", 0, contentEntryDao)

                val readerDoc = Jsoup.connect(generateReaderUrl(africanBooksUrl, bookId)).get()

                val langList = readerDoc.select("li#accordianRelatedStories div.accordion-item-content a")

                // find the list of translations for the book we are currently on
                var originalEntry = childEntry
                val relatedEntries = ArrayList<ContentEntry>()
                for (element in langList) {

                    var lang = ""
                    try {
                        var id = element.attr("onclickss")
                        id = id.substring(id.indexOf("(") + 1, id.lastIndexOf(")"))
                        val value = element.selectFirst("span")?.text()

                        lang = value ?: ""
                        lang = StringUtils.remove(lang, "(Original)")
                        lang = StringUtils.remove(lang, "(Adaptation)")
                        lang = StringUtils.remove(lang, "(Translation)").trim { it <= ' ' }.toLowerCase()
                        lang = StringUtils.capitalize(lang)

                        var relatedVariant = ""
                        var relatedLangValue = lang
                        if (lang.contains("(")) {
                            relatedVariant = StringUtils.capitalize(lang.substring(lang.indexOf("(") + 1, lang.indexOf(")")))
                            relatedLangValue = lang.substring(0, lang.indexOf("(")).trim({ it <= ' ' })
                        }

                        var relatedLanguage = languageDao.findByName(relatedLangValue)
                        if (relatedLanguage == null) {
                            relatedLanguage = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, lang)
                        }
                        var relatedLanguageVariant: LanguageVariant? = null
                        if (!variant.isEmpty()) {
                            relatedLanguageVariant = ContentScraperUtil.insertOrUpdateLanguageVariant(variantDao, relatedVariant, relatedLanguage)
                        }

                        val content = generateEPubUrl(africanBooksUrl, id)
                        val relatedSourceUrl = content.getPath() + (if ((content.getQuery() != null && !content.getQuery().isEmpty())) "?" + content.getQuery() else "")
                        var contentEntry = contentEntryDao.findBySourceUrl(relatedSourceUrl)
                        if (contentEntry == null) {
                            contentEntry = ContentEntry()
                            contentEntry!!.sourceUrl = relatedSourceUrl
                            contentEntry!!.primaryLanguageUid = relatedLanguage!!.langUid
                            if (relatedLanguageVariant != null) {
                                contentEntry!!.languageVariantUid = relatedLanguageVariant!!.langVariantUid
                            }
                            contentEntry!!.leaf = true
                            contentEntry!!.contentEntryUid = contentEntryDao.insert(contentEntry)
                        }
                        relatedEntries.add(contentEntry)

                        if (value?.contains("Original") == true) {
                            originalEntry = contentEntry
                        }
                    } catch (e: NullPointerException) {
                        UMLogUtil.logError("A translated book could not be parsed " + lang + "for book " + bookObj.title)
                    }

                }

                for (entry in relatedEntries) {

                    ContentScraperUtil.insertOrUpdateRelatedContentJoin(relatedEntryJoinDao, entry, originalEntry,
                            ContentEntryRelatedEntryJoin.REL_TYPE_TRANSLATED_VERSION)

                }
                ContentScraperUtil.insertOrUpdateRelatedContentJoin(relatedEntryJoinDao, childEntry, originalEntry,
                        ContentEntryRelatedEntryJoin.REL_TYPE_TRANSLATED_VERSION)

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, asbParentEntry, childEntry, i)

                val schema = ContentScraperUtil.insertOrUpdateSchema(categorySchemeDao,
                        "African Storybooks Reading Level", "africanstorybooks/reading/")

                val category = ContentScraperUtil.insertOrUpdateCategoryContent(categoryDao, schema, "Reading Level " + bookObj.level!!)
                ContentScraperUtil.insertOrUpdateChildWithMultipleCategoriesJoin(contentCategoryJoinDao, category, childEntry)

                val isUpdated = ContentScraperUtil.isFileContentsUpdated(modifiedFile, bookObj.date!!)

                if (!isUpdated) {
                    i++
                    continue
                }

                val tmpFolder = File(UMFileUtil.stripExtensionIfPresent(ePubFile.name))
                if (ContentScraperUtil.fileHasContent(tmpFolder)) {
                    FileUtils.deleteDirectory(tmpFolder)
                }


                FileUtils.copyURLToFile(epubUrl, ePubFile)
                UMLogUtil.logTrace("Got the epub")
                if (ePubFile.length() == 0L) {
                    ContentScraperUtil.deleteFile(modifiedFile)
                    retry++
                    if (retry == 3) {
                        retry = 0
                        UMLogUtil.logError(ePubFile.name + " size 0 bytes after 3rd try: failed! for title " + bookObj.title)
                        i++
                        continue
                    }
                    i--
                    driver.manage().deleteAllCookies()
                    i++
                    continue
                }
                retry = 0

                if (ContentScraperUtil.fileHasContent(ePubFile)) {
                    updateAsbEpub(bookObj, ePubFile)
                }

                val options = ShrinkerUtil.EpubShrinkerOptions()
                options.linkHelper = {
                    IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.ASB_CSS_HELPER), UTF_ENCODING)
                }
                val tmpDir = ShrinkerUtil.shrinkEpub(ePubFile, options)
                UMLogUtil.logTrace("Shrunk Epub")
                ContentScraperUtil.insertContainer(containerDao, childEntry, true,
                        ScraperConstants.MIMETYPE_EPUB, ePubFile.lastModified(),
                        tmpDir, db, repository, containerDir)
                UMLogUtil.logTrace("Completed: Created Container")
                ContentScraperUtil.deleteFile(ePubFile)

            } catch (e: Exception) {
                ContentScraperUtil.deleteFile(modifiedFile)
                retry++
                if (retry == 3) {
                    retry = 0
                    UMLogUtil.logError("Exception downloading/checking after 3rd try : " + ePubFile.getName() + " with title " + bookObj.title)
                    i++
                    continue
                }
                i--
                driver.manage().deleteAllCookies()
            }

            i++
        }
        driver.close()
        driver.quit()
    }

    @Throws(MalformedURLException::class)
    fun generateReaderUrl(url: URL, bookId: String?): String {
        return URL(url, "/reader.php?id=" + bookId!!).toString()
    }

    fun getCoverUrl(bookId: String?): String {
        return COVER_URL + bookId + ScraperConstants.PNG_EXT
    }

    @Throws(MalformedURLException::class)
    fun generatePublishUrl(africanBooksUrl: URL, bookId: String?): URL {
        return URL(africanBooksUrl, "/myspace/publish/epub.php?id=$bookId")
    }

    @Throws(MalformedURLException::class)
    fun generateMakeUrl(africanBooksUrl: URL, bookId: String): URL {
        return URL(africanBooksUrl, "/make/publish/epub.php?id=$bookId")
    }

    @Throws(MalformedURLException::class)
    fun generateEPubUrl(africanBooksUrl: URL, bookId: String): URL {
        return URL(africanBooksUrl, "/read/downloadepub.php?id=$bookId")
    }


    @Throws(MalformedURLException::class)
    fun generateURL(): URL {
        return URL("https://www.africanstorybook.org/booklist.php")
    }


    @Throws(IOException::class)
    protected fun parseBooklist(booklistIn: InputStream): List<AfricanBooksResponse> {

        val gson = GsonBuilder().disableHtmlEscaping().create()

        val reader = BufferedReader(InputStreamReader(booklistIn, "UTF-8"))
        val retVal = ArrayList<AfricanBooksResponse>()
        var inList = false
        var currentObj: AfricanBooksResponse
        var parsedCounter = 0
        var failCounter = 0

        reader.lineSequence().forEach {
            if (!inList && !it.startsWith("<script>"))
                return@forEach

            var line = it
            if (line.startsWith("<script>")) {
                line = line.substring("<script>".length)
                inList = true
            }

            if (line.startsWith("parent.bookItems")) {
                line = StringEscapeUtils.unescapeHtml4(line)
                var jsonStr = line.substring(line.indexOf("({") + 1,
                        line.indexOf("})") + 1)
                jsonStr = jsonStr.replace("\n", " ")
                jsonStr = jsonStr.replace("\r", " ")
                try {
                    currentObj = gson.fromJson(jsonStr, AfricanBooksResponse::class.java)
                    retVal.add(currentObj)
                    parsedCounter++
                } catch (e: Exception) {
                    UMLogUtil.logError("Failed to parse: $line")
                    UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                    failCounter++
                }

            }
        }

        UMLogUtil.logInfo("Parsed $parsedCounter / failed $failCounter items from booklist.php")

        reader.close()
        booklistIn.close()

        return retVal
    }

    /**
     * EPUBs from ASB don't contain the description that is in the booklist.php file. We need to add that.
     * We also need to check to make sure the cover image is correctly specified. Sometimes the properties='cover-image'
     * is not specified on the EPUB provided by African Story Book so we need to add that.
     *
     * @param booklistEntry
     * @param epubFile
     */
    fun updateAsbEpub(booklistEntry: AfricanBooksResponse, epubFile: File) {
        var zipFs: FileSystem? = null

        var opfReader: BufferedReader? = null
        try {

            zipFs = FileSystems.newFileSystem(epubFile.toPath(), ClassLoader.getSystemClassLoader())
            opfReader = BufferedReader(
                    InputStreamReader(Files.newInputStream(zipFs!!.getPath("content.opf")), UTF_ENCODING))
            val opfModBuffer = StringBuffer()
            var modified = false
            var hasDescription = false

            val descTag = ("<dc:description>" + StringEscapeUtils.escapeXml(booklistEntry.summary)
                    + "</dc:description>")
           opfReader.lineSequence().forEach {
                if (it.contains("dc:description")) {
                    opfModBuffer.append(descTag).append('\n')
                    hasDescription = true
                    modified = true
                } else if (!hasDescription && it.contains("</metadata>")) {
                    opfModBuffer.append(descTag).append("\n</metadata>\n")
                    modified = true
                } else if (it.contains("<item id=\"cover-image\"") && !it.contains("properties=\"cover-image\"")) {
                    opfModBuffer.append(" <item id=\"cover-image\" href=\"images/cover.png\"  media-type=\"image/png\" properties=\"cover-image\"/>\n")
                } else {
                    opfModBuffer.append(it).append('\n')
                }
            }

            opfReader.close()

            if (modified) {
                Files.write(
                        zipFs.getPath("content.opf"), opfModBuffer.toString().toByteArray(charset(UTF_ENCODING)),
                        StandardOpenOption.TRUNCATE_EXISTING)
            }

        } catch (e: Exception) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
        } finally {
            if (opfReader != null) {
                try {
                    opfReader!!.close()
                } catch (e: IOException) {
                    UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                }

            }

            if (zipFs != null) {
                try {
                    zipFs!!.close()
                } catch (e: IOException) {
                    UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                }

            }
        }
    }

    companion object {

        private const val AFRICAN_STORY_BOOKS = "African Story Books"

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 2) {
                System.err.println("Usage: <file destination><file container destination><optional log{trace, debug, info, warn, error, fatal}>")
                System.exit(1)
            }
            UMLogUtil.setLevel(if (args.size == 3) args[2] else "")

            UMLogUtil.logInfo(args[0])
            try {
                AsbScraper().findContent(File(args[0]), File(args[1]))
            } catch (e: IOException) {
                UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logFatal("Exception running findContent AsbScraper")
            }

        }
    }

}