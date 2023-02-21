package com.ustadmobile.lib.contentscrapers

import io.github.aakira.napier.Napier
import com.google.common.collect.Lists
import com.google.gson.GsonBuilder
import com.neovisionaries.i18n.CountryCode
import com.neovisionaries.i18n.LanguageAlpha3Code
import com.neovisionaries.i18n.LanguageCode
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.lib.contentscrapers.khanacademy.ItemData
import com.ustadmobile.lib.contentscrapers.util.SrtFormat
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentCategory
import com.ustadmobile.lib.db.entities.ContentCategorySchema
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryContentCategoryJoin
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.LanguageVariant
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import com.ustadmobile.core.io.ext.addDirToContainer
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZip
import com.ustadmobile.core.io.ext.addFileToContainer
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.contentscrapers.ScraperConstants.CK12_PASS

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.jsoup.Jsoup
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.logging.LogEntry
import org.openqa.selenium.logging.LogType
import org.openqa.selenium.logging.LoggingPreferences
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.time.format.DateTimeFormatter
import java.util.logging.Level
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

import com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_SPACE

import com.ustadmobile.lib.contentscrapers.ScraperConstants.FORWARD_SLASH
import com.ustadmobile.lib.contentscrapers.ScraperConstants.GRAPHIE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.KHAN_GRAPHIE_PREFIX
import com.ustadmobile.lib.contentscrapers.ScraperConstants.KHAN_PASS
import com.ustadmobile.lib.contentscrapers.ScraperConstants.KHAN_USERNAME
import com.ustadmobile.lib.contentscrapers.ScraperConstants.OPUS_EXT
import com.ustadmobile.lib.contentscrapers.ScraperConstants.REQUEST_HEAD
import com.ustadmobile.lib.contentscrapers.ScraperConstants.SCRAPER_TAG
import com.ustadmobile.lib.contentscrapers.ScraperConstants.SVG_EXT
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TIME_OUT_SELENIUM
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TINCAN_FILENAME
import com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING
import com.ustadmobile.lib.contentscrapers.ScraperConstants.WEBM_EXT
import com.ustadmobile.lib.contentscrapers.ScraperConstants.WEBP_EXT
import kotlinx.coroutines.runBlocking
import net.lightbody.bmp.BrowserMobProxyServer
import net.lightbody.bmp.client.ClientUtil
import net.lightbody.bmp.core.har.HarNameValuePair
import org.openqa.selenium.Cookie
import org.openqa.selenium.Proxy
import org.openqa.selenium.remote.CapabilityType
import java.lang.IllegalArgumentException
import java.net.*
import java.time.*
import java.time.temporal.TemporalQuery
import kotlin.system.exitProcess


object ContentScraperUtil {

    private val LOOSE_ISO_DATE_TIME_ZONE_PARSER = DateTimeFormatter.ofPattern("[yyyyMMdd][yyyy-MM-dd][yyyy-DDD]['T'[HHmmss][HHmm][HH:mm:ss][HH:mm][.SSSSSSSSS][.SSSSSS][.SSS][.SS][.S]][OOOO][O][z][XXXXX][XXXX]['['VV']']")


    const val CHROME_PATH_KEY = "chromedriver"

    const val FFMPEG_PATH_KEY = "webm"

    const val CODEC2_PATH_KEY = "codec2"

    const val WEBP_PATH_KEY = "webp"

    const val MOGRIFY_PATH_KEY = "mogrify"

    const val YOUTUBE_DL_PATH_KEY = "youtube-dl"

    val SEARCH_LOCATIONS = mapOf(
            CHROME_PATH_KEY to listOf("/usr/bin/chromedriver"),
            FFMPEG_PATH_KEY to listOf("/usr/bin/ffmpeg"),
            CODEC2_PATH_KEY to listOf("/usr/bin/c2enc", "/usr/local/bin/c2enc"),
            WEBP_PATH_KEY to listOf("/usr/bin/cwebp"),
            MOGRIFY_PATH_KEY to listOf("/usr/bin/mogrify"),
            YOUTUBE_DL_PATH_KEY to listOf("/usr/local/bin/youtube-dl"))

    val driversList = listOf(CHROME_PATH_KEY, FFMPEG_PATH_KEY, CODEC2_PATH_KEY, WEBP_PATH_KEY, YOUTUBE_DL_PATH_KEY)

    fun checkIfPathsToDriversExist() {
        driversList.forEach { driver ->
            if (System.getProperty(driver) == null) {
                val location = SEARCH_LOCATIONS.getValue(driver).firstOrNull { File(it).exists() }
                if (location != null)
                    System.setProperty(driver, location)
                else {
                    println("$driver path is not set")
                    exitProcess(0)
                }

            }
        }


    }

    /**
     * Is the given componentType "Imported Component"
     *
     * @param component_type enum type
     * @return true if type matches ImportedComponent
     */
    fun isImportedComponent(component_type: String): Boolean {
        return ScraperConstants.ComponentType.IMPORTED.type.equals(component_type, ignoreCase = true)
    }


