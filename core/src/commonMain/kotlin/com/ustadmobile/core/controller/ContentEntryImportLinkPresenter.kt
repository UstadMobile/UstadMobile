package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.ext.putFromOtherMapIfPresent
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.view.ContentEntryEdit2View.Companion.BLOCK_REQUIRED
import com.ustadmobile.core.view.ContentEntryImportLinkView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEAF
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_RESULT_DEST_VIEWNAME
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ContentEntry
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI
import org.kodein.di.instance

class ContentEntryImportLinkPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: ContentEntryImportLinkView,
    di: DI
): UstadBaseController<ContentEntryImportLinkView>(context, arguments, view, di) {

    val accountManager: UstadAccountManager by instance()

    val systemImpl: UstadMobileSystemImpl by instance()

    private val currentHttpClient: HttpClient by instance()

    fun handleClickDone(link: String) {
        presenterScope.launch(doorMainDispatcher()) {

            view.inProgress = true
            try {
                val response = currentHttpClient.post {
                    url(UMFileUtil.joinPaths(accountManager.activeAccount.endpointUrl,
                            "/import/validateLink"))
                    parameter("url", link)
                    expectSuccess = false
                }

                val status = response.status
                if (status.value != 200) {
                    view.validLink = false
                    view.inProgress = false
                    return@launch
                }

                val data = response.body<MetadataResult>()
                view.inProgress = false

                if (arguments[ARG_RESULT_DEST_VIEWNAME] == ContentEntryEdit2View.VIEW_NAME) {
                    finishWithResult(safeStringify(di, ListSerializer(MetadataResult.serializer()),
                        listOf(data)))
                } else {
                    val args = mutableMapOf<String, String>()
                    args.putEntityAsJson(ContentEntryEdit2View.ARG_IMPORTED_METADATA, json,
                            MetadataResult.serializer(), data)
                    args.putFromOtherMapIfPresent(arguments, ARG_LEAF)
                    args.putFromOtherMapIfPresent(arguments, ARG_PARENT_ENTRY_UID)
                    args.putFromOtherMapIfPresent(arguments, BLOCK_REQUIRED)
                    args.putFromOtherMapIfPresent(arguments, UstadView.ARG_CLAZZUID)

                    navigateForResult(
                        NavigateForResultOptions(
                            this@ContentEntryImportLinkPresenter,
                            null,
                            ContentEntryEdit2View.VIEW_NAME,
                            ContentEntry::class,
                            ContentEntry.serializer(),
                            arguments = args)
                    )
                }
            } catch (e: Exception) {
                view.inProgress = false
                view.showSnackBar(systemImpl.getString(MessageID.import_link_error, context))
                Napier.e("Exception attempting to input import link url", e)
            }
        }

    }

}