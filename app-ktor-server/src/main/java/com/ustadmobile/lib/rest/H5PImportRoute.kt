package com.ustadmobile.lib.rest

import com.google.common.collect.Lists
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.controller.ContentEntryImportLinkPresenter.Companion.FILE_SIZE
import com.ustadmobile.core.controller.checkIfH5PValidAndReturnItsContent
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.lib.db.entities.H5PImportData
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.client.response.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.request.header
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.util.toMap
import io.ktor.util.url
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.jsoup.Jsoup
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.logging.LogEntry
import org.openqa.selenium.logging.LogType
import org.openqa.selenium.logging.LoggingPreferences
import org.openqa.selenium.remote.DesiredCapabilities
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.logging.Level
import java.util.regex.Pattern
import javax.naming.InitialContext
import kotlin.collections.set

fun Route.H5PImportRoute(db: UmAppDatabase, h5pDownloadFn: (String, Long, String, Long) -> Unit) {

    route("ImportH5P") {
        get("importUrl") {
            val urlString = call.request.queryParameters["hp5Url"] ?: ""
            val parentUid = call.request.queryParameters["parentUid"]?.toLong() ?: 0L
            val content = checkIfH5PValidAndReturnItsContent(urlString)
            val isValid = content?.contains("H5PIntegration")
            when {
                isValid == null -> call.respond(HttpStatusCode.BadRequest, "Invalid URL")
                isValid -> {

                    val entryDao = db.contentEntryDao
                    val parentChildJoinDao = db.contentEntryParentChildJoinDao
                    val containerDao = db.containerDao

                    val contentEntry = ContentEntry()
                    contentEntry.leaf = true
                    contentEntry.title = Jsoup.parse(content).title()
                    contentEntry.contentEntryUid = entryDao.insert(contentEntry)

                    val parentChildJoin = ContentEntryParentChildJoin()
                    parentChildJoin.cepcjParentContentEntryUid = parentUid
                    parentChildJoin.cepcjChildContentEntryUid = contentEntry.contentEntryUid
                    parentChildJoinDao.insert(parentChildJoin)

                    val container = Container(contentEntry)
                    container.containerContentEntryUid = contentEntry.contentEntryUid
                    container.fileSize = 1
                    container.mimeType = "application/webchunk+zip"
                    container.containerUid = containerDao.insert(container)

                    call.respond(H5PImportData(contentEntry, container, parentChildJoin))
                    h5pDownloadFn(urlString, contentEntry.contentEntryUid, content, container.containerUid)

                }
                !isValid -> call.respond(HttpStatusCode.UnsupportedMediaType, "Content not supported")
            }

        }

        get("importVideo") {

            val urlString = call.request.queryParameters["hp5Url"] ?: ""
            val parentUid = call.request.queryParameters["parentUid"]?.toLong() ?: 0L
            val videoTitle = call.request.queryParameters["title"]?: ""

            val response = defaultHttpClient().head<HttpResponse>(urlString)

            val headers = response.headers

            if (headers["Content-Type"]?.startsWith("video/") == true) {

                if (headers["Content-Length"]?.toInt() ?: FILE_SIZE >= FILE_SIZE) {
                    call.respond(HttpStatusCode.BadRequest, "File size too big")
                    return@get
                }

                val entryDao = db.contentEntryDao
                val parentChildJoinDao = db.contentEntryParentChildJoinDao
                val containerDao = db.containerDao

                val contentEntry = ContentEntry()
                contentEntry.leaf = true
                contentEntry.title = videoTitle
                contentEntry.contentEntryUid = entryDao.insert(contentEntry)

                val parentChildJoin = ContentEntryParentChildJoin()
                parentChildJoin.cepcjParentContentEntryUid = parentUid
                parentChildJoin.cepcjChildContentEntryUid = contentEntry.contentEntryUid
                parentChildJoinDao.insert(parentChildJoin)

                val container = Container(contentEntry)
                container.containerContentEntryUid = contentEntry.contentEntryUid

                container.mimeType = headers["Content-Type"]!!


                val http = defaultHttpClient()
                val iContext = InitialContext()
                val containerDirPath = iContext.lookup("java:/comp/env/ustadmobile/app-ktor-server/containerDirPath") as String
                val containerDir = File(containerDirPath)
                containerDir.mkdirs()

                val parentDir = Files.createTempDirectory("video").toFile()
                val videoFile = File(parentDir, FilenameUtils.getName(urlString))
                val input = http.get<InputStream>(urlString)
                FileUtils.copyInputStreamToFile(input, videoFile)

                container.fileSize = videoFile.length()
                container.lastModified = parentDir.lastModified()
                container.mobileOptimized = true
                container.containerUid = containerDao.insert(container)

                val manager = ContainerManager(container, db,
                        db, containerDir.absolutePath)

                manager.addEntries(ContainerManager.FileEntrySource(videoFile, videoFile.name))

                call.respond(H5PImportData(contentEntry, container, parentChildJoin))


            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid URL")
            }
        }


    }
}