    /**
     * Given an html String, search for all tags that have src attribute to download from
     *
     * @param html           html string that might have src attributes
     * @param destinationDir location the src file will be stored
     * @param baseUrl        is needed for when the src is a path for the url
     * @returns the html with modified src pointing to its new location
     */
    fun downloadAllResources(html: String, destinationDir: File, baseUrl: URL): String {

        if (html.isEmpty()) {
            // no string to parse
            return html
        }

        val doc = Jsoup.parse(html)

        val contentList = doc.select("[src]")
        for (content in contentList) {

            var url = content.attr("src")
            if (url.contains("data:image") && url.contains("base64") || url.contains("file://")) {
                continue
            } else if (url.contains(ScraperConstants.brainGenieLink)) {
                val videoHtml: String
                try {
                    if (url.startsWith("//")) {
                        url = "https:$url"
                    }
                    videoHtml = Jsoup.connect(url).followRedirects(true).get().select("video").outerHtml()
                } catch (e: IOException) {
                    continue
                }

                content.parent()?.html(downloadAllResources(videoHtml, destinationDir, baseUrl))
                continue
            } else if (url.contains("youtube")) {
                // content.parent().html("We cannot download youtube content, please watch using the link below <p></p><a href=" + url + "\"><img src=\"video-thumbnail.jpg\"/></a>");
                content.parent()?.html("")
                continue
            } else if (url.contains(ScraperConstants.slideShareLink)) {
                // content.html("We cannot download slideshare content, please watch using the link below <p></p><img href=" + url + "\" src=\"video-thumbnail.jpg\"/>");
                content.parent()?.html("")
                continue
            }

            var conn: HttpURLConnection? = null
            try {
                val contentUrl = URL(baseUrl, url)

                conn = contentUrl.openConnection() as HttpURLConnection
                conn.requestMethod = REQUEST_HEAD
                val fileName = getFileNameFromUrl(contentUrl)
                val contentFile = File(destinationDir, fileName)

                var destinationFile = contentFile
                val ext = FilenameUtils.getExtension(fileName)
                if (ScraperConstants.IMAGE_EXTENSIONS.contains(ext)) {
                    destinationFile = File(UMFileUtil.stripExtensionIfPresent(contentFile.path) + WEBP_EXT)
                } else if (ScraperConstants.VIDEO_EXTENSIONS.contains(ext)) {
                    destinationFile = File(UMFileUtil.stripExtensionIfPresent(contentFile.path) + WEBM_EXT)
                } else if (ScraperConstants.AUDIO_EXTENSIONS.contains(ext)) {
                    destinationFile = File(UMFileUtil.stripExtensionIfPresent(contentFile.path) + OPUS_EXT)
                }

                content.attr("src", destinationDir.name + "/" + destinationFile.name)

                if (!isFileModified(conn, destinationDir, fileName) && fileHasContent(destinationFile)) {
                    continue
                }
                FileUtils.copyURLToFile(contentUrl, contentFile)
                if (destinationFile.name.endsWith(WEBP_EXT)) {
                    ShrinkerUtil.convertImageToWebp(contentFile, destinationFile)
                    contentFile.delete()
                } else if (destinationFile.name.endsWith(WEBM_EXT)) {
                    ShrinkerUtil.convertVideoToWebM(contentFile, destinationFile)
                    contentFile.delete()
                } else if (destinationFile.name.endsWith(OPUS_EXT)) {
                    ShrinkerUtil.convertAudioToOpos(contentFile, destinationFile)
                    contentFile.delete()
                }

            } catch (e: IOException) {
                println("Url path $url failed to download to file with base url $baseUrl")
                e.printStackTrace()
            } finally {
                conn?.disconnect()
            }

        }

        return doc.body().html()
    }

    /**
     * Given a url link, find the file name, if fileName does not exist in path, use the url to create the filename
     *
     * @param url download link to file
     * @return the extracted file name from url link
     */
    fun getFileNameFromUrl(url: URL): String {
        val decodedPath = URLDecoder.decode(url.path, UTF_ENCODING)
        val fileName = FilenameUtils.getPath(decodedPath).replace("[^a-zA-Z0-9\\.\\-]".toRegex(), "_") + FilenameUtils.getName(decodedPath).replace("[^a-zA-Z0-9\\.\\-]".toRegex(), "_")
        return if (fileName.isEmpty()) {
            url.path.replace("[^a-zA-Z0-9\\.\\-]".toRegex(), "_")
        } else fileName
    }


    /**
     * Given the list, save it as json file
     *
     * @param destinationDir directory it will be saved
     * @param list           ArrayList of Objects to be parsed to a string
     * @throws IOException
     */
    @Throws(IOException::class)
    fun saveListAsJson(destinationDir: File, list: List<*>, fileName: String) {

        val gson = GsonBuilder().disableHtmlEscaping().create()
        val savedQuestionsJson = gson.toJson(list, List::class.java)
        val savedQuestionsFile = File(destinationDir, fileName)

        FileUtils.writeStringToFile(savedQuestionsFile, savedQuestionsJson, UTF_ENCODING)
    }

    /**
     * Given the last modified time from server, check if the file that is saved is up to date with server
     *
     * @param modifiedTime the last time file was modified from server
     * @param file         the current file in our directory
     * @return true if file does not exist or modified time on server is greater than the time in the directory
     */
    fun isContentUpdated(modifiedTime: Long, file: File): Boolean {

        return if (fileHasContent(file)) {
            modifiedTime >= file.lastModified()
        } else true
    }


    /**
     * Given a file, check it exists and has content by checking its size
     *
     * @param file that contains content
     * @return true if the size of the file is greater than 0
     */
    fun fileHasContent(file: File): Boolean {
        return file.exists() && file.length() > 0
    }


