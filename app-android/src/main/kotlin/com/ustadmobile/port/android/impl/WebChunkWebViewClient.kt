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
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.regex.Pattern


class WebChunkWebViewClient(pathToZip: Container, mPresenter: WebChunkPresenter, context: Any) : WebViewClient() {


    private var containerManager: ContainerManager? = null
    private var presenter: WebChunkPresenter? = null
    private val indexMap = HashMap<String, IndexLog.IndexEntry>()
    private val linkPatterns = HashMap<Pattern, String>()
    var url: String? = null

    init {
        try {
            this.presenter = mPresenter
            val repoAppDatabase = UmAccountManager.getRepositoryForActiveAccount(context)
            val appDatabase = UmAppDatabase.getInstance(context)

            containerManager = ContainerManager(pathToZip, appDatabase, repoAppDatabase)

            val index = containerManager!!.getEntry("index.json")

            val indexLog = Gson().fromJson(UMIOUtils.readStreamToString(containerManager!!.getInputStream(index!!)), IndexLog::class.java)
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
            presenter!!.handleUrlLinkToContentEntry(requestUrl)
            return true
        }
        return super.shouldOverrideUrlLoading(view, request)
    }


    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val requestUrl = StringBuilder(request.url.toString())
        val sourceUrl = checkWithPattern(requestUrl.toString())
        if (sourceUrl != null) {
            presenter!!.handleUrlLinkToContentEntry(sourceUrl)
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
            val data = containerManager!!.getInputStream(containerManager!!.getEntry(log.path!!)!!)

            return WebResourceResponse(log.mimeType, "utf-8", 200, "OK", log.headers, data)
        } catch (e: IOException) {
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
