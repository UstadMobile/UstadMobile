package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.view.ContentEntryImportLinkView
import com.ustadmobile.core.view.ContentEntryImportLinkView.Companion.CONTENT_ENTRY_PARENT_UID
import com.ustadmobile.lib.db.entities.H5PImportData
import io.ktor.client.call.receive
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.response.HttpResponse
import io.ktor.http.URLBuilder
import io.ktor.http.URLParserException
import io.ktor.http.Url
import kotlinx.coroutines.Runnable

class ContentEntryImportLinkPresenter(context: Any, arguments: Map<String, String?>, view: ContentEntryImportLinkView, var endpointUrl: String) :
        UstadBaseController<ContentEntryImportLinkView>(context, arguments, view) {

    private var parentContentEntryUid: Long = 0

    private var hp5Url: String = ""

    val systemImpl = UstadMobileSystemImpl.instance

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        parentContentEntryUid = arguments.getValue(CONTENT_ENTRY_PARENT_UID)!!.toLong()


    }

    suspend fun handleUrlTextUpdated(url: String) {
        val content = checkIfH5PValidAndReturnItsContent(url)

        if (content == null) {
            view.showUrlStatus(false, "Invalid Url")
            return
        }

        val isValid = content.contains("H5PIntegration")
        if (isValid) {
            this.hp5Url = url
            view.showUrlStatus(isValid, "")
            view.displayUrl(url)
        } else {
            view.showUrlStatus(isValid, "Content not supported")
        }
    }


    suspend fun handleClickImport() {

        val client = defaultHttpClient()
        val response = client.get<HttpResponse>("$endpointUrl/ImportH5P/importUrl") {
            parameter("hp5Url", hp5Url)
            parameter("parentUid", parentContentEntryUid)
        }


        if (response.status.value == 200) {

            val content = response.receive<H5PImportData>()
            val db = UmAppDatabase.getInstance(context)
            db.contentEntryDao.insert(content.contentEntry)
            db.contentEntryParentChildJoinDao.insert(content.parentChildJoin)
            db.containerDao.insert(content.container)

        } else {


        }

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