    /**
     * Given an Server date, return it as a long
     *
     * @param date Date format from server
     * @return the date given in a long format
     */
    fun parseServerDate(date: String): Long {
        val temporalAccessor = LOOSE_ISO_DATE_TIME_ZONE_PARSER.parseBest(date, TemporalQuery<Any> { ZonedDateTime.from(it) }, TemporalQuery<Any> { LocalDateTime.from(it) }, TemporalQuery<Any> { LocalDate.from(it) })
        return (temporalAccessor as? ZonedDateTime)?.toInstant()?.toEpochMilli()
                ?: ((temporalAccessor as? LocalDateTime)?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
                        ?: (temporalAccessor as LocalDate).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
    }


    /**
     * Given a directory, save it using the filename, download its content and save in the given directory
     *
     * @param directoryToZip location of the folder that will be zipped
     * @param filename       name of the zip
     * @param locationToSave location where the zipped folder will be placed
     * @throws IOException
     */
    @Throws(IOException::class)
    fun zipDirectory(directoryToZip: File, filename: String, locationToSave: File) {

        val zippedFile = File(locationToSave, filename)
        ZipOutputStream(Files.newOutputStream(zippedFile.toPath()), StandardCharsets.UTF_8).use { out ->
            val sourceDirPath = Paths.get(directoryToZip.toURI())
            Files.walk(sourceDirPath).filter { path -> !Files.isDirectory(path) }
                    .forEach { path ->
                        val zipEntry = ZipEntry(sourceDirPath.relativize(path).toString()
                                .replace(Pattern.quote("\\").toRegex(), "/"))
                        try {
                            out.putNextEntry(zipEntry)
                            out.write(Files.readAllBytes(path))
                            out.closeEntry()
                        } catch (e: Exception) {
                            System.err.println(e.cause)
                        }
                    }
        }


    }

    fun createContainerFromDirectory(directory: File, filemap: MutableMap<File, String>): Map<File, String> {
        val sourceDirPath = Paths.get(directory.toURI())
        try {
            Files.walk(sourceDirPath).filter { path -> !Files.isDirectory(path) }
                    .forEach { path ->
                        val relativePath = sourceDirPath.relativize(path).toString()
                                .replace(Pattern.quote("\\").toRegex(), "/")
                        filemap[path.toFile()] = relativePath

                    }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return filemap
    }

    /**
     * Once Selenium is setup and you load a page, use this method to wait for the page to load completely
     *
     * @param waitDriver driver used to wait for conditions on webpage
     * @return true once wait is complete
     */
    fun waitForJSandJQueryToLoad(waitDriver: WebDriverWait): Boolean {

        // wait for jQuery to load
        val jQueryLoad = ExpectedCondition { driver ->
            try {
                (driver as JavascriptExecutor).executeScript("return jQuery.active") as Long == 0L
            } catch (e: Exception) {
                // no jQuery present
                true
            }
        }

        // wait for Javascript to load
        val jsLoad = ExpectedCondition { driver ->
            (driver as JavascriptExecutor).executeScript("return document.readyState")
                    .toString() == "complete"
        }

        return waitDriver.until(jQueryLoad) && waitDriver.until(jsLoad)
    }

    /**
     * Generate tincan xml file
     *
     * @param destinationDirectory directory it will be saved
     * @param activityName         name of course/simulation
     * @param langCode             language of the course/simulation
     * @param fileName             name of file tincan will launch
     * @param typeText             type of tincan file - get from https://registry.tincanapi.com/
     * @param entityId             id of activity should match entry id of opds link
     * @param description          description of course/simulation
     * @param descLang             lang of description
     * @throws ParserConfigurationException fails to create an xml document
     * @throws TransformerException         fails to save the xml document in the directory
     */
    @Throws(TransformerException::class, ParserConfigurationException::class)
    fun generateTinCanXMLFile(destinationDirectory: File, activityName: String, langCode: String, fileName: String, typeText: String, entityId: String, description: String, descLang: String) {

        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.newDocument()

        val rootElement = doc.createElement("tincan")
        val xlms = doc.createAttribute("xmlns")
        xlms.value = "http://projecttincan.com/tincan.xsd"
        rootElement.setAttributeNode(xlms)
        doc.appendChild(rootElement)

        val activities = doc.createElement("activities")
        rootElement.appendChild(activities)

        val activityNode = doc.createElement("activity")
        val id = doc.createAttribute("id")
        val type = doc.createAttribute("type")
        id.value = entityId
        type.value = typeText
        activityNode.setAttributeNode(id)
        activityNode.setAttributeNode(type)
        activities.appendChild(activityNode)

        val nameElement = doc.createElement("name")
        nameElement.appendChild(doc.createTextNode(activityName))
        activityNode.appendChild(nameElement)

        val descElement = doc.createElement("description")
        val lang = doc.createAttribute("lang")
        lang.value = descLang
        descElement.setAttributeNode(lang)
        descElement.appendChild(doc.createTextNode(description))
        activityNode.appendChild(descElement)

        val launchElement = doc.createElement("launch")
        val langLaunch = doc.createAttribute("lang")
        langLaunch.value = langCode
        launchElement.setAttributeNode(langLaunch)
        launchElement.appendChild(doc.createTextNode(fileName))
        activityNode.appendChild(launchElement)

        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        val source = DOMSource(doc)
        val result = StreamResult(File(destinationDirectory, TINCAN_FILENAME))
        transformer.transform(source, result)

    }


    /**
     * Setup Chrome driver for selenium
     *
     * @param headless true if chrome browser is required to open
     * @return
     */
    fun setupChrome(headless: Boolean): ChromeDriver {

        val option = ChromeOptions()
        option.setHeadless(headless)
        return ChromeDriver(option)
    }

    /**
     * Set the system property of the driver in your machine
     */
    fun setChromeDriverLocation() {

    }


    /**
     * check if the file has been modified by comparing eTag or modified Text from server and file in folder.
     *
     * @param conn           url from where the file is being downloaded
     * @param destinationDir location of possible eTag and last modified file
     * @param fileName
     * @return true if file modified
     * @throws IOException
     */
    @Throws(IOException::class)
    fun isFileModified(conn: URLConnection, destinationDir: File, fileName: String): Boolean {

        var eTag: String? = conn.getHeaderField("ETag")
        if (eTag != null) {
            eTag = eTag.replace("\"".toRegex(), "")
            val eTagFile = File(destinationDir, FilenameUtils.getBaseName(fileName) + ScraperConstants.ETAG_TXT)
            return isFileContentsUpdated(eTagFile, eTag)
        }

        val lastModified = conn.getHeaderField("Last-Modified")
        val modifiedFile = File(destinationDir, FilenameUtils.getBaseName(fileName) + ScraperConstants.LAST_MODIFIED_TXT)
        return if (lastModified != null) {
            isFileContentsUpdated(modifiedFile, lastModified)
        } else true

    }


    fun deleteETagOrModified(destination: File, fileName: String) {

        val eTagFile = File(destination, FilenameUtils.getBaseName(fileName) + ScraperConstants.ETAG_TXT)
        if (fileHasContent(eTagFile)) {
            deleteFile(eTagFile)
        }

        val modifiedFile = File(destination, FilenameUtils.getBaseName(fileName) + ScraperConstants.LAST_MODIFIED_TXT)
        if (fileHasContent(modifiedFile)) {
            deleteFile(modifiedFile)
        }

    }


    /**
     * Insert or Update the database for those parentChild Joins where the child have 1 parent
     *
     * @param dao         dao to insert/updateState
     * @param parentEntry
     * @param childEntry
     * @param index
     * @return the updated/created join
     */
    fun insertOrUpdateParentChildJoin(dao: ContentEntryParentChildJoinDao, parentEntry: ContentEntry?, childEntry: ContentEntry, index: Int): ContentEntryParentChildJoin {

        val existingParentChildJoin = dao.findParentByChildUuids(childEntry.contentEntryUid)

        val newJoin = ContentEntryParentChildJoin()
        newJoin.cepcjParentContentEntryUid = parentEntry?.contentEntryUid ?: -4103245208651563007L
        newJoin.cepcjChildContentEntryUid = childEntry.contentEntryUid
        newJoin.childIndex = if(index == 0) existingParentChildJoin?.childIndex ?: index else index
        return if (existingParentChildJoin == null) {
            newJoin.cepcjUid = dao.insert(newJoin)
            newJoin
        } else {
            newJoin.cepcjUid = existingParentChildJoin.cepcjUid
            if (newJoin != existingParentChildJoin) {
                dao.update(newJoin)
            }
            newJoin
        }
    }


    /**
     * Insert or Update the database for those parentChildJoin where the child might have multiple parents (search by uuids of parent and child)
     *
     * @param dao         database to search
     * @param parentEntry parent entry
     * @param childEntry  child entry
     * @param index       count
     * @return the updated/created join
     */
    fun insertOrUpdateChildWithMultipleParentsJoin(dao: ContentEntryParentChildJoinDao, parentEntry: ContentEntry?, childEntry: ContentEntry, index: Int): ContentEntryParentChildJoin {

        val parentUid = parentEntry?.contentEntryUid ?: UstadView.MASTER_SERVER_ROOT_ENTRY_UID
        val existingParentChildJoin = dao.findJoinByParentChildUuids(parentUid, childEntry.contentEntryUid)
        Napier.d("Joining 2 entries, parent: ${parentEntry?.title ?: "root"} and child ${childEntry.title}", tag = SCRAPER_TAG)

        val newJoin = ContentEntryParentChildJoin()
        newJoin.cepcjParentContentEntryUid = parentUid
        newJoin.cepcjChildContentEntryUid = childEntry.contentEntryUid
        newJoin.childIndex = if(index == 0) existingParentChildJoin?.childIndex ?: index else index
        return if (existingParentChildJoin == null) {
            newJoin.cepcjUid = dao.insert(newJoin)
            Napier.d("didnt exist in db, make new one ${newJoin.cepcjUid} for parent ${parentEntry?.title ?: "root"}", tag = SCRAPER_TAG)
            newJoin
        } else {
            newJoin.cepcjUid = existingParentChildJoin.cepcjUid
            if (newJoin != existingParentChildJoin) {
                dao.update(newJoin)
            }

            Napier.d("updated db with uid ${newJoin.cepcjUid} for ${parentEntry?.title ?: "root"}", tag = SCRAPER_TAG)
            newJoin
        }
    }


    /**
     * Insert or Update the database for those parentChildJoin where the child might have multiple categories (search by uuids of category and child)
     *
     * @param contentEntryCategoryJoinDao database to search
     * @param category                    parent entry
     * @param childEntry                  child entry
     * @return the updated/created join
     */
    fun insertOrUpdateChildWithMultipleCategoriesJoin(contentEntryCategoryJoinDao: ContentEntryContentCategoryJoinDao,
                                                      category: ContentCategory, childEntry: ContentEntry): ContentEntryContentCategoryJoin {
        var categoryToSimulationJoin = contentEntryCategoryJoinDao.findJoinByParentChildUuids(category.contentCategoryUid, childEntry.contentEntryUid)
        if (categoryToSimulationJoin == null) {
            categoryToSimulationJoin = ContentEntryContentCategoryJoin()
            categoryToSimulationJoin.ceccjContentCategoryUid = category.contentCategoryUid
            categoryToSimulationJoin.ceccjContentEntryUid = childEntry.contentEntryUid
            categoryToSimulationJoin.ceccjUid = contentEntryCategoryJoinDao.insert(categoryToSimulationJoin)

        } else {
            val changedCategoryEntryJoin = ContentEntryContentCategoryJoin()
            changedCategoryEntryJoin.ceccjUid = categoryToSimulationJoin.ceccjUid
            changedCategoryEntryJoin.ceccjContentCategoryUid = category.contentCategoryUid
            changedCategoryEntryJoin.ceccjContentEntryUid = childEntry.contentEntryUid
            if (changedCategoryEntryJoin != categoryToSimulationJoin) {
                contentEntryCategoryJoinDao.update(changedCategoryEntryJoin)
            }
            categoryToSimulationJoin = changedCategoryEntryJoin
        }
        return categoryToSimulationJoin
    }

    /**
     * Insert or updateState the database with a new/updated Schema
     *
     * @param categorySchemeDao dao to insert/updateState
     * @param schemaName        schema Name
     * @param schemaUrl         schema Url
     * @return the entry that was created/updated
     */
    fun insertOrUpdateSchema(categorySchemeDao: ContentCategorySchemaDao, schemaName: String, schemaUrl: String): ContentCategorySchema {
        var schema = categorySchemeDao.findBySchemaUrl(schemaUrl)
        if (schema == null) {
            schema = ContentCategorySchema()
            schema.schemaName = schemaName
            schema.schemaUrl = schemaUrl
            schema.contentCategorySchemaUid = categorySchemeDao.insert(schema)
        } else {
            val changedSchema = ContentCategorySchema()
            changedSchema.contentCategorySchemaUid = schema.contentCategorySchemaUid
            changedSchema.schemaName = schemaName
            changedSchema.schemaUrl = schemaUrl
            if (changedSchema != schema) {
                categorySchemeDao.update(changedSchema)
            }
            schema = changedSchema
        }
        return schema
    }

    /**
     * Insert or updateState the category that belongs in a schema
     *
     * @param categoryDao  dao to insert/updateState
     * @param schema       schema the category belongs in
     * @param categoryName name of category
     * @return the new/updated category entry
     */
    fun insertOrUpdateCategoryContent(categoryDao: ContentCategoryDao, schema: ContentCategorySchema, categoryName: String): ContentCategory {
        var category = categoryDao.findCategoryBySchemaIdAndName(schema.contentCategorySchemaUid, categoryName)
        if (category == null) {
            category = ContentCategory()
            category.ctnCatContentCategorySchemaUid = schema.contentCategorySchemaUid
            category.name = categoryName
            category.contentCategoryUid = categoryDao.insert(category)
        } else {
            val changedCategory = ContentCategory()
            changedCategory.contentCategoryUid = category.contentCategoryUid
            changedCategory.ctnCatContentCategorySchemaUid = schema.contentCategorySchemaUid
            changedCategory.name = categoryName
            if (changedCategory != category) {
                categoryDao.update(changedCategory)
            }
            category = changedCategory
        }
        return category
    }

    /**
     * Insert or updateState the relation between 2 content entry
     *
     * @param contentEntryRelatedJoinDao dao to insert/updateState
     * @param relatedEntry               related entry of parent contententry
     * @param parentEntry                parent content entry
     * @param relatedType                type of relation (Translation, related content)
     * @return
     */
    fun insertOrUpdateRelatedContentJoin(contentEntryRelatedJoinDao: ContentEntryRelatedEntryJoinDao, relatedEntry: ContentEntry, parentEntry: ContentEntry, relatedType: Int): ContentEntryRelatedEntryJoin {
        var relatedTranslationJoin = contentEntryRelatedJoinDao.findPrimaryByTranslation(relatedEntry.contentEntryUid)
        if (relatedTranslationJoin == null) {
            relatedTranslationJoin = ContentEntryRelatedEntryJoin()
            relatedTranslationJoin.cerejRelLanguageUid = relatedEntry.primaryLanguageUid
            relatedTranslationJoin.cerejContentEntryUid = parentEntry.contentEntryUid
            relatedTranslationJoin.cerejRelatedEntryUid = relatedEntry.contentEntryUid
            relatedTranslationJoin.relType = relatedType
            relatedTranslationJoin.cerejUid = contentEntryRelatedJoinDao.insert(relatedTranslationJoin)
        } else {
            val changedRelatedJoin = ContentEntryRelatedEntryJoin()
            changedRelatedJoin.cerejUid = relatedTranslationJoin.cerejUid
            changedRelatedJoin.cerejRelLanguageUid = relatedEntry.primaryLanguageUid
            changedRelatedJoin.cerejContentEntryUid = parentEntry.contentEntryUid
            changedRelatedJoin.cerejRelatedEntryUid = relatedEntry.contentEntryUid
            changedRelatedJoin.relType = relatedType
            if (changedRelatedJoin != relatedTranslationJoin) {
                contentEntryRelatedJoinDao.update(changedRelatedJoin)
            }
            relatedTranslationJoin = changedRelatedJoin
        }
        return relatedTranslationJoin
    }

    /**
     * Given a language name, check if this language exists in db before adding it
     *
     * @param languageDao dao to query and insert
     * @param langName    name of the language
     * @return the entity language
     */
    fun insertOrUpdateLanguageByName(languageDao: LanguageDao, langName: String): Language {
        var threeLetterCode = ""
        var twoLetterCode = ""

        val langAlpha3List = LanguageAlpha3Code.findByName(langName)
        if (!langAlpha3List.isEmpty()) {
            threeLetterCode = langAlpha3List[0].name
            val code = LanguageCode.getByCode(threeLetterCode)
            twoLetterCode = if (code != null) LanguageCode.getByCode(threeLetterCode).name else ""
        }
        var langObj = getLanguageFromDao(langName, twoLetterCode, languageDao)
        if (langObj == null) {
            langObj = Language()
            langObj.name = langName
            if (!threeLetterCode.isEmpty()) {
                langObj.iso_639_1_standard = twoLetterCode
                langObj.iso_639_2_standard = threeLetterCode
            }
            langObj.langUid = languageDao.insert(langObj)
        } else {
            val changedLang = Language()
            changedLang.langUid = langObj.langUid
            changedLang.name = langName
            var isChanged = false

            if (changedLang.name != langObj.name) {
                isChanged = true
            }

            if (!threeLetterCode.isEmpty()) {
                changedLang.iso_639_1_standard = twoLetterCode
                changedLang.iso_639_2_standard = threeLetterCode

                if (changedLang.iso_639_1_standard != langObj.iso_639_1_standard) {
                    isChanged = true
                }

                if (changedLang.iso_639_2_standard != langObj.iso_639_2_standard) {
                    isChanged = true
                }

            }

            if (isChanged) {
                languageDao.update(changedLang)
            }
            langObj = changedLang

        }
        return langObj
    }

    private fun getLanguageFromDao(langName: String, twoLetterCode: String, dao: LanguageDao): Language? {
        var lang: Language? = null
        if (!langName.isEmpty()) {
            lang = dao.findByName(langName)
        }
        return if (!twoLetterCode.isEmpty() && lang == null) {
            dao.findByTwoCode(twoLetterCode)
        } else lang
    }

    /**
     * Given a language with 2 digit code, check if this language exists in db before adding it
     *
     * @param languageDao dao to query and insert
     * @param langTwoCode two digit code of language
     * @return the entity language
     */
    fun insertOrUpdateLanguageByTwoCode(languageDao: LanguageDao, langTwoCode: String): Language {

        var language = languageDao.findByTwoCode(langTwoCode)
        if (language == null) {
            language = Language()
            language.iso_639_1_standard = langTwoCode
            val nameOfLang = LanguageCode.getByCode(langTwoCode)
            if (nameOfLang != null) {
                language.name = nameOfLang.getName()
            }
            language.langUid = languageDao.insert(language)
        } else {
            val changedLang = Language()
            changedLang.langUid = language.langUid
            changedLang.iso_639_1_standard = langTwoCode
            val nameOfLang = LanguageCode.getByCode(langTwoCode)
            if (nameOfLang != null) {
                changedLang.name = nameOfLang.getName()
            }
            var isChanged = false
            if (language.iso_639_1_standard == null || language.iso_639_1_standard != changedLang.iso_639_1_standard) {
                isChanged = true
            }
            if (language.name == null || language.name == changedLang.name) {
                isChanged = true
            }
            if (isChanged) {
                languageDao.update(changedLang)
            }
            language = changedLang
        }
        return language
    }

    fun insertOrUpdateLanguageByThreeCode(langDao: LanguageDao, langThreeCode: String): Language {
        var language = langDao.findByThreeCode(langThreeCode)
        if (language == null) {
            language = Language()
            language.iso_639_3_standard = langThreeCode
            val nameOfLang = LanguageCode.getByCode(langThreeCode)
            if (nameOfLang != null) {
                language.name = nameOfLang.getName()
            } else {
                language.name = LanguageAlpha3Code.getByCode(langThreeCode).getName()
            }
            language.langUid = langDao.insert(language)
        } else {
            val changedLang = Language()
            changedLang.langUid = language.langUid
            changedLang.iso_639_3_standard = langThreeCode
            val nameOfLang = LanguageCode.getByCode(langThreeCode)
            if (nameOfLang != null) {
                changedLang.name = nameOfLang.getName()
            } else {
                var code = LanguageAlpha3Code.getByCode(langThreeCode)
                if (code != null) {
                    language.name = LanguageAlpha3Code.getByCode(langThreeCode).getName()
                }
            }
            var isChanged = false
            if (language.iso_639_3_standard == null || language.iso_639_3_standard != changedLang.iso_639_3_standard) {
                isChanged = true
            }
            if (language.name == null || language.name == changedLang.name) {
                isChanged = true
            }
            if (isChanged) {
                langDao.update(changedLang)
            }
            language = changedLang
        }
        return language
    }


    @Throws(IOException::class)
    fun isFileContentsUpdated(modifiedFile: File, data: String): Boolean {
        if (fileHasContent(modifiedFile)) {
            val text = FileUtils.readFileToString(modifiedFile, UTF_ENCODING)
            return !data.equals(text, ignoreCase = true)
        } else {
            FileUtils.writeStringToFile(modifiedFile, data,
                    UTF_ENCODING)
        }
        return true
    }

    fun getDefaultSeleniumProxy(proxy: BrowserMobProxyServer): Proxy {
        return ClientUtil.createSeleniumProxy(proxy)
    }



    /**
     * @param contentEntry    entry that is joined to file
     * @param mobileOptimized isMobileOptimized
     * @param fileType        filetype of file
     * @param tmpDir
     * @param db
     * @param repository
     * @param containerDir
     * @returns the entry file
     */
    @Throws(IOException::class)
    // TODO scrapers need di to support
    @Deprecated("not updated since scrapers")
    fun insertContainer(containerDao: ContainerDao, contentEntry: ContentEntry,
                        mobileOptimized: Boolean, fileType: String,
                        lastModified: Long, tmpDir: File, db: UmAppDatabase,
                        repository: UmAppDatabase, containerDir: File): Container {

        val container = Container()
        container.mimeType = fileType
        container.cntLastModified = lastModified
        container.containerContentEntryUid = contentEntry.contentEntryUid
        container.mobileOptimized = mobileOptimized
        container.containerUid = containerDao.insert(container)

        val containerAddOptions = ContainerAddOptions(storageDirUri = containerDir.toDoorUri())
        runBlocking {
            if (tmpDir.isDirectory) {
               /* repository.addDirToContainer(container.containerUid, tmpDir.toDoorUri(),
                        true, containerAddOptions, di)*/
            } else if(fileType == ScraperConstants.MIMETYPE_ZIP ||
                    fileType == ScraperConstants.MIMETYPE_EPUB ||
                    fileType == ScraperConstants.MIMETYPE_TINCAN){
                repository.addEntriesToContainerFromZip(
                        container.containerUid,
                        tmpDir.toDoorUri(), containerAddOptions, Any()
                )
            }else{
             /*   repository.addFileToContainer(container.containerUid, tmpDir.toDoorUri(),
                        tmpDir.name, containerAddOptions, di)*/
            }
        }

        return container
    }

    @Throws(IOException::class)
    fun getMd5(ePubFile: File): String {
        val fis = FileInputStream(ePubFile)
        val md5EpubFile = DigestUtils.md5Hex(fis)
        fis.close()

        return md5EpubFile
    }


    /**
     * Insert or updateState language variant
     *
     * @param variantDao variant dao to insert/updateState
     * @param variant    variant of the language
     * @param language   the language the variant belongs to
     * @return the language variant entry that was created/updated
     */
    fun insertOrUpdateLanguageVariant(variantDao: LanguageVariantDao, variant: String?, language: Language): LanguageVariant? {
        var languageVariant: LanguageVariant? = null
        if (variant != null && variant.isNotEmpty()) {
            var countryCode: CountryCode? = CountryCode.getByCode(variant.toUpperCase())
            if (countryCode == null) {
                val countryList = CountryCode.findByName(variant)
                if (countryList.isNotEmpty()) {
                    countryCode = countryList[0]
                }
            }
            if (countryCode != null) {
                val alpha2 = countryCode.alpha2
                val name = countryCode.getName()
                languageVariant = variantDao.findByCode(alpha2)
                if (languageVariant == null) {
                    languageVariant = LanguageVariant()
                    languageVariant.countryCode = alpha2
                    languageVariant.name = name
                    languageVariant.langUid = language.langUid
                    languageVariant.langVariantUid = variantDao.insert(languageVariant)
                } else {
                    val changedVariant = LanguageVariant()
                    changedVariant.langVariantUid = languageVariant.langVariantUid
                    changedVariant.countryCode = alpha2
                    changedVariant.name = name
                    changedVariant.langUid = language.langUid
                    if (changedVariant != languageVariant) {
                        variantDao.update(languageVariant)
                    }
                    languageVariant = changedVariant
                }
            }
        }
        return languageVariant
    }

    private fun checkContentEntryChanges(changedEntry: ContentEntry, oldEntry: ContentEntry, contentEntryDao: ContentEntryDao): ContentEntry {
        changedEntry.contentEntryUid = oldEntry.contentEntryUid
        if (changedEntry != oldEntry) {
            changedEntry.lastModified = System.currentTimeMillis()
            contentEntryDao.update(changedEntry)
        }
        return changedEntry
    }

    /**
     * @param id              entry id
     * @param title           title of entry
     * @param sourceUrl       source url of entry
     * @param publisher       publisher of entry
     * @param licenseType     license Type of entry(predefined)
     * @param primaryLanguage primary language uid of entry
     * @param languageVariant language variant uid of entry
     * @param description     description of entry
     * @param isLeaf          is the entry a leaf (last child)
     * @param author          author of entry
     * @param thumbnailUrl    thumbnail Url of entry if exists
     * @param licenseName     license name of entry
     * @param licenseUrl      license Url of entry
     * @return the contententry
     */
    private fun createContentEntryObject(id: String?, title: String?, sourceUrl: String, publisher: String, licenseType: Int,
                                         primaryLanguage: Long, languageVariant: Long?, description: String?, isLeaf: Boolean,
                                         author: String?, thumbnailUrl: String?, licenseName: String, licenseUrl: String, contentTypeFlag: Int): ContentEntry {
        val contentEntry = ContentEntry()
        contentEntry.entryId = id
        contentEntry.title = title
        contentEntry.sourceUrl = sourceUrl
        contentEntry.publisher = publisher
        contentEntry.licenseType = licenseType
        contentEntry.primaryLanguageUid = primaryLanguage
        if (languageVariant != null) {
            contentEntry.languageVariantUid = languageVariant
        }
        contentEntry.description = description
        contentEntry.leaf = isLeaf
        contentEntry.author = author
        contentEntry.thumbnailUrl = thumbnailUrl
        contentEntry.licenseName = licenseName
        contentEntry.licenseUrl = licenseUrl
        contentEntry.publik = true
        contentEntry.contentTypeFlag = contentTypeFlag
        contentEntry.contentFlags = ContentEntry.FLAG_SCRAPPED
        return contentEntry
    }

    /**
     * @param id              entry id
     * @param title           title of entry
     * @param sourceUrl       source url of entry
     * @param publisher       publisher of entry
     * @param licenseType     license Type of entry(predefined)
     * @param primaryLanguage primary language uid of entry
     * @param languageVariant language variant uid of entry
     * @param description     description of entry
     * @param isLeaf          is the entry a leaf (last child)
     * @param author          author of entry
     * @param thumbnailUrl    thumbnail Url of entry if exists
     * @param licenseName     license name of entry
     * @param licenseUrl      license Url of entry
     * @param contentEntryDao dao to insert or updateState
     * @return the updated content entry
     */
    fun createOrUpdateContentEntry(id: String?, title: String?, sourceUrl: String, publisher: String, licenseType: Int,
                                   primaryLanguage: Long, languageVariant: Long?, description: String?, isLeaf: Boolean,
                                   author: String?, thumbnailUrl: String?, licenseName: String, licenseUrl: String,
                                   contentTypeFlag: Int,
                                   contentEntryDao: ContentEntryDao): ContentEntry {

        var contentEntry = contentEntryDao.findBySourceUrl(sourceUrl)
        if (contentEntry == null) {
            contentEntry = createContentEntryObject(id, title, sourceUrl, publisher, licenseType, primaryLanguage,
                    languageVariant, description, isLeaf, author, thumbnailUrl, licenseName, licenseUrl, contentTypeFlag)
            contentEntry.lastModified = System.currentTimeMillis()
            contentEntry.contentEntryUid = contentEntryDao.insert(contentEntry)
        } else {
            val changedEntry = createContentEntryObject(id, title, sourceUrl, publisher, licenseType, primaryLanguage,
                    languageVariant, description, isLeaf, author, thumbnailUrl, licenseName, licenseUrl, contentTypeFlag)
            contentEntry = checkContentEntryChanges(changedEntry, contentEntry, contentEntryDao)
        }
        return contentEntry
    }


    fun createQueueItem(queueDao: ScrapeQueueItemDao, subjectUrl: URL,
                        subjectEntry: ContentEntry, destination: File,
                        type: String, runId: Int, itemType: Int): ScrapeQueueItem? {

        var item = queueDao.findExistingQueueItem(runId, subjectEntry.contentEntryUid)
        if (item == null) {
            item = ScrapeQueueItem()
            item.destDir = destination.path
            item.scrapeUrl = subjectUrl.toString()
            item.sqiContentEntryParentUid = subjectEntry.contentEntryUid
            item.status = ScrapeQueueItemDaoCommon.STATUS_PENDING
            item.contentType = type
            item.runId = runId
            item.itemType = itemType
            item.timeAdded = System.currentTimeMillis()
            queueDao.insert(item)
        }
        return item
    }

    /**
     * Save files that are in android directory into the log index folder
     *
     * @param url       url of the resource
     * @param directory directory it will be saved
     * @param mimeType  mimeType of resource
     * @param filePath  filePath of resource
     * @param fileName
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun createIndexWithResourceFiles(url: String, directory: File, mimeType: String, filePath: InputStream, fileName: String): LogIndex.IndexEntry {

        val imageUrl = URL(url)
        val imageFolder = createDirectoryFromUrl(directory, imageUrl)

        val imageFile = File(imageFolder, fileName)
        FileUtils.copyToFile(filePath, imageFile)

        return createIndexFromLog(imageUrl.toString(), mimeType,
                imageFolder, imageFile, null)
    }


    /**
     * Download a file from the log entry, check if it has headers, add them to url if available
     *
     * @param url         url file to download
     * @param destination destination of file
     * @param log         log details (has request headers info)
     * @return the file that was download
     * @throws IOException
     */
    @Throws(IOException::class)
    fun downloadFileFromLogIndex(url: URL, destination: File, log: LogResponse?, cookies: String?): File {

        if (url.host.contains("youtube")) {
            throw IllegalArgumentException("cannot download youtube")
        }

        val fileName = getFileNameFromUrl(url)
        val file = File(destination, fileName)
        if (log != null && log.message!!.params!!.response!!.requestHeaders != null || cookies != null) {
            var conn: HttpURLConnection? = null
            try {
                conn = url.openConnection() as HttpURLConnection
                if (log!!.message!!.params!!.response!!.requestHeaders != null) {
                    for ((key, value) in log.message!!.params!!.response!!.requestHeaders!!) {
                        if (key.equals("Accept-Encoding", ignoreCase = true)) {
                            continue
                        }
                        conn.addRequestProperty(key.replace(":".toRegex(), ""), value)
                    }
                }
                if (cookies != null) {
                    conn.addRequestProperty("cookie", cookies)
                }
                FileUtils.copyInputStreamToFile(conn.inputStream, file)
            } catch (e: IOException) {
                UMLogUtil.logError("Error downloading file from log index with url $url")
            } finally {
                conn?.disconnect()
            }
        } else {
            FileUtils.copyURLToFile(url, file)
        }

        return file

    }

    /**
     * Create a folder based on the url name eg. www.khanacademy.com/video/10 = folder name khanacademy
     *
     * @param destination destination of folder
     * @param url         url
     * @return
     */
    fun createDirectoryFromUrl(destination: File, url: URL): File {
        val urlFolder = File(destination, url.authority.replace("[^a-zA-Z0-9\\.\\-]".toRegex(), "_"))
        urlFolder.mkdirs()
        return urlFolder
    }

    /**
     * @param urlString    url for the log index
     * @param mimeType     mimeType of file download
     * @param urlDirectory directory of url
     * @param file         file downloaded
     * @param log          log response of index
     * @return
     */
    fun createIndexFromLog(urlString: String, mimeType: String?, urlDirectory: File?, file: File?, log: LogResponse?): LogIndex.IndexEntry {
        val logIndex = LogIndex.IndexEntry()
        logIndex.url = urlString
        logIndex.mimeType = mimeType
        logIndex.path = if (file != null) (urlDirectory!!.name + FORWARD_SLASH + file.name) else ""
        if (log != null) {
            logIndex.headers = log.message!!.params?.response?.headers
                    ?: log.message!!.params?.redirectResponse?.headers
        }
        return logIndex
    }

    fun createIndexFromHar(urlString: String, mimeType: String?, urlDirectory: File?, file: File?, headers: MutableList<HarNameValuePair>?): LogIndex.IndexEntry {
        val logIndex = LogIndex.IndexEntry()
        logIndex.url = urlString
        logIndex.mimeType = mimeType
        logIndex.path = if (file != null) (urlDirectory!!.name + FORWARD_SLASH + file.name) else ""
        logIndex.headers = headers?.map { it.name to it.value }?.toMap()
        return logIndex

    }

    /**
     * Create a chrome driver that saves a log of all the files that was downloaded via settings
     *
     * @return Chrome Driver with Log enabled
     */
    fun setupLogIndexChromeDriver(): ChromeDriver {
        val d = ChromeOptions()
        d.setCapability("opera.arguments", "-screenwidth 411 -screenheight 731")

        val logPrefs = LoggingPreferences()
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL)
        d.setCapability("goog:loggingPrefs", logPrefs)

        return ChromeDriver(d)
    }

    fun setupChromeDriverWithSeleniumProxy(seleniumProxy: Proxy): ChromeDriver {
        val d = ChromeOptions()
        d.setCapability("opera.arguments", "-screenwidth 411 -screenheight 731")
        d.setCapability(CapabilityType.PROXY, seleniumProxy)

        return ChromeDriver(d)

    }

    /**
     * Given a map of params, convert into a stringbuffer for post requests
     *
     * @param params params to include in post request
     * @return map converted to string
     * @throws IOException
     */
    @Throws(IOException::class)
    fun convertMapToStringBuffer(params: Map<String, String>): StringBuffer {
        val requestParams = StringBuffer()
        for (key in params.keys) {
            val value = params[key]
            requestParams.append(URLEncoder.encode(key, UTF_ENCODING))
            requestParams.append("=").append(
                    URLEncoder.encode(value, UTF_ENCODING))
            requestParams.append("&")
        }
        return requestParams
    }


    /**
     * Clear the console log in chrome, wait for it to finish clearing
     *
     * @param driver
     */
    fun clearChromeConsoleLog(driver: ChromeDriver) {
        val js = driver as JavascriptExecutor
        js.executeScript("console.clear()")
    }

    fun loginKhanAcademy(): ChromeDriver {

        val driver = setupLogIndexChromeDriver()

        val waitDriver = WebDriverWait(driver, TIME_OUT_SELENIUM)
        waitForJSandJQueryToLoad(waitDriver)
        waitDriver.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#login-signup-root")))

        driver.findElement(By.cssSelector("div#login-signup-root input[id*=email-or-username]")).sendKeys(KHAN_USERNAME)
        driver.findElement(By.cssSelector("div#login-signup-root input[id*=text-field-1-password]")).sendKeys(KHAN_PASS)

        val elements = driver.findElements(By.cssSelector("div#login-signup-root button div"))
        for (element in elements) {
            if (element.text.contains("Log in")) {
                element.click()
                break
            }
        }

        waitDriver.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.user-info-container")))

        clearChromeConsoleLog(driver)

        return driver
    }

    fun loginCK12(driver: ChromeDriver): ChromeDriver {

        driver.findElement(By.cssSelector("div input#username")).sendKeys(KHAN_USERNAME)
        driver.findElement(By.cssSelector("div input#password")).sendKeys(CK12_PASS)

        driver.findElement(By.cssSelector("div.loginsubmitwrap input.btn")).click()
        val waitDriver = WebDriverWait(driver, TIME_OUT_SELENIUM)
        waitForJSandJQueryToLoad(waitDriver)
        clearChromeConsoleLog(driver)

        return driver
    }


    fun downloadImagesFromJsonContent(images: MutableMap<String, ItemData.Content.Image?>, destDir: File, scrapeUrl: String, indexList: MutableList<LogIndex.IndexEntry>) {
        for (imageValue in images.keys) {
            var conn: HttpURLConnection? = null
            try {
                var image = imageValue.replace(EMPTY_SPACE.toRegex(), "")
                var imageUrlString = image
                if (image.contains(GRAPHIE)) {
                    imageUrlString = KHAN_GRAPHIE_PREFIX + image.substring(image.lastIndexOf("/") + 1) + SVG_EXT
                }

                val imageUrl = URL(imageUrlString)
                conn = imageUrl.openConnection() as HttpURLConnection
                conn.requestMethod = REQUEST_HEAD
                val mimeType = conn.contentType
                val imageFile = createDirectoryFromUrl(destDir, imageUrl)

                val imageContent = File(imageFile, FilenameUtils.getName(imageUrl.path))
                FileUtils.copyURLToFile(imageUrl, imageContent)

                val logIndex = createIndexFromLog(imageUrlString, mimeType,
                        imageFile, imageContent, null)
                indexList.add(logIndex)
            } catch (e: MalformedURLException) {
                UMLogUtil.logDebug(ExceptionUtils.getStackTrace(e))
            } catch (e: Exception) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("Error downloading an image for index log$imageValue with url $scrapeUrl")
            } finally {
                conn?.disconnect()
            }

        }

    }

