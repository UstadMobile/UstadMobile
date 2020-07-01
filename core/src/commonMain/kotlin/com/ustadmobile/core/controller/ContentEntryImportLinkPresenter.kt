package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.model.HeadResponse
import com.ustadmobile.core.networkmanager.PlatformHttpClient
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.view.ContentEntryImportLinkView
import com.ustadmobile.core.view.ContentEntryImportLinkView.Companion.CONTENT_ENTRY_PARENT_UID
import com.ustadmobile.core.view.UstadView
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpStatement
import io.ktor.http.URLParserException
import io.ktor.http.Url
import io.ktor.util.toMap
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class ContentEntryImportLinkPresenter(context: Any, arguments: Map<String, String>,
                                      view: ContentEntryImportLinkView,
                                      var endpointUrl: String,
                                      val db: UmAppDatabase,
                                      val repoDb: UmAppDatabase) :
        UstadBaseController<ContentEntryImportLinkView>(context, arguments, view) {


    private var videoTitle: String? = null

    private var parentContentEntryUid: Long = 0

    private var contentEntryUid = 0L

    private var hp5Url: String = ""

    private var contentType = -1

    var isDoneEnabled = false

    var jobCount = 0

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        parentContentEntryUid = arguments.getValue(CONTENT_ENTRY_PARENT_UID)?.toLong() ?: 0L
        contentEntryUid = arguments[UstadView.ARG_CONTENT_ENTRY_UID]?.toLong() ?: 0L
        if (contentEntryUid != 0L) {
            updateUIWithExistingContentEntry()
        }


    }

    fun updateUIWithExistingContentEntry(): Job {
        jobCount++
        checkProgressBar()
        return GlobalScope.launch {

            val contentEntry = db.contentEntryDao.findByUidAsync(contentEntryUid)
            videoTitle = contentEntry!!.title
            handleTitleChanged(videoTitle!!)

            if (contentEntry.sourceUrl != null) {
                view.updateSourceUrl(contentEntry.sourceUrl!!)
            }
            jobCount--
            checkProgressBar()

        }
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
                    url = headResponse.headers["location"]?.get(0)!!

                    defaultHttpClient().get<HttpStatement>(url).execute { response ->
                        headResponse = HeadResponse(response.status.value, response.headers.toMap())
                    }

//                    var response = defaultHttpClient().get<HttpResponse>(url)
//                    headResponse = HeadResponse(response.status.value, response.headers.toMap())
//
//                    response.discardRemaining()
//                    response.close()
                }

            } catch (e: Exception) {
                view.showUrlStatus(false, UstadMobileSystemImpl.instance.getString(MessageID.invalid_url, context))
                jobCount--
                checkProgressBar()
                return@launch
            }

            val headResponseVal = headResponse ?: return@launch

            contentType = -1

            if (headResponseVal.status != 200) {
                view.showUrlStatus(false, UstadMobileSystemImpl.instance.getString(MessageID.invalid_url, context))
                jobCount--
                checkProgressBar()
                return@launch
            }

            var contentTypeHeader = headResponseVal.headers["content-type"]?.get(0) ?: ""
            if (contentTypeHeader.contains(";")) {
                contentTypeHeader = contentTypeHeader.split(";")[0]
            }

            val length = headResponseVal.headers["content-length"]?.get(0)?.toInt() ?: 0

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
                view.showHideVideoTitle(contentEntryUid == 0L)
                if (contentEntryUid != 0L) {
                    handleTitleChanged(videoTitle!!)
                }
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
                view.showUrlStatus(false, UstadMobileSystemImpl.instance.getString(MessageID.invalid_url, context))
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
                view.showUrlStatus(isValid, UstadMobileSystemImpl.instance.getString(MessageID.invalid_url, context))
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
            view.enableDisableEditText(false)
            view.showHideErrorMessage(false)
            var requestedOk = false
            try {
                when (contentType) {
                    HTML -> {
                        client.get<Unit>("$endpointUrl/ImportH5P/importUrl") {
                            parameter("hp5Url", hp5Url)
                            parameter("parentUid", parentContentEntryUid)
                            if (contentEntryUid != 0L) parameter("contentEntryUid", contentEntryUid)
                        }
                        requestedOk = true

                    }
                    VIDEO -> {
                        if (videoTitle.isNullOrEmpty()) {
                            view.showNoTitleEntered(UstadMobileSystemImpl.instance.getString(MessageID.import_title_not_entered, context))
                            jobCount--
                            checkProgressBar()
                            return@launch
                        }

                        client.get<Unit>("$endpointUrl/ImportH5P/importVideo") {
                            parameter("hp5Url", hp5Url)
                            parameter("parentUid", parentContentEntryUid)
                            parameter("title", videoTitle)
                            if (contentEntryUid != 0L) parameter("contentEntryUid", contentEntryUid)
                        }
                        requestedOk = true
                    }
                }
            } catch (e: Exception) {
                //Do nothing - to be reviewed
            }



            if (requestedOk) {
                view.enableDisableEditText(true)


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
        println("error :$exception.message")
        return null
    }

    return try {
        defaultHttpClient().get<String>(urlLink)
    } catch (e: Exception) {
        null
    }


}