package com.ustadmobile.port.android.impl

import android.os.Handler
import android.os.Looper
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.Gson
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.controller.WebChunkPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerEntryFile.Companion.COMPRESSION_GZIP
import com.ustadmobile.lib.util.parseRangeRequestHeader
import com.ustadmobile.port.sharedse.impl.http.RangeInputStream
import fi.iki.elonen.NanoHTTPD
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.regex.Pattern


class WebChunkWebViewClient(pathToZip: Container, mPresenter: WebChunkPresenter, context: Any) : WebViewClient() {


    private lateinit var containerManager: ContainerManager
    private lateinit var presenter: WebChunkPresenter
    private val indexMap = HashMap<String, IndexLog.IndexEntry>()
    private val linkPatterns = HashMap<Pattern, String>()
    var url: String? = null

    init {
        try {
            this.presenter = mPresenter
            val repoAppDatabase = UmAccountManager.getRepositoryForActiveAccount(context)
            val appDatabase = UmAppDatabase.getInstance(context)

            containerManager = ContainerManager(pathToZip, appDatabase, repoAppDatabase)

            val index = containerManager.getEntry("index.json")

            val indexLog = Gson().fromJson(UMIOUtils.readStreamToString(containerManager.getInputStream(index!!)), IndexLog::class.java)
            val indexList = indexLog.entries
            val firstUrlToOpen = indexList!![0]
            url = firstUrlToOpen.url


            for (log in indexList) {
                indexMap[log.url!!] = log
            }
            val linksMap = indexLog.links
            if (linksMap != null && linksMap.isNotEmpty()) {
                for (link in linksMap.keys) {
                    linkPatterns[Pattern.compile(link)] = linksMap[link]!!
                }
            }
        } catch (e: Exception) {
            System.err.println("Error opening Zip File from path $pathToZip")
        }

    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val requestUrl = checkWithPattern(request.url.toString())
        if (requestUrl != null) {
            presenter.handleUrlLinkToContentEntry(requestUrl)
            return true
        }
        return super.shouldOverrideUrlLoading(view, request)
    }


    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val requestUrl = StringBuilder(request.url.toString())
        val sourceUrl = checkWithPattern(requestUrl.toString())
        if (sourceUrl != null) {
            presenter.handleUrlLinkToContentEntry(sourceUrl)
            Handler(Looper.getMainLooper()).post { view.loadUrl(url) }
            return WebResourceResponse("text/html", "utf-8", null)
        }