    fun waitForNewFiles(driver: ChromeDriver): List<LogEntry> {
        val logs = Lists.newArrayList(driver.manage().logs().get(LogType.PERFORMANCE).all)
        var hasMore: Boolean
        do {
            try {
                Thread.sleep(2000)
            } catch (ignored: InterruptedException) {
            }

            val newLogs = Lists.newArrayList(driver.manage().logs().get(LogType.PERFORMANCE).all)
            hasMore = newLogs.size > 0
            UMLogUtil.logTrace("size of new logs from driver is" + newLogs.size)
            logs.addAll(newLogs)
        } while (hasMore)
        return logs
    }

    @Throws(IOException::class)
    fun createSrtFile(srtFormatList: List<SrtFormat>?, srtFile: File): String {

        if (srtFormatList == null || srtFormatList.isEmpty()) {
            return ""
        }

        val buffer = StringBuilder()
        var count = 1
        for (format in srtFormatList) {

            buffer.append(count++)
            buffer.append(System.lineSeparator())
            buffer.append(formatTimeInMs(format.startTime))
            buffer.append(" --> ")
            buffer.append(formatTimeInMs(format.endTime))
            buffer.append(System.lineSeparator())
            buffer.append(format.text)
            buffer.append(System.lineSeparator())
            buffer.append(System.lineSeparator())

        }

        FileUtils.writeStringToFile(srtFile, buffer.toString(), UTF_ENCODING)
        return buffer.toString()
    }

