package com.ustadmobile.core.controller

import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.dumpException
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.ext.putFromOtherMapIfPresent
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.ContentEntryEdit2View.Companion.ARG_IMPORTED_METADATA
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEAF
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_POPUPTO_ON_FINISH
import com.ustadmobile.core.view.UstadView.Companion.ARG_RESULT_DEST_KEY
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI
import org.kodein.di.instance


class ContentEntryImportLinkPresenter(context: Any, arguments: Map<String, String>, view: ContentEntryImportLinkView,
                                      lifecycleOwner: DoorLifecycleOwner,
                                      di: DI) :
    UstadEditPresenter<ContentEntryImportLinkView, String>(context, arguments, view, di, lifecycleOwner){


    private val currentHttpClient: HttpClient by instance()

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON

    override fun onLoadFromJson(bundle: Map<String, String>): String? {
        return ""
    }

    override fun handleClickSave(entity: String) {
        GlobalScope.launch(doorMainDispatcher()) {

            view.showProgress = true
            try {
                currentHttpClient.post<HttpStatement>() {
                    url(UMFileUtil.joinPaths(accountManager.activeAccount.endpointUrl,
                        "/import/validateLink"))
                    parameter("url", entity)
                    expectSuccess = false
                }.execute() {

                    val status = it.status
                    if (status.value != 200) {
                        view.validLink = false
                        view.showProgress = false
                        return@execute
                    }

                    val data = it.receive<MetadataResult>()
                    view.showProgress = false

                    if (arguments.containsKey(ARG_RESULT_DEST_KEY)) {
                        finishWithResult(safeStringify(di, ListSerializer(MetadataResult.serializer()), listOf(data)))
                    } else {
                        val args = mutableMapOf<String, String>()
                        args.putEntityAsJson(
                            ARG_IMPORTED_METADATA,
                            MetadataResult.serializer(), data)
                        args[ARG_POPUPTO_ON_FINISH] = ContentEntryImportLinkView.VIEW_NAME
                        args.putFromOtherMapIfPresent(arguments, ARG_LEAF)
                        args.putFromOtherMapIfPresent(arguments, ARG_PARENT_ENTRY_UID)
                        systemImpl.go(ContentEntryEdit2View.VIEW_NAME,
                            args, context)
                    }
                }

            } catch (e: Exception) {
                view.showProgress = true
                view.showSnackBar(systemImpl.getString(MessageID.import_link_error, context))
                dumpException(e)
            }
        }
    }

}