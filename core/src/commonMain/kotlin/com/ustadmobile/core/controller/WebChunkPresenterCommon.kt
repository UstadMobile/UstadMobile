package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NoAppFoundException
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.ARG_REFERRER
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.GoToEntryFn
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.goToContentEntry
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.HomeView
import com.ustadmobile.core.view.WebChunkView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlin.js.JsName

open class IndexLog {

    var title: String? = null

    var entries: MutableList<IndexEntry>? = null

    var links: Map<String, String>? = null

    inner class IndexEntry {

        var url: String = ""

        var mimeType: String? = null

        var path: String? = null

        var headers: Map<String, String>? = null

        var requestHeaders: Map<String, String>? = null

    }

}

abstract class WebChunkPresenterCommon(context: Any, arguments: Map<String, String>,
                                       view: WebChunkView,
                                       private val isDownloadEnabled: Boolean,
                                       private val appRepo: UmAppDatabase,
                                       val umAppDb: UmAppDatabase,
                                       private val goToEntryFn: GoToEntryFn = ::goToContentEntry)

    : UstadBaseController<WebChunkView>(context, arguments, view) {

    private var navigation: String? = null

    internal var containerUid: Long? = null

    @JsName("handleMountChunk")
    abstract suspend fun handleMountChunk()

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        var entryUuid = arguments.getValue(ContentEntryDetailPresenter.ARG_CONTENT_ENTRY_UID)!!.toLong()
        containerUid = arguments.getValue(ContentEntryDetailPresenter.ARG_CONTAINER_UID)!!.toLong()

        navigation = arguments[ARG_REFERRER] ?: ""

        GlobalScope.launch {
            try {
                val result = umAppDb.contentEntryDao.getContentByUuidAsync(entryUuid)
                view.runOnUiThread(Runnable {
                    val resultTitle = result?.title
                    if (resultTitle != null)
                        view.setToolbarTitle(resultTitle)
                })
            } catch (e: Exception) {
                view.runOnUiThread(Runnable {
                    view.showError(UstadMobileSystemImpl.instance
                            .getString(MessageID.error_opening_file, context))
                })
            }

            handleMountChunk()
        }


    }

    @JsName("handleUrlLinkToContentEntry")
    fun handleUrlLinkToContentEntry(sourceUrl: String) {
        val impl = UstadMobileSystemImpl.instance

        val dest = sourceUrl.replace("content-detail?",
                ContentEntryDetailView.VIEW_NAME + "?")
        val params = UMFileUtil.parseURLQueryString(dest)

        if (params.containsKey("sourceUrl")) {

            GlobalScope.launch {
                try {
                    val entry = appRepo.contentEntryDao.findBySourceUrlWithContentEntryStatusAsync(params.getValue("sourceUrl"))
                            ?: throw IllegalArgumentException("No File found")
                    goToEntryFn(entry.contentEntryUid, umAppDb, context, impl, true,
                            true,
                            arguments[ContentEntryListFragmentPresenter.ARG_NO_IFRAMES]?.toBoolean()!!)
                } catch (e: Exception) {
                    if (e is NoAppFoundException) {
                        view.showErrorWithAction(impl.getString(MessageID.no_app_found, context),
                                MessageID.get_app,
                                e.mimeType!!)
                    } else {
                        view.showError(e.message!!)
                    }
                }

            }

        }
    }

    fun handleUpNavigation() {
        val impl = UstadMobileSystemImpl.instance
        val lastEntryListArgs = UMFileUtil.getLastReferrerArgsByViewname(ContentEntryDetailView.VIEW_NAME, navigation!!)
        if (lastEntryListArgs !=
                null) {
            impl.go(ContentEntryDetailView.VIEW_NAME,
                    UMFileUtil.parseURLQueryString(lastEntryListArgs), view.viewContext,
                    UstadMobileSystemCommon.GO_FLAG_CLEAR_TOP or UstadMobileSystemCommon.GO_FLAG_SINGLE_TOP)
        } else {
            impl.go(HomeView.VIEW_NAME, mapOf(), view.viewContext,
                    UstadMobileSystemCommon.GO_FLAG_CLEAR_TOP or UstadMobileSystemCommon.GO_FLAG_SINGLE_TOP)
        }

    }

}