fun downloadH5PUrl(db: UmAppDatabase, h5pUrl: String, contentEntryUid: Long, parentDir: File, h5pContentUrl: String?, containerUid: Long) {

    try {
        runBlocking {

            System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver")
            val driver = setupLogIndexChromeDriver()

            val indexList = mutableListOf<LogIndex.IndexEntry>()


            val json = Json(JsonConfiguration.Stable.copy(strictMode = false))
            val http = defaultHttpClient()

            val iContext = InitialContext()
            val containerDirPath = iContext.lookup("java:/comp/env/ustadmobile/app-ktor-server/containerDirPath") as String
            val containerDir = File(containerDirPath)
            containerDir.mkdirs()

            try {

                val htmlContent = Jsoup.parse(h5pContentUrl)

                var h5pcontentIdLocation = htmlContent.selectFirst("div.h5p-content")
                if (h5pcontentIdLocation == null) {
                    h5pcontentIdLocation = htmlContent.selectFirst("iframe.h5p-iframe")
                }

                val contentId = h5pcontentIdLocation.attr("data-content-id")


                val dataList = htmlContent.select("script").filter { it.data().contains("H5PIntegration") }
                dataList.map { it.data() }
                        .forEach {

                            val indexOfH5p = it.indexOf("H5PIntegration = ") + 17
                            val endOfJson = it.lastIndexOf("};") + 1
                            val jsonContent = it.substring(indexOfH5p, endOfJson)
                            val fullJson = json.parseJson(jsonContent)

                            var baseUrl = fullJson.jsonObject["baseUrl"].toString() + fullJson.jsonObject["url"].toString() + "/content/$contentId/"
                            baseUrl = baseUrl.replace("\"", "")


                            val contentBody = fullJson.jsonObject["contents"]!!
                                    .jsonObject["cid-$contentId"]!!
                            val embedHtml = contentBody.jsonObject["embedCode"]!!.content
                            val iframeUrl = Jsoup.parse(embedHtml).selectFirst("iframe").attr("src")

                            driver.get(iframeUrl)
                            val logs = waitForNewFiles(driver)

                            // open browser and download all links
                            logs.map { json.parse(LogResponse.serializer(), it.message) }
                                    .filter { (RESPONSE_RECEIVED == it.message!!.method) }
                                    .forEach { log ->
                                        val mimeType = log.message!!.params!!.response!!.mimeType
                                        val urlString = log.message.params!!.response!!.url!!

                                        try {

                                            val url = URL(urlString)
                                            val urlDirectory = File(parentDir, getNameFromUrl(url))
                                            urlDirectory.mkdirs()

                                            val response = getResponseFromUrl(http, urlString, log.message.params.response?.requestHeaders)
                                            val h5pFile = downloadFileFromLogIndex(response, urlString, urlDirectory)
                                            val logIndex = createIndexFromLog(urlString,
                                                    mimeType
                                                            ?: response.contentType()?.contentType, urlDirectory,
                                                    h5pFile, log.message.params.response?.headers
                                                    ?: response.headers.toMap().entries.associate { it.key to it.value[0] })
                                            indexList.add(logIndex)

                                        } catch (e: Exception) {
                                            print(e.message)
                                        }

                                    }


                            // download h5P integration
                            val contentsJson = contentBody.jsonObject["jsonContent"]!!.content

                            val h5pContent = json.parseJson(contentsJson).jsonObject
                            val links = findLinks(h5pContent)
                            links.forEach { itUrl ->
                                try {
                                    val linkIndex = addToIndex(baseUrl, itUrl, parentDir, http)
                                    indexList.add(linkIndex)
                                } catch (e: Exception) {
                                    println("url invalid$itUrl")
                                }
                            }
                        }


            } catch (e: java.lang.Exception) {
                println(e.stackTrace)
            }


            val index = LogIndex()
            index.title = driver.title
            index.entries = indexList

            val indexJsonFile = File(parentDir, "index.json")
            indexJsonFile.writeText(json.stringify(LogIndex.serializer(), index))

            driver.close()

            val container = Container()
            container.mimeType = "application/webchunk+zip"
            container.lastModified = parentDir.lastModified()
            container.containerContentEntryUid = contentEntryUid
            container.mobileOptimized = true
            container.containerUid = containerUid
            db.containerDao.update(container)

            val fileMap = HashMap<File, String>()
            createContainerFromDirectory(parentDir, fileMap)

            val manager = ContainerManager(container, db,
                    db, containerDir.absolutePath)
            fileMap.forEach {
                manager.addEntries(ContainerManager.FileEntrySource(it.component1(), it.component2()))
            }
        }
    } catch (e: java.lang.Exception) {
        print(e.stackTrace)
        print(e.message)
    }
}

