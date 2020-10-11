package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.impl.dumpException
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ContentEntryImportLinkView
import com.ustadmobile.door.doorMainDispatcher
import io.ktor.client.call.receive
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.client.statement.HttpStatement
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

class ContentEntryImportLinkPresenter(context: Any, arguments: Map<String, String>, view: ContentEntryImportLinkView,
                                      di: DI) :
        UstadBaseController<ContentEntryImportLinkView>(context, arguments, view, di) {

    val accountManager: UstadAccountManager by instance()

    private val currentHttpClient = defaultHttpClient()

    fun handleClickDone(link: String) {
        GlobalScope.launch(doorMainDispatcher()) {

            view.showHideProgress(false)
            try {
                currentHttpClient.post<HttpStatement>() {
                    url(UMFileUtil.joinPaths(accountManager.activeAccount.endpointUrl, "/import/validateLink/"))
                    parameter("url", link)
                }.execute() {

                    val status = it.status
                    if (status.value != 200) {
                        view.validLink = false
                        view.showHideProgress(true)
                        return@execute
                    }

                    val data = it.receive<ImportedContentEntryMetaData>()
                    view.showHideProgress(true)
                    view.finishWithResult(data)

                }

            }catch (e: Exception){
                view.showHideProgress(true)
                dumpException(e)
            }
        }

    }

}