        if (requestUrl.toString().contains("/Take-a-hint")) {
            return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("true".toByteArray(StandardCharsets.UTF_8)))
        }

        var log: IndexLog.IndexEntry? = indexMap[requestUrl.toString()]
        if (log == null) {
            for ((key) in indexMap) {

                if (key.contains("plixbrowse") && requestUrl.toString().contains("plixbrowse")) {
                    log = indexMap[key]
                    break
                }
                if (key.contains("https://www.ck12.org/assessment/api/render/questionInstance?qID") && requestUrl.toString().contains("https://www.ck12.org/assessment/api/render/questionInstance?qID")) {
                    log = indexMap[key]
                    break
                }
                if (key.contains("https://www.ck12.org/assessment/api/get/info/test/plix%20practice/plixID/") && requestUrl.toString().contains("https://www.ck12.org/assessment/api/get/info/test/plix%20practice/plixID/")) {
                    log = indexMap[key]
                    break
                }
                if (key.contains("https://www.ck12.org/assessment/api/start/tests/") && requestUrl.toString().contains("https://www.ck12.org/assessment/api/start/tests/")) {
                    log = indexMap[key]
                    break
                }
                if (key.contains("hint") && requestUrl.toString().contains("hint")) {
                    log = indexMap[key]
                    break
                }
                if (key.contains("attempt") && requestUrl.toString().contains("attempt")) {
                    log = indexMap[key]
                    break
                }
                if (key.contains("/api/internal/user/task/practice/") && requestUrl.toString().contains("/api/internal/user/task/practice/")) {
                    view.post { view.loadUrl(url) }
                    return super.shouldInterceptRequest(view, request)
                }
                if (key.contains("/assessment_item") && requestUrl.toString().contains("/assessment_item")) {
                    val langIndex = requestUrl.indexOf("?lang")

                    val newRequestUrl = requestUrl.substring(0, langIndex)
                    log = indexMap[newRequestUrl]
                    if (log != null) {
                        break
                    }
                }
                if (key.contains("/Quiz/Answer") && requestUrl.toString().contains("/Quiz/Answer")) {

                    val headers = request.requestHeaders
                    val pageIndex = headers["PageIndex"]
                    val answerId = headers["AnswerId"]

                    requestUrl.append("?page=").append(pageIndex)
                    if (answerId != null && answerId.isEmpty()) {
                        requestUrl.append("&answer=").append(answerId)
                    }

                    log = indexMap[requestUrl.toString()]
                    break

                }
            }
        }


        if (log == null) {
            System.err.println("did not find match for url in indexMap " + request.url.toString())
            return WebResourceResponse("", "utf-8", 200, "OK", null, null)
        }
        try {

            val entry = containerManager.getEntry(log.path!!)
                    ?: return WebResourceResponse("", "utf-8", 404, "Not Found", null, null)

            var mutMap = mutableMapOf<String, String>()
            if (log.headers != null) {
                mutMap.putAll(log.headers!!)
            }
            if(entry.containerEntryFile!!.compression == COMPRESSION_GZIP){
                mutMap["Content-Encoding"] = "gzip"
                mutMap["Content-Length"] = entry.containerEntryFile!!.ceCompressedSize.toString()
            }


            var data = containerManager.getInputStream(entry)

            // if not range header, load the file as normal
            var rangeHeader: String? = request.requestHeaders["Range"]
                    ?: return WebResourceResponse(log.mimeType, "utf-8", 200, "OK", mutMap, data)

            val totalLength = entry.containerEntryFile!!.ceTotalSize
            val isHEADRequest = request.method == "HEAD"

            var range = if (rangeHeader != null) {
                parseRangeRequestHeader(rangeHeader, totalLength)
            } else {
                null
            }
            if (range != null && range.statusCode == 206) {
                if(!isHEADRequest){
                    data = RangeInputStream(data, range.fromByte, range.toByte)
                }

                range.responseHeaders.forEach { mutMap[it.key] = it.value }
                return WebResourceResponse(log.mimeType, "utf-8", HttpURLConnection.HTTP_PARTIAL,
                        "Partial Content", mutMap, if(isHEADRequest) null else data)

            } else if (range?.statusCode == 416) {
                return WebResourceResponse("text/plain", "utf-8",416,
                        if (isHEADRequest) "" else "Range request not satisfiable", null, null)
            } else {

                mutMap["Content-Length"] = totalLength.toString()
                mutMap["Connection"] = "close"
                return WebResourceResponse(log.mimeType, "utf-8", 200,
                        "OK", mutMap, data)
            }
        } catch (e: Exception) {
            System.err.println("did not find entry in zip for url " + log.url!!)
            e.printStackTrace()
        }

        return super.shouldInterceptRequest(view, request)
    }

    private fun checkWithPattern(requestUrl: String): String? {
        for (linkPattern in linkPatterns.keys) {
            if (linkPattern.matcher(requestUrl).lookingAt()) {
                return linkPatterns[linkPattern]
            }
        }
        return null
    }

    inner class IndexLog {

        var title: String? = null

        var entries: List<IndexEntry>? = null

        var links: Map<String, String>? = null

        inner class IndexEntry {

            var url: String? = null

            var mimeType: String? = null

            var path: String? = null

            var headers: Map<String, String>? = null

            var requestHeaders: Map<String, String>? = null

        }

    }

}
