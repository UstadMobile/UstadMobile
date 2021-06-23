package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.dumpException
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.ext.putFromOtherMapIfPresent
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.view.ContentEntryImportLinkView
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEAF
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_POPUPTO_ON_FINISH
import com.ustadmobile.core.view.UstadView.Companion.ARG_RESULT_DEST_ID
import com.ustadmobile.core.view.UstadView.Companion.CURRENT_DEST
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ContentEntry
import io.ktor.client.*
import io.ktor.client.call.receive
import io.ktor.client.features.*
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

    val systemImpl: UstadMobileSystemImpl by instance()

    private val currentHttpClient: HttpClient by instance()

    fun handleClickDone(link: String) {
        GlobalScope.launch(doorMainDispatcher()) {

            view.showHideProgress(false)
            try {
                currentHttpClient.post<HttpStatement>() {
                    url(UMFileUtil.joinPaths(accountManager.activeAccount.endpointUrl,
                            "/import/validateLink"))
                    parameter("url", link)
                    expectSuccess = false
                }.execute() {

                    val status = it.status
                    if (status.value != 200) {
                        view.validLink = false
                        view.showHideProgress(true)
                        return@execute
                    }

                    val data = it.receive<ImportedContentEntryMetaData>()
                    view.showHideProgress(true)

                    if (arguments.containsKey(ARG_RESULT_DEST_ID)) {
                        view.finishWithResult(data)
                    } else {
                        val args = mutableMapOf<String, String>()
                        args.putEntityAsJson(ContentEntryEdit2View.ARG_IMPORTED_METADATA,
                                ImportedContentEntryMetaData.serializer(), data)
                        args.putFromOtherMapIfPresent(arguments, ARG_LEAF)
                        args.putFromOtherMapIfPresent(arguments, ARG_PARENT_ENTRY_UID)
                        systemImpl.go(ContentEntryEdit2View.VIEW_NAME,
                                args, context)
                    }


                }

            } catch (e: Exception) {
                view.showHideProgress(true)
                dumpException(e)
            }
        }

    }

}