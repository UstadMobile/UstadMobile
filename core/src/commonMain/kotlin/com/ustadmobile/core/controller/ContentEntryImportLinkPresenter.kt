package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.dumpException
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.ext.putFromOtherMapIfPresent
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.view.ContentEntryImportLinkView
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEAF
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_POPUPTO_ON_FINISH
import com.ustadmobile.core.view.UstadView.Companion.ARG_RESULT_DEST_KEY
import com.ustadmobile.door.doorMainDispatcher
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
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
        presenterScope.launch(doorMainDispatcher()) {

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

                    val data = it.receive<MetadataResult>()
                    view.showHideProgress(true)

                    if (arguments.containsKey(ARG_RESULT_DEST_KEY)) {
                        view.finishWithResult(data)
                    } else {
                        val args = mutableMapOf<String, String>()
                        args.putEntityAsJson(ContentEntryEdit2View.ARG_IMPORTED_METADATA,
                                MetadataResult.serializer(), data)
                        args[ARG_POPUPTO_ON_FINISH] = ContentEntryImportLinkView.VIEW_NAME
                        args.putFromOtherMapIfPresent(arguments, ARG_LEAF)
                        args.putFromOtherMapIfPresent(arguments, ARG_PARENT_ENTRY_UID)
                        systemImpl.go(ContentEntryEdit2View.VIEW_NAME,
                                args, context)
                    }


                }

            } catch (e: Exception) {
                view.showHideProgress(true)
                view.showSnackBar(systemImpl.getString(MessageID.import_link_error, context))
                dumpException(e)
            }
        }

    }

}