suspend fun addToIndex(baseUrl: String, itUrl: String, parentDir: File, http: HttpClient): LogIndex.IndexEntry {
    var downloadUrl = URL(baseUrl + itUrl.replace("\"", ""))

    val urlDirectory = File(parentDir, getNameFromUrl(downloadUrl))
    urlDirectory.mkdirs()
    val response = getResponseFromUrl(http, downloadUrl.toString(), null)
    val h5pFile = downloadFileFromLogIndex(response, downloadUrl.toString(), urlDirectory)
    return createIndexFromLog(downloadUrl.toString(),
            response.contentType()?.contentType, urlDirectory,
            h5pFile, response.headers.toMap().entries.associate { it.key to it.value[0] })


}

fun findLinks(content: JsonObject): List<String> {

    var linksFound = mutableListOf<String>()
    content.keys.forEach {
        if (it == "path") {
            linksFound.add(content[it].toString())
        } else if (content[it] is JsonObject) {
            linksFound.addAll(findLinks(content[it] as JsonObject))
        } else if (content[it] is JsonArray) {
            (content[it] as JsonArray).forEach {
                if (it is JsonObject) {
                    linksFound.addAll(findLinks(it))
                }
            }
        }
    }
    return linksFound.toList()
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
 * @param urlString    url for the log index
 * @param mimeType     mimeType of file download
 * @param urlDirectory directory of url
 * @param file         file downloaded
 * @param headers          log response of index
 * @return
 */
fun createIndexFromLog(urlString: String, mimeType: String?, urlDirectory: File, file: File, headers: Map<String, String>?): LogIndex.IndexEntry {
    val logIndex = LogIndex.IndexEntry()
    logIndex.url = urlString
    logIndex.mimeType = mimeType
    logIndex.path = urlDirectory.name + "/" + file.name
    logIndex.headers = headers
    return logIndex
}

fun getNameFromUrl(url: URL): String {
    return url.authority.replace("[^a-zA-Z0-9\\.\\-]".toRegex(), "_")
}

/**
 * Create a chrome driver that saves a log of all the files that was downloaded via settings
 *
 * @return Chrome Driver with Log enabled
 */
fun setupLogIndexChromeDriver(): ChromeDriver {
    val desiredCapabilities = DesiredCapabilities.chrome()
    val logPrefs = LoggingPreferences()
    logPrefs.enable(LogType.PERFORMANCE, Level.ALL)
    desiredCapabilities.setCapability("goog:loggingPrefs", logPrefs)

    return ChromeDriver(desiredCapabilities)
}

suspend fun waitForNewFiles(driver: ChromeDriver): List<LogEntry> {
    val logs = Lists.newArrayList(driver.manage().logs().get(LogType.PERFORMANCE).all)
    var hasMore: Boolean
    do {
        delay(5000)
        val newLogs = Lists.newArrayList(driver.manage().logs().get(LogType.PERFORMANCE).all)
        hasMore = newLogs.size > 0
        logs.addAll(newLogs)
    } while (hasMore)
    return logs
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
suspend fun downloadFileFromLogIndex(response: HttpResponse, url: String, destination: File): File {

    var fileName = FilenameUtils.getName(url)
    var index = fileName.indexOf("?")
    if (index != -1) {
        fileName = fileName.substring(0, index)
    }
    val file = File(destination, fileName)
    var inputStream: InputStream? = null
    try {
        file.writeBytes(response.receive<InputStream>().readBytes())
    } catch (e: IOException) {
        println(e.message)
    } finally {
        inputStream?.close()
    }
    return file

}

suspend fun getResponseFromUrl(http: HttpClient, url: String, requestHeaders: Map<String, String>?): HttpResponse {
    return http.get(url) {
        if (requestHeaders != null) {
            for (e in requestHeaders.entries) {
                if (e.key.equals("Accept-Encoding", ignoreCase = true)) {
                    continue
                }
                header(e.key.replace(":", ""), e.value)
            }
        }
    }
}


const val RESPONSE_RECEIVED = "Network.responseReceived"

@Serializable
data class LogResponse(val message: Message? = null) {

    @Serializable
    data class Message(val method: String?, val params: Params? = null) {

        @Serializable
        data class Params(var response: Response? = null) {

            @Serializable
            data class Response(var mimeType: String?, var url: String?, var headers: Map<String, String>?, var requestHeaders: Map<String, String>? = null)

        }

    }

}


@Serializable
data class H5PContent(val contents: String)

@Serializable
class LogIndex {

    var title: String? = null

    var entries: List<IndexEntry>? = null

    var links: Map<String, String>? = null

    @Serializable
    class IndexEntry {

        var url: String? = null

        var mimeType: String? = null

        var path: String? = null

        var headers: Map<String, String>? = null

        var requestHeaders: Map<String, String>? = null

    }

}