    fun formatTimeInMs(timeMs: Long): String {

        val millis = timeMs % 1000
        val second = timeMs / 1000 % 60
        val minute = timeMs / (1000 * 60) % 60
        val hour = timeMs / (1000 * 60 * 60) % 24

        return String.format("%02d:%02d:%02d,%03d", hour, minute, second, millis)
    }


    fun deleteFile(content: File?) {
        if (content != null) {
            if (!content.delete()) {
                UMLogUtil.logTrace("Could not delete: " + content.path)
            }
        }
    }

    fun insertOrUpdateLanguageManual(langDao: LanguageDao, langName: String, langCode: String) {
        val lang = langDao.findByName(langName)

        val newLang = Language()
        newLang.name = langName
        newLang.iso_639_3_standard = langCode
        if (lang == null) {
            newLang.langUid = langDao.insert(newLang)
        }
    }

    fun returnListOfCookies(url: String, cookieList: Set<Cookie>): String {
        return cookieList.filter { it.domain in url }.joinToString(separator = "; ") { cookie -> "${cookie.name}=${cookie.value}" }
    }

    fun insertTempContentEntry(contentEntryDao: ContentEntryDao, url: String, primaryLanguageUid: Long, title: String): ContentEntry {
        return contentEntryDao.findBySourceUrl(url) ?: ContentEntry().apply {
            this.sourceUrl = url
            this.primaryLanguageUid = primaryLanguageUid
            this.title = title
            leaf = true
            this.contentEntryUid = contentEntryDao.insert(this)
        }
    }


    fun insertTempYoutubeContentEntry(contentEntryDao: ContentEntryDao, url: String, primaryLanguageUid: Long, title: String, publisherText: String, license: Int, langVariant: Long): ContentEntry {
        return contentEntryDao.findBySourceUrl(url) ?: ContentEntry().apply {
            this.sourceUrl = url
            this.primaryLanguageUid = primaryLanguageUid
            this.languageVariantUid = langVariant
            this.title = title
            this.publisher = publisherText
            this.licenseType = license
            leaf = true
            this.contentEntryUid = contentEntryDao.insert(this)
        }
    }


}
