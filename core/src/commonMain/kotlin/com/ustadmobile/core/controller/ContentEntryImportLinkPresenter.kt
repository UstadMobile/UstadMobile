package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.model.HeadResponse
import com.ustadmobile.core.networkmanager.PlatformHttpClient
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.view.ContentEntryImportLinkView
import com.ustadmobile.core.view.ContentEntryImportLinkView.Companion.CONTENT_ENTRY_PARENT_UID
import com.ustadmobile.lib.db.entities.H5PImportData
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.features.HttpRedirect
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.parameter
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.discardRemaining
import io.ktor.http.HeaderValue
import io.ktor.http.URLParserException
import io.ktor.http.Url
import io.ktor.http.userAgent
import io.ktor.util.toMap
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class ContentEntryImportLinkPresenter(context: Any, arguments: Map<String, String?>, view: ContentEntryImportLinkView, var endpointUrl: String) :
        UstadBaseController<ContentEntryImportLinkView>(context, arguments, view) {


    private var videoTitle: String? = null

    private var parentContentEntryUid: Long = 0

    private var hp5Url: String = ""

    private var contentType = -1

    var isDoneEnabled = false

    var jobCount = 0

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        parentContentEntryUid = arguments.getValue(CONTENT_ENTRY_PARENT_UID)!!.toLong()
    }

    fun checkProgressBar() {
        view.showBaseProgressBar(jobCount > 0)
    }


    fun handleUrlTextUpdated(url: String): Job {
        var url = url
        jobCount++
        checkProgressBar()
        return GlobalScope.launch {
            view.showHideVideoTitle(false)
            isDoneEnabled = false
            view.checkDoneButton()

            val isGoogleDrive = url.contains("drive.google.com")

            if (isGoogleDrive) {
                var id = url.substringAfter("id=")
                if (id == url) {
                    val startIndex = url.indexOf("file/d/") + 7
                    val endIndex = url.indexOf("/", startIndex)
                    id = url.substring(startIndex, endIndex)
                }

                url = GOOGLE_DRIVE_LINK + id
            }


            var headResponse: HeadResponse?
            try {

                var client = PlatformHttpClient()
                headResponse = client.headRequest(url)

                if (headResponse.status == 302 && isGoogleDrive) {
                    val googleDriveUrl = headResponse.headers["location"]?.get(0)!!
                    var response = defaultHttpClient().get<HttpResponse>(googleDriveUrl)
                    headResponse = HeadResponse(response.status.value, response.headers.toMap())

                    response.discardRemaining()
                    response.close()
                }

            } catch (e: Exception) {
                view.showUrlStatus(false, UstadMobileSystemImpl.instance.getString(MessageID.import_link_invalid_url, context))
                jobCount--
                checkProgressBar()
                return@launch
            }

            contentType = -1

            if (headResponse.status != 200) {
                view.showUrlStatus(false, UstadMobileSystemImpl.instance.getString(MessageID.import_link_invalid_url, context))
                jobCount--
                checkProgressBar()
                return@launch
            }

            var contentTypeHeader = headResponse.headers["content-type"]?.get(0) ?: ""
            if (contentTypeHeader.contains(";")) {
                contentTypeHeader = contentTypeHeader.split(";")[0]
            }

            val length = headResponse.headers["content-length"]?.get(0)?.toInt() ?: 0

            if (contentTypeHeader.startsWith("video/")) {

                if (length >= FILE_SIZE) {
                    view.showUrlStatus(false, UstadMobileSystemImpl.instance.getString(MessageID.import_link_big_size, context))
                    jobCount--
                    checkProgressBar()
                    return@launch
                }

                contentType = VIDEO
                hp5Url = url
                view.showUrlStatus(true, "")
                view.showHideVideoTitle(true)
                jobCount--
                checkProgressBar()
                return@launch

            } else if (!listOfHtmlContentType.contains(contentTypeHeader)) {
                view.showUrlStatus(false, UstadMobileSystemImpl.instance.getString(MessageID.import_link_content_not_supported, context))
                jobCount--
                checkProgressBar()
                return@launch
            }

            val content = checkIfH5PValidAndReturnItsContent(url)

            if (content.isNullOrEmpty()) {
                view.showUrlStatus(false, UstadMobileSystemImpl.instance.getString(MessageID.import_link_invalid_url, context))
                jobCount--
                checkProgressBar()
                return@launch
            }

            val isValid = content.contains("H5PIntegration")
            if (isValid) {
                contentType = HTML
                hp5Url = url
                view.showUrlStatus(isValid, "")
                view.displayUrl(url)
                isDoneEnabled = true
                view.checkDoneButton()
            } else {
                view.showUrlStatus(isValid, UstadMobileSystemImpl.instance.getString(MessageID.import_link_invalid_url, context))
            }
            jobCount--
            checkProgressBar()
        }
    }


    fun handleClickImport(): Job {
        jobCount++
        checkProgressBar()
        return GlobalScope.launch {
            val client = defaultHttpClient()
            var response: HttpResponse? = null
            view.enableDisableEditText(false)
            view.showHideErrorMessage(false)
            try {
                when (contentType) {
                    HTML -> {

                        response = client.get<HttpResponse>("$endpointUrl/ImportH5P/importUrl") {
                            parameter("hp5Url", hp5Url)
                            parameter("parentUid", parentContentEntryUid)
                        }

                    }
                    VIDEO -> {

                        if (videoTitle.isNullOrEmpty()) {
                            view.showNoTitleEntered(UstadMobileSystemImpl.instance.getString(MessageID.import_title_not_entered, context))
                            jobCount--
                            checkProgressBar()
                            return@launch
                        }

                        response = client.get<HttpResponse>("$endpointUrl/ImportH5P/importVideo") {
                            parameter("hp5Url", hp5Url)
                            parameter("parentUid", parentContentEntryUid)
                            parameter("title", videoTitle)
                        }
                    }
                }
            } catch (e: Exception) {
                response = null
            }



            if (response?.status?.value == 200) {

                val content = response.receive<H5PImportData>()
                view.enableDisableEditText(true)

                val db = UmAppDatabase.getInstance(context)
                db.contentEntryDao.insert(content.contentEntry)
                db.contentEntryParentChildJoinDao.insert(content.parentChildJoin)
                db.containerDao.insert(content.container)

                view.runOnUiThread(Runnable {
                    jobCount--
                    checkProgressBar()
                    view.returnResult()
                })

            } else {

                view.enableDisableEditText(true)
                view.showHideErrorMessage(true)
                jobCount--
                checkProgressBar()

            }
        }


    }

    fun handleTitleChanged(title: String) {
        this.videoTitle = title
        if (title.isNotEmpty()) {
            isDoneEnabled = true
            view.checkDoneButton()
            view.showNoTitleEntered("")
        }
    }

    companion object {

        const val HTML = 1

        const val VIDEO = 2

        const val FILE_SIZE = 104857600

        val listOfHtmlContentType = listOf("text/html", "application/xhtml+xml", "application/xml", "text/xml")

        var GOOGLE_DRIVE_LINK = "https://drive.google.com/uc?export=download&id="

    }

}


suspend fun checkIfH5PValidAndReturnItsContent(url: String): String? {
    var urlLink: Url?
    try {
        urlLink = Url(url)
    } catch (exception: URLParserException) {
        return null
    }

    return try {
        defaultHttpClient().get<String>(urlLink)
    } catch (e: Exception) {
        null
    }


}