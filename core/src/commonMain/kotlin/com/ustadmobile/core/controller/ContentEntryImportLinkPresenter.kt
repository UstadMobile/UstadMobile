package com.ustadmobile.core.controller

import com.ustadmobile.core.networkmanager.defaultHttClient
import com.ustadmobile.core.view.ContentEntryImportLinkView
import io.ktor.client.request.get
import io.ktor.http.URLParserException
import io.ktor.http.Url

class ContentEntryImportLinkPresenter(context: Any, arguments: Map<String, String?>, view: ContentEntryImportLinkView) :
        UstadBaseController<ContentEntryImportLinkView>(context, arguments, view) {


    suspend fun handleUrlTextUpdated(url: String) {
        val isValid = checkIfH5PValidAndReturnItsContent(url)?.first

        if (isValid == null) {
            view.showUrlStatus(false, "Invalid Url")
            return
        }

        if (isValid) {
            view.showUrlStatus(isValid, "")
            view.displayUrl(url)
        } else {
            view.showUrlStatus(isValid, "Content not supported")
        }
    }


    fun handleClickImport() {
        //check if we have
    }


}

suspend fun checkIfH5PValidAndReturnItsContent(url: String): Pair<Boolean?, String>? {
    var urlLink: Url?
    try {
        urlLink = Url(url)
    } catch (exception: URLParserException) {
        return null
    }

    var content = defaultHttClient().get<String>(urlLink)
    return Pair(content.contains("H5PIntegration"), content